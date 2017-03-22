package com.winning.itom.task.core.impl;

import com.winning.itom.task.core.ITask;
import com.winning.itom.task.core.ITaskManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by nicholasyan on 17/3/17.
 */
public class TaskManager implements ITaskManager {

    private final HashMap<String, ITask> taskHashMap;

    public TaskManager() {
        this(null);
    }

    public TaskManager(Set<ITask> taskSet) {
        this.taskHashMap = new HashMap<>();
        if (taskSet != null) {
            for (ITask task : taskSet) {
                this.taskHashMap.put(task.getName(), task);
            }
        }
    }

    @Override
    public void registTask(ITask task) {
        this.taskHashMap.put(task.getName(), task);
    }

    @Override
    public void doTask(String taskName, Map<String, String> arguments) {
        if (!this.taskHashMap.containsKey(taskName))
            return;

        this.taskHashMap.get(taskName).doTask(arguments);
    }
}
