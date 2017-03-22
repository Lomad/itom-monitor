package com.winning.itom.monitor.machine.realtime.analyzer;

import com.winning.itom.monitor.api.ICollectDataAnalyzer;
import com.winning.itom.monitor.api.constants.RequestParamConstants;
import com.winning.itom.monitor.api.entity.CollectData;
import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.calculator.RedisTimeCalculator;
import com.winning.itom.monitor.machine.utils.IDCreator;
import com.winning.itom.task.core.ITaskManager;
import com.winning.itom.task.core.ITimedTaskFactory;
import com.winning.itom.task.core.entity.TimedTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by nicholasyan on 17/3/21.
 */
public abstract class AbstractAnalyzer implements ICollectDataAnalyzer {

    protected final long MINUTE = 60000;
    protected final long HOUR = 60 * MINUTE;
    protected final long DAY = 24 * HOUR;

    @Autowired(required = false)
    protected MongoTemplate mongoTemplate;
    @Autowired
    @Qualifier("itomRedisTemplate")
    protected RedisTemplate redisTemplate;
    @Autowired
    @Qualifier("MachineTimedTaskFactory")
    protected ITimedTaskFactory timedTaskFactory;
    private Map<String, RedisTimeCalculator> redisTimeCalculatorMap = new Hashtable<>();

    @PostConstruct
    private void init() {
        this.initTask(this.timedTaskFactory.getTaskManager());
    }

    protected abstract void initTask(ITaskManager taskManager);

    protected void analyzeCollectData(RequestInfo requestInfo,
                                      CollectDataMap collectDataMap,
                                      String collectDataName,
                                      int[] lastMinutes) {

        CollectData collectData = collectDataMap.getCollectData(collectDataName);
        double currentValue = Double.parseDouble(collectData.getValue().toString());

        String id = IDCreator.createId(requestInfo, collectDataName);

        RedisTimeCalculator redisTimeCalculator = this.getRedisTimeCalculator(id);
        long currentTime = requestInfo.getTimestamp();
        //精确到秒
        long currentSecond = currentTime - currentTime % 1000;
        long currentMinute = currentTime - currentTime % MINUTE;
        redisTimeCalculator.addValue(currentSecond, currentValue);
        //加入到队列中,计算当前的
        String realtimeKey = IDCreator.createId(requestInfo, collectDataName, "realtime");
        Map<String, Double> values = new HashMap<>();
        values.put("current", currentValue);

        if (lastMinutes != null) {
            for (int minutes : lastMinutes) {
                long past = currentSecond - MINUTE * minutes + 1;
                double lastMinutesAvg = redisTimeCalculator.calcAvg(past, currentTime);
                String key = "last" + minutes + "m";
                values.put(key, lastMinutesAvg);
            }
        }

        //当前分钟的平均值
        double currentMinuteAvg = redisTimeCalculator.calcAvg(currentMinute, currentTime);
        this.redisTemplate.opsForHash().putAll(realtimeKey, values);
        this.redisTemplate.expire(realtimeKey, 1, TimeUnit.MINUTES);

        String minuteKey = IDCreator.createId(requestInfo, collectDataName, "minutes");
        this.redisTemplate.opsForZSet().removeRangeByScore(minuteKey, currentMinute, currentMinute);
        String currentMinuteAvgKey = currentMinute + "," + currentMinuteAvg;
        this.redisTemplate.opsForZSet().add(minuteKey, currentMinuteAvgKey, currentMinute);
    }

    protected RedisTimeCalculator getRedisTimeCalculator(String key) {
        if (!this.redisTimeCalculatorMap.containsKey(key))
            this.redisTimeCalculatorMap.put(key, new RedisTimeCalculator(key, this.redisTemplate));

        return this.redisTimeCalculatorMap.get(key);
    }


    protected void addTimedTask(RequestInfo requestInfo,
                                String taskName,
                                long currentMinute,
                                int intervalMinute,
                                Map<String, String> args) {

        String taskId = IDCreator.createId(requestInfo, taskName, String.valueOf(currentMinute));
        TimedTask task = new TimedTask(taskId);

        long nextTime = currentMinute + MINUTE * intervalMinute;

        task.setRunTime(nextTime);
        task.setTodoTaskName(taskName);
        task.put("time", String.valueOf(currentMinute));
        task.put(RequestParamConstants.CLIENT_ID, requestInfo.getClientId());
        task.put(RequestParamConstants.CLIENT_NAME, requestInfo.getClientName());
        task.put(RequestParamConstants.HOST_IP, requestInfo.getIpAddress());
        task.put(RequestParamConstants.HOST_NAME, requestInfo.getIpAddress());

        if (args != null)
            task.putAll(args);

        this.timedTaskFactory.addTimedTask(task);
    }


    protected long getCurrentMinute(RequestInfo requestInfo) {
        long currentTime = requestInfo.getTimestamp();
        return currentTime - currentTime % MINUTE;
    }

    protected long getCurrentHour(RequestInfo requestInfo) {
        long currentTime = requestInfo.getTimestamp();
        return currentTime - currentTime % HOUR;
    }


//    protected void addStoreMinuteValueTask(RequestInfo requestInfo,
//                                           String collectDataName,
//                                           long currentMinute) {
//
//        String id = this.getId(requestInfo, collectDataName);
//
//        String taskId = id + "." + String.valueOf(currentMinute);
//        TimedTask task = new TimedTask(taskId);
//        long nextTime = currentMinute + 61000;
//        task.setRunTime(nextTime);
//        task.setTodoTaskName(this.getMinuteTaskName());
//        task.put("id", id);
//        task.put("time", String.valueOf(currentMinute));
//        task.put("clientId", requestInfo.getClientId());
//        task.put("clientName", requestInfo.getClientName());
//        task.put("machineIP", requestInfo.getIpAddress());
//        task.put("collectDataName", collectDataName);
//
//        this.timedTaskFactory.addTimedTask(task);
//    }


}
