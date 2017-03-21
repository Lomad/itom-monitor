package com.winning.itom.task.redis;

import com.alibaba.fastjson.JSON;
import com.winning.itom.task.core.ITaskManager;
import com.winning.itom.task.core.entity.TimedTask;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.Set;

/**
 * Created by nicholasyan on 17/3/17.
 */
public class RedisTimedTaskJob implements Job {

    private final static Logger logger = LoggerFactory.getLogger(RedisTimedTaskJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        RedisTemplate redisTemplate = (RedisTemplate) jobExecutionContext.getJobDetail().getJobDataMap().get("redisTemplate");
        ITaskManager taskManager = (ITaskManager) jobExecutionContext.getJobDetail().getJobDataMap().get("taskManager");

        String redisZSetKey = jobExecutionContext.getJobDetail().getJobDataMap().getString("redisZSetKey");
        String redisLockKey = jobExecutionContext.getJobDetail().getJobDataMap().getString("redisLockKey");
        String redisTaskKey = jobExecutionContext.getJobDetail().getJobDataMap().getString("redisTaskKey");

        this.scanTasks(redisTemplate, taskManager, redisZSetKey, redisLockKey, redisTaskKey);
    }


    private void scanTasks(RedisTemplate redisTemplate, ITaskManager taskManager,
                           String redisZSetKey, String redisLockKey, String redisTaskKey) {

        long current = System.currentTimeMillis();

        Set<String> taskSet = redisTemplate.opsForZSet().rangeByScore(redisZSetKey, 0, new Date().getTime());
        for (String taskId : taskSet) {
            String lockKey = redisLockKey + "." + taskId;
            String valueKey = redisTaskKey + "." + taskId;

            try {
                this.doTask(redisTemplate, taskManager, lockKey, valueKey);
            } catch (Exception e) {
                //发生错误,记录日志,或进行重试
                logger.error("执行任务时发生错误", e);
            } finally {
                redisTemplate.delete(valueKey);
                redisTemplate.delete(lockKey);
                redisTemplate.opsForZSet().remove(redisZSetKey, taskId);
            }

            logger.debug("当前剩余{}作业未执行", redisTemplate.opsForZSet().size(redisZSetKey));
        }

        logger.debug("操作用时:{}", System.currentTimeMillis() - current);
    }

    private void doTask(RedisTemplate redisTemplate, ITaskManager taskManager,
                        String lockKey, String valueKey) {
        long expired = System.currentTimeMillis() + 60000 + 1;
        if (!redisTemplate.opsForValue().setIfAbsent(lockKey, expired)) {
            long expiredTime = 0;
            try {
                expiredTime = (long) redisTemplate.opsForValue().get(lockKey);
            } catch (Exception e) {

            }
            if (System.currentTimeMillis() > expiredTime) {
                redisTemplate.opsForValue().set(lockKey, expired);
            }
        }

        String json = (String) redisTemplate.opsForValue().get(valueKey);
        if (json != null) {
            TimedTask timedTask = JSON.parseObject(json, TimedTask.class);
            taskManager.doTask(timedTask.getTodoTaskName(), timedTask.getTaskArgs());
        }
    }

}
