package com.winning.itom.monitor.machine.realtime.analyzer;

import com.winning.itom.monitor.api.entity.CollectData;
import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.realtime.tasks.WinNetworkPerMinuteTask;
import com.winning.itom.task.core.ITaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nicholasyan on 17/3/21.
 */
public class WinNetworkAnalyzer extends AbstractAnalyzer {


    private final static Logger logger = LoggerFactory.getLogger(WinNetworkAnalyzer.class);

    @Override
    public String getCollectDataName() {
        return WinCounterConstants.NETWORK_RECEIVED_PERSEC;
    }

    @Override
    public void analyzeCollectData(RequestInfo requestInfo,
                                   CollectDataMap collectDataMap) {

        long current = System.currentTimeMillis();

        HashSet<String> networkInterfaces = new HashSet<>();
        networkInterfaces.addAll(this.analyzeCollectData(requestInfo, collectDataMap, WinCounterConstants.NETWORK_RECEIVED_PERSEC));
        networkInterfaces.addAll(this.analyzeCollectData(requestInfo, collectDataMap, WinCounterConstants.NETWORK_SENT_PERSEC));

        for (String networkInterface : networkInterfaces) {
            HashMap<String, String> args = new HashMap<>();
            args.put("networkInterface", networkInterface);
            this.addTimedTask(requestInfo, WinNetworkPerMinuteTask.TASK_NAME, networkInterface, 1, args);
        }

        logger.info("处理网络用时{}ms", System.currentTimeMillis() - current);
    }


    private Set<String> analyzeCollectData(RequestInfo requestInfo,
                                           CollectDataMap collectDataMap,
                                           String collectDataName) {

        CollectData data = collectDataMap.getCollectData(collectDataName);
        HashSet<String> networkInterfaces = new HashSet<>();

        Map<String, Object> valueMap = (Map<String, Object>) data.getValue();
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String networkInterface = entry.getKey();
            double value = ((BigDecimal) entry.getValue()).doubleValue();
            //获取实时数据的名称
            this.putRealtimeInstanceValue(requestInfo, collectDataName, networkInterface, value);
            this.putTimelineInstanceValue(requestInfo, collectDataName, networkInterface, value);
            networkInterfaces.add(networkInterface);
        }

        return networkInterfaces;
    }


    @Override
    protected void initTask(ITaskManager taskManager) {
        taskManager.registTask(new WinNetworkPerMinuteTask(this.redisTemplate, this.mongoTemplate));
    }

}
