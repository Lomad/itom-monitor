package com.winning.itom.monitor.machine.realtime.analyzer;

import com.winning.itom.monitor.api.ICollectDataAnalyzer;
import com.winning.itom.monitor.api.entity.CollectData;
import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.calculator.RedisTimeCalculator;
import com.winning.itom.monitor.machine.realtime.tasks.WinProcessorTimePerMinuteTask;
import com.winning.itom.task.core.ITimedTaskFactory;
import com.winning.itom.task.core.entity.TimedTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by nicholasyan on 17/3/21.
 */
public abstract class AbstractAnalyzer implements ICollectDataAnalyzer {

    @Autowired(required = false)
    protected MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("itomRedisTemplate")
    protected RedisTemplate redisTemplate;

    @Autowired
    @Qualifier("MachineTimedTaskFactory")
    protected ITimedTaskFactory timedTaskFactory;

    private Map<String, RedisTimeCalculator> redisTimeCalculatorMap = new Hashtable<>();

    protected void analyzeCollectData(RequestInfo requestInfo,
                                      CollectDataMap collectDataMap,
                                      String collectDataName,
                                      int[] lastMinutes) {

        CollectData collectData = collectDataMap.getCollectData(collectDataName);
        double currentValue = Double.parseDouble(collectData.getValue().toString());

        String id = requestInfo.getClientId() + "." +
                requestInfo.getIpAddress() + "." + collectDataName;

        RedisTimeCalculator redisTimeCalculator = this.getRedisTimeCalculator(id);
        long currentTime = requestInfo.getTimestamp();
        //精确到秒
        long currentSecond = currentTime - currentTime % 1000;
        long currentMinute = currentTime - currentTime % 60000;
        redisTimeCalculator.addValue(currentSecond, currentValue);
        //加入到队列中,计算当前的
        String realtimeKey = id + ".realtime";
        Map<String, Double> values = new HashMap<>();
        values.put("current", currentValue);


        if (lastMinutes != null) {
            for (int minutes : lastMinutes) {
                long past = currentSecond - 60000 * minutes + 1;
                double lastMinutesAvg = redisTimeCalculator.calcAvg(past, currentTime);
                String key = "last" + minutes + "m";
                values.put(key, lastMinutesAvg);
            }
        }

        //当前分钟的平均值
        double currentMinuteAvg = redisTimeCalculator.calcAvg(currentMinute, currentTime);
        this.redisTemplate.opsForHash().putAll(realtimeKey, values);
        this.redisTemplate.expire(realtimeKey, 1, TimeUnit.MINUTES);

        String minuteKey = id + ".minutes";
        this.redisTemplate.opsForZSet().removeRangeByScore(minuteKey, currentMinute, currentMinute);
        String currentMinuteAvgKey = currentMinute + "," + currentMinuteAvg;
        this.redisTemplate.opsForZSet().add(minuteKey, currentMinuteAvgKey, currentMinute);

        this.addStoreMinuteValueTask(requestInfo, id, currentMinute, collectDataName);
    }

    protected RedisTimeCalculator getRedisTimeCalculator(String key) {
        if (!this.redisTimeCalculatorMap.containsKey(key))
            this.redisTimeCalculatorMap.put(key, new RedisTimeCalculator(key, this.redisTemplate));

        return this.redisTimeCalculatorMap.get(key);
    }

    private void addStoreMinuteValueTask(RequestInfo requestInfo, String id, long currentMinute,
                                         String collectDataName) {
        String taskId = id + "." + String.valueOf(currentMinute);
        TimedTask task = new TimedTask(taskId);
        long nextTime = currentMinute + 61000;
        task.setRunTime(nextTime);
        task.setTodoTaskName(WinProcessorTimePerMinuteTask.TASK_NAME);
        task.put("id", id);
        task.put("time", String.valueOf(currentMinute));
        task.put("clientId", requestInfo.getClientId());
        task.put("clientName", requestInfo.getClientName());
        task.put("machineIP", requestInfo.getIpAddress());
        task.put("collectDataName", collectDataName);

        this.timedTaskFactory.addTimedTask(task);
    }

}
