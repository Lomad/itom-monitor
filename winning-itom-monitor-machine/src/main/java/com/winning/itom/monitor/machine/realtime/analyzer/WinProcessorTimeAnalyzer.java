package com.winning.itom.monitor.machine.realtime.analyzer;

import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.realtime.tasks.WinProcessorTimePerMinuteTask;
import com.winning.itom.task.core.ITaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nicholasyan on 17/3/15.
 */
public class WinProcessorTimeAnalyzer extends AbstractAnalyzer {

    private final static Logger logger = LoggerFactory.getLogger(WinProcessorTimeAnalyzer.class);

    @Override
    public String getCollectDataName() {
        return WinCounterConstants.PROCESSOR_TIME;
    }

    @Override
    public void analyzeCollectData(RequestInfo requestInfo, CollectDataMap collectDataMap) {

        long current = System.currentTimeMillis();

        this.analyzeCollectData(requestInfo, collectDataMap, WinCounterConstants.PROCESSOR_TIME, new int[]{1, 5, 15});
        this.analyzeCollectData(requestInfo, collectDataMap, WinCounterConstants.USER_TIME, new int[]{1, 5, 15});

        this.addTimedTask(requestInfo, WinProcessorTimePerMinuteTask.TASK_NAME, 1, null);

        logger.info("处理CPU用时{}ms", System.currentTimeMillis() - current);
    }


    protected void analyzeCollectData(RequestInfo requestInfo, CollectDataMap collectDataMap,
                                      String collectDataName, int[] lastMinutes) {

        Double processTime = this.getDoubleValue(collectDataMap, collectDataName);
        this.putRealtimeInstanceValue(requestInfo, collectDataName, "current", processTime);
        this.putTimelineValue(requestInfo, collectDataMap, collectDataName);

        if (lastMinutes != null) {
            for (int minutes : lastMinutes) {
                Double value = this.calcLastMinuteAvg(redisTemplate, requestInfo, collectDataName, minutes);
                String key = "last" + minutes + "m";
                this.putRealtimeInstanceValue(requestInfo, collectDataName, key, value);
            }
        }
    }


    @Override
    protected void initTask(ITaskManager taskManager) {
        taskManager.registTask(new WinProcessorTimePerMinuteTask(this.redisTemplate, this.mongoTemplate));
    }

}
