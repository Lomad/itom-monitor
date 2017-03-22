package com.winning.itom.task.core;

import java.util.Map;

/**
 * Created by nicholasyan on 17/3/17.
 */
public interface ITaskManager {

    void registTask(ITask task);

    void doTask(String taskName, Map<String, String> arguments);

}
