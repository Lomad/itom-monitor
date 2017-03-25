package com.winning.itom.monitor.machine.realtime.tasks;

import com.alibaba.fastjson.JSON;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.calculator.TimelineRedisCalculator;
import com.winning.itom.monitor.machine.realtime.tasks.entity.AnalyzerTimedTaskArgs;
import com.winning.itom.monitor.machine.utils.IDCreator;
import com.winning.itom.task.core.ITask;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by nicholasyan on 17/3/25.
 */
public abstract class AnalyzerTimedTask implements ITask {

    protected final static long HOURLY = 60 * 60000;
    protected final static long MINUTE = 60000;
    protected final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected final RedisTemplate redisTemplate;
    protected final MongoTemplate mongoTemplate;


    public AnalyzerTimedTask(RedisTemplate redisTemplate,
                             MongoTemplate mongoTemplate) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void doTask(Map<String, String> args) {
        AnalyzerTimedTaskArgs analyzerTimeTaskArgs = null;

        if (args.containsKey(AnalyzerTimedTaskArgs.TASK_ARG_KEY)) {
            analyzerTimeTaskArgs = JSON.parseObject(args.get(AnalyzerTimedTaskArgs.TASK_ARG_KEY), AnalyzerTimedTaskArgs.class);
        } else {
            analyzerTimeTaskArgs = new AnalyzerTimedTaskArgs(args);
        }

        this.doTask(analyzerTimeTaskArgs);
    }

    protected abstract void doTask(AnalyzerTimedTaskArgs analyzerTimeTaskArgs);


    protected Double calcTimelineMinuteAvg(RequestInfo requestInfo, String collectDataName, long startTime) {
        String id = IDCreator.createId(requestInfo, collectDataName);
        return TimelineRedisCalculator.calcAvg(redisTemplate, id, startTime, startTime + 60000 - 1);
    }

    protected Double calcTimelineMinuteAvg(RequestInfo requestInfo, String collectDataName, String instanceName, long startTime) {
        String id = IDCreator.createId(requestInfo, collectDataName, instanceName);
        return TimelineRedisCalculator.calcAvg(redisTemplate, id, startTime, startTime + 60000 - 1);
    }

    protected void clearTimelineValue(RequestInfo requestInfo, String collectDataName, int hours) {
        String id = IDCreator.createId(requestInfo, collectDataName);
        TimelineRedisCalculator.clearPastTimelineValue(redisTemplate, id, System.currentTimeMillis() - hours * HOURLY);
    }

    protected void clearTimelineValue(RequestInfo requestInfo, String collectDataName, String instanceName, int hours) {
        String id = IDCreator.createId(requestInfo, collectDataName, instanceName);
        TimelineRedisCalculator.clearPastTimelineValue(redisTemplate, id, System.currentTimeMillis() - hours * HOURLY);
    }

    protected Date getHourlyDate(long time) {
        return new Date(time - time % HOURLY);
    }

    protected String getHourlyDateText(long time) {
        return simpleDateFormat.format(getHourlyDate(time));
    }

    protected int getMinuteValue(long time) {
        Long minute = (time % HOURLY) / MINUTE;
        return minute.intValue();
    }

}
