package com.winning.itom.task.redis;

import com.alibaba.fastjson.JSON;
import com.winning.itom.task.core.ITaskManager;
import com.winning.itom.task.core.ITimedTaskFactory;
import com.winning.itom.task.core.entity.TimedTask;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by nicholasyan on 17/3/17.
 */
public class RedisTimedTaskFactory implements ITimedTaskFactory {

    private final String redisZSetKey;
    private final String redisLockKey;
    private final String redisTaskKey;
    private final Scheduler scheduler;
    private final JobDetail jobDetail;
    private final Trigger trigger;
    private final RedisTemplate redisTemplate;
    private final String name;
    private final ITaskManager taskManager;


    public RedisTimedTaskFactory(String name, RedisTemplate redisTemplate, ITaskManager taskManager)
            throws SchedulerException {
        this.name = name;
        this.redisTemplate = redisTemplate;
        this.taskManager = taskManager;

        this.redisZSetKey = "com.winning.itom.task.redis.RedisTimedTask.ZSet." + name;
        this.redisLockKey = "com.winning.itom.task.redis.RedisTimedTask.Lock." + name;
        this.redisTaskKey = "com.winning.itom.task.redis.RedisTimedTask.Task." + name;

        this.scheduler = new StdSchedulerFactory().getScheduler();
        this.jobDetail = newJob(RedisTimedTaskJob.class).build();
        this.jobDetail.getJobDataMap().put("redisTemplate", redisTemplate);
        this.jobDetail.getJobDataMap().put("redisZSetKey", redisZSetKey);
        this.jobDetail.getJobDataMap().put("redisLockKey", redisLockKey);
        this.jobDetail.getJobDataMap().put("redisTaskKey", redisTaskKey);
        this.jobDetail.getJobDataMap().put("taskManager", this.taskManager);

        this.trigger = newTrigger()
                .withSchedule(simpleSchedule().withIntervalInSeconds(1).repeatForever())
                .startNow().build();

        // 注册并进行调度
        this.scheduler.scheduleJob(this.jobDetail, trigger);

        // 启动调度器
        this.scheduler.start();
    }


    @Override
    public void addTimedTask(TimedTask timedTask) {
        String taskId = timedTask.getTaskId();
        String lockKey = this.redisLockKey + "." + taskId;
        String valueKey = this.redisTaskKey + "." + taskId;

        this.redisTemplate.opsForZSet().add(this.redisZSetKey, timedTask.getTaskId(), timedTask.getRunTime());
        this.redisTemplate.opsForValue().set(lockKey, "", 3, TimeUnit.MINUTES);
        String json = JSON.toJSONString(timedTask);
        this.redisTemplate.opsForValue().set(valueKey, json, 7, TimeUnit.DAYS);
    }

    @Override
    public ITaskManager getTaskManager() {
        return this.taskManager;
    }
}
