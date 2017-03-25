package com.winning.itom.monitor.machine.realtime.analyzer;

import com.winning.itom.monitor.api.entity.CollectData;
import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.realtime.tasks.WinPhysicDiskPerMinuteTask;
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
public class WinPhysicDiskAnalyzer extends AbstractAnalyzer {


    private final static Logger logger = LoggerFactory.getLogger(WinPhysicDiskAnalyzer.class);

    @Override
    public String getCollectDataName() {
        return WinCounterConstants.PHYSIC_DISK_READS_PERSEC;
    }

    @Override
    public void analyzeCollectData(RequestInfo requestInfo,
                                   CollectDataMap collectDataMap) {

        long current = System.currentTimeMillis();

        HashSet<String> physicDisks = new HashSet<>();
        physicDisks.addAll(this.analyzeCollectData(requestInfo, collectDataMap, WinCounterConstants.PHYSIC_DISK_READS_PERSEC));
        physicDisks.addAll(this.analyzeCollectData(requestInfo, collectDataMap, WinCounterConstants.PHYSIC_DISK_WRITES_PERSEC));
        physicDisks.addAll(this.analyzeCollectData(requestInfo, collectDataMap, WinCounterConstants.PHYSIC_DISK_TRANSFERS_PERSEC));

        //0 C: 1 D: E: F: 2:_Total
        for (String physicDisk : physicDisks) {
            HashMap<String, String> args = new HashMap<>();
            args.put("physicDisk", physicDisk);
            this.addTimedTask(requestInfo, WinPhysicDiskPerMinuteTask.TASK_NAME, physicDisk, 1, args);
        }

        logger.info("处理物理磁盘用时{}ms", System.currentTimeMillis() - current);
    }


    private Set<String> analyzeCollectData(RequestInfo requestInfo,
                                           CollectDataMap collectDataMap,
                                           String collectDataName) {

        CollectData data = collectDataMap.getCollectData(collectDataName);
        HashSet<String> physicDisks = new HashSet<>();

        Map<String, Object> valueMap = (Map<String, Object>) data.getValue();
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String physicDisk = entry.getKey();
            double value = ((BigDecimal) entry.getValue()).doubleValue();
            //获取实时数据的名称
            this.putRealtimeInstanceValue(requestInfo, collectDataName, physicDisk, value);
            this.putTimelineInstanceValue(requestInfo, collectDataName, physicDisk, value);
            physicDisks.add(physicDisk);
        }

        return physicDisks;
    }


    @Override
    protected void initTask(ITaskManager taskManager) {
        taskManager.registTask(new WinPhysicDiskPerMinuteTask(this.redisTemplate, this.mongoTemplate));
    }

}
