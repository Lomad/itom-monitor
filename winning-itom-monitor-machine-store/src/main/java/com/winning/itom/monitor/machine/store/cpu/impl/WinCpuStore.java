package com.winning.itom.monitor.machine.store.cpu.impl;

import com.winning.itom.monitor.machine.store.cpu.IWinCpuStore;
import com.winning.itom.monitor.machine.store.cpu.entity.CpuValues;
import com.winning.itom.monitor.machine.store.cpu.report.WinProcessorTimeHourlyReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nicholasyan on 17/3/18.
 */
public class WinCpuStore implements IWinCpuStore {

    public static final String WIN_PROCESSOR_TIME_COLLECT_DATA_NAME = "system.windows.processor.processorTime";
    private final static long HOURLY = 60 * 60000;
    private final static long MINUTE = 60000;
    private final SimpleDateFormat hourMinuteDateFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    @Qualifier("itomRedisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    @Qualifier("itomMongoTemplate")
    private MongoTemplate mongoTemplate;

    @Override
    public CpuValues findCurrentCpuValues(String clientId, String ip) {
        String id = clientId + "." + ip + "." + WIN_PROCESSOR_TIME_COLLECT_DATA_NAME;
        String realtimeKey = id + ".realtime";
        Map<String, Double> values = this.redisTemplate.opsForHash().entries(realtimeKey);
        CpuValues cpuValues = new CpuValues();
        cpuValues.putAll(values);

        return cpuValues;
    }

    @Override
    public CpuValues findLastCpuValuesInCache(String clientId, String ip, long start) {
        String id = clientId + "." + ip + "." + WIN_PROCESSOR_TIME_COLLECT_DATA_NAME;
        String minuteKey = id + ".minutes";

        CpuValues cpuValues = new CpuValues();

        Set<ZSetOperations.TypedTuple> values =
                this.redisTemplate.opsForZSet().rangeByScoreWithScores(minuteKey, start, System.currentTimeMillis());

        for (ZSetOperations.TypedTuple value : values) {
            String valueKey = value.getValue().toString();
            String time = value.getScore().longValue() + ",";
            String hourMinute = hourMinuteDateFormat.format(value.getScore().longValue());
            double doubleValue = Double.valueOf(valueKey.replace(time, ""));
            cpuValues.put(hourMinute, doubleValue);
        }

        return cpuValues;
    }

    @Override
    public CpuValues findMinuteCpuValues(String clientId, String ip, long startHour, long endHour) {
        String startTimeText = this.simpleDateFormat.format(startHour - startHour % HOURLY);
        String endTimeText = this.simpleDateFormat.format(endHour - endHour % HOURLY);
        CpuValues cpuValues = new CpuValues();

        Query query = Query.query(
                Criteria.where("clientId").is(clientId)
                        .and("machineIP").is(ip)
                        .and("hourlyDateText").gte(startTimeText).lte(endTimeText));

        String collectionName = "WinProcessorTimeHourlyReport";
        List<WinProcessorTimeHourlyReport> reports =
                this.mongoTemplate.find(query, WinProcessorTimeHourlyReport.class, collectionName);

        if (reports.size() == 0)
            return cpuValues;

        for (WinProcessorTimeHourlyReport report : reports) {
            long hourTime = report.getHourlyDate().getTime();
            for (Integer minute : report.getValues().keySet()) {
                String time = this.hourMinuteDateFormat.format(hourTime + minute * MINUTE);
                double doubleValue = report.getValues().get(minute);
                cpuValues.put(time, doubleValue);
            }
        }

        return cpuValues;
    }
}
