package com.winning.itom.task.core;

import java.util.Map;

/**
 * Created by nicholasyan on 17/3/17.
 */
public interface ITask {

    String getName();

    void doTask(Map<String, String> args);

}
