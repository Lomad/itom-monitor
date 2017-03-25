package com.winning.itom.monitor.machine.realtime.analyzer;

import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.realtime.tasks.WinMemoryPerMinuteTask;
import com.winning.itom.task.core.ITaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nicholasyan on 17/3/21.
 */
public class WinMemoryAnalyzer extends AbstractAnalyzer {


    private final static Logger logger = LoggerFactory.getLogger(WinMemoryAnalyzer.class);

    @Override
    public String getCollectDataName() {
        return WinCounterConstants.MEMORY_AVAILABLE;
    }

    @Override
    public void analyzeCollectData(RequestInfo requestInfo,
                                   CollectDataMap collectDataMap) {

        long current = System.currentTimeMillis();

        this.putRealtimeValue(requestInfo, collectDataMap, WinCounterConstants.MEMORY_AVAILABLE);
        this.putTimelineValue(requestInfo, collectDataMap, WinCounterConstants.MEMORY_AVAILABLE);

        this.putRealtimeValue(requestInfo, collectDataMap, WinCounterConstants.MEMORY_PAGES_PERSEC);
        this.putTimelineValue(requestInfo, collectDataMap, WinCounterConstants.MEMORY_PAGES_PERSEC);

        //一分钟后进行分析处理
        this.addTimedTask(requestInfo, WinMemoryPerMinuteTask.TASK_NAME, 1, null);

        logger.info("处理内存用时{}ms", System.currentTimeMillis() - current);
    }


    @Override
    protected void initTask(ITaskManager taskManager) {
        taskManager.registTask(new WinMemoryPerMinuteTask(this.redisTemplate, this.mongoTemplate));
    }

}
