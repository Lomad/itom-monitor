package com.winning.itom.monitor.machine.realtime.tasks.entity;

import com.winning.itom.monitor.api.entity.RequestInfo;

import java.util.Map;

/**
 * Created by nicholasyan on 17/3/25.
 */
public class AnalyzerTimedTaskArgs {

    public final static String TASK_ARG_KEY = "tasks.entity.AnalyzerTimedTaskArgs";
    private RequestInfo requestInfo;
    private long time;
    private Map<String, String> args;

    public AnalyzerTimedTaskArgs() {

    }

    public AnalyzerTimedTaskArgs(Map<String, String> args) {
        this(null, -1, args);
    }

    public AnalyzerTimedTaskArgs(RequestInfo requestInfo, long currentTime, Map<String, String> args) {
        this.requestInfo = requestInfo;
        this.args = args;
        this.time = currentTime;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getArg(String arg) {
        if (this.args == null)
            return null;
        return this.args.get(arg);
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }
}
