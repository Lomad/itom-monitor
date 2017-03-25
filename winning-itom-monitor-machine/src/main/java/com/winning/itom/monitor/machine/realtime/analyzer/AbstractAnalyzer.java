package com.winning.itom.monitor.machine.realtime.analyzer;

import com.alibaba.fastjson.JSON;
import com.winning.itom.monitor.api.ICollectDataAnalyzer;
import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.calculator.RealtimeRedisCalculator;
import com.winning.itom.monitor.machine.calculator.RedisTimeCalculator;
import com.winning.itom.monitor.machine.calculator.TimelineRedisCalculator;
import com.winning.itom.monitor.machine.realtime.tasks.entity.AnalyzerTimedTaskArgs;
import com.winning.itom.monitor.machine.utils.IDCreator;
import com.winning.itom.task.core.ITaskManager;
import com.winning.itom.task.core.ITimedTaskFactory;
import com.winning.itom.task.core.entity.TimedTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.Hashtable;
import java.util.Map;

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


    protected Double getDoubleValue(CollectDataMap collectDataMap,
                                    String collectDataName) {
        return Double.parseDouble(collectDataMap.getCollectData(collectDataName).getValue().toString());
    }


    protected void putRealtimeValue(RequestInfo requestInfo,
                                    CollectDataMap collectDataMap,
                                    String collectDataName) {
        String id = IDCreator.createId(requestInfo, collectDataName);
        double value = getDoubleValue(collectDataMap, collectDataName);
        RealtimeRedisCalculator.putRealtimeValue(redisTemplate, id, value, 1);
    }

    protected void putRealtimeInstanceValue(RequestInfo requestInfo,
                                            String collectDataName,
                                            String instanceName,
                                            double value) {
        String id = IDCreator.createId(requestInfo, collectDataName);
        RealtimeRedisCalculator.putRealtimeValue(redisTemplate, id, instanceName, value, 1);
    }


    protected void putTimelineValue(RequestInfo requestInfo,
                                    CollectDataMap collectDataMap,
                                    String collectDataName) {
        String id = IDCreator.createId(requestInfo, collectDataName);
        double value = getDoubleValue(collectDataMap, collectDataName);
        TimelineRedisCalculator.addTimelineValue(redisTemplate, id, requestInfo.getTimestamp(), value);
    }

    protected void putTimelineInstanceValue(RequestInfo requestInfo,
                                            String collectDataName,
                                            String instanceName,
                                            double value) {
        String id = IDCreator.createId(requestInfo, collectDataName, instanceName);
        TimelineRedisCalculator.addTimelineValue(redisTemplate, id, requestInfo.getTimestamp(), value);
    }


    protected Double calcLastMinuteAvg(RedisTemplate redisTemplate,
                                       RequestInfo requestInfo,
                                       String collectDataName,
                                       int lastMinutes) {

        String id = IDCreator.createId(requestInfo, collectDataName);
        long currentTime = requestInfo.getTimestamp();
        long past = currentTime - MINUTE * lastMinutes + 1;
        return TimelineRedisCalculator.calcAvg(redisTemplate, id, past, currentTime);
    }


    protected RedisTimeCalculator getRedisTimeCalculator(String key) {
        if (!this.redisTimeCalculatorMap.containsKey(key))
            this.redisTimeCalculatorMap.put(key, new RedisTimeCalculator(this.redisTemplate, key));

        return this.redisTimeCalculatorMap.get(key);
    }

    protected void addTimedTask(RequestInfo requestInfo,
                                String taskName,

                                int intervalMinute,
                                Map<String, String> args) {

    }


    protected void addTimedTask(RequestInfo requestInfo,
                                String taskName,
                                String instanceName,
                                int intervalMinute,
                                Map<String, String> args) {

        long currentMinute = this.getCurrentMinute(requestInfo);
        String taskId;
        if (instanceName == null)
            taskId = IDCreator.createId(requestInfo, taskName, String.valueOf(currentMinute));
        else
            taskId = IDCreator.createId(requestInfo, taskName, instanceName, String.valueOf(currentMinute));

        TimedTask task = new TimedTask(taskId);

        long nextTime = currentMinute + MINUTE * intervalMinute;

        task.setRunTime(nextTime);
        task.setTodoTaskName(taskName);

        AnalyzerTimedTaskArgs timedTaskArgs = new AnalyzerTimedTaskArgs(requestInfo, currentMinute, args);
        String json = JSON.toJSONString(timedTaskArgs);
        task.put(AnalyzerTimedTaskArgs.TASK_ARG_KEY, json);
        task.put("time", String.valueOf(currentMinute));
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


}
