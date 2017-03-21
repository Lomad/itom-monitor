package com.winning.itom.task.core.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by nicholasyan on 17/3/17.
 */
public class TimedTask {

    private String taskId;

    private long runTime;

    private String todoTaskName;

    private Map<String, String> taskArgs = new LinkedHashMap<>();

    private int retryTimes = 0;

    public TimedTask() {
        this.taskId = UUID.randomUUID().toString();
    }

    public TimedTask(String id) {
        this.taskId = id;
    }


    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
    }

    public String getTodoTaskName() {
        return todoTaskName;
    }

    public void setTodoTaskName(String todoTaskName) {
        this.todoTaskName = todoTaskName;
    }

    public Map<String, String> getTaskArgs() {
        return taskArgs;
    }

    public void setTaskArgs(Map<String, String> taskArgs) {
        this.taskArgs = taskArgs;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public void put(String key, String value) {
        this.taskArgs.put(key, value);
    }


}
