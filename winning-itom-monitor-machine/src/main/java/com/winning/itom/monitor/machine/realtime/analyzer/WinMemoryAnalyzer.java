package com.winning.itom.monitor.machine.realtime.analyzer;

import com.winning.itom.monitor.api.ICollectDataAnalyzer;
import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.task.core.ITimedTaskFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by nicholasyan on 17/3/21.
 */
public class WinMemoryAnalyzer implements ICollectDataAnalyzer {

    public static final String COLLECT_DATA_NAME = "system.windows.memory.available";

    @Override
    public String getCollectDataName() {
        return COLLECT_DATA_NAME;
    }

    @Override
    public void analyzeCollectData(RequestInfo requestInfo,
                                   CollectDataMap collectDataMap) {


    }
}
