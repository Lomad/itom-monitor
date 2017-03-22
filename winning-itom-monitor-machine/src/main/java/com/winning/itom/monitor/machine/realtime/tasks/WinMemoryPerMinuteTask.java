package com.winning.itom.monitor.machine.realtime.tasks;

import com.winning.itom.monitor.api.constants.RequestParamConstants;
import com.winning.itom.monitor.machine.realtime.analyzer.WinCounterConstants;
import com.winning.itom.monitor.machine.realtime.report.WinMemoryHourlyReport;
import com.winning.itom.monitor.machine.utils.IDCreator;
import com.winning.itom.task.core.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by nicholasyan on 17/3/17.
 */
public class WinMemoryPerMinuteTask implements ITask {

    public final static String TASK_NAME = "com.winning.itom.monitor.machine.realtime.tasks.WinMemoryPerMinuteTask";
    private final static String COLLECTION_NAME = "WinMemoryHourlyReport";

    private final static Logger logger = LoggerFactory.getLogger(WinMemoryPerMinuteTask.class);
    private final static long HOURLY = 60 * 60000;
    private final static long MINUTE = 60000;
    private final RedisTemplate redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public WinMemoryPerMinuteTask(RedisTemplate redisTemplate,
                                  MongoTemplate mongoTemplate) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }


    @Override
    public void doTask(Map<String, String> args) {

        long current = System.currentTimeMillis();
        Date currentMinute = new Date(Long.parseLong(args.get("time")));

        String clientId = args.get(RequestParamConstants.CLIENT_ID);
        String clientName = args.get(RequestParamConstants.CLIENT_NAME);
        String machineIP = args.get(RequestParamConstants.HOST_IP);
        Date hourlyDate = new Date(currentMinute.getTime() - currentMinute.getTime() % HOURLY);
        String hourlyDateText = this.simpleDateFormat.format(hourlyDate);

        Long minute = (currentMinute.getTime() % HOURLY) / MINUTE;
        //开始计算一分钟平均值
        Double memoryAvailableValue = this.getMinuteValue(clientId, machineIP, WinCounterConstants.MEMORY_AVAILABLE, currentMinute);

        Query query = new Query(
                Criteria.where("clientId").is(clientId)
                        .and("machineIP").is(machineIP)
                        .and("hourlyDateText").is(hourlyDateText));

        WinMemoryHourlyReport report =
                this.mongoTemplate.findOne(query, WinMemoryHourlyReport.class, COLLECTION_NAME);

        if (report == null) {
            report = new WinMemoryHourlyReport();
            report.setClientId(clientId);
            report.setClientName(clientName);
            report.setMachineIP(machineIP);
            report.setHourlyDateText(hourlyDateText);
            report.setHourlyDate(hourlyDate);
            report.getMemoryAvailable().put(minute.intValue(), memoryAvailableValue);
            this.mongoTemplate.insert(report, COLLECTION_NAME);
        } else {
            String memoryAvailableKey = "memoryAvailable." + minute.intValue();
            Update update = new Update()
                    .set(memoryAvailableKey, memoryAvailableValue);
            this.mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
        }

        logger.info("Memory 当前时间{},处理时间{}完毕,共耗时{}ms",
                simpleDateFormat.format(new Date()),
                simpleDateFormat.format(currentMinute), System.currentTimeMillis() - current);
    }


    private Double getMinuteValue(String clientId, String machineIP, String collectDataType, Date currentMinute) {
        String minuteKey = IDCreator.createId(clientId, machineIP, collectDataType, "minutes");
        Set<String> values = this.redisTemplate.opsForZSet().rangeByScore(minuteKey,
                currentMinute.getTime(), currentMinute.getTime());

        long expiredTime = new Date().getTime() - 24 * HOURLY;
        this.redisTemplate.opsForZSet().removeRangeByScore(minuteKey, 0, expiredTime);

        if (values.size() == 0)
            return null;

        String currentMinuteKey = currentMinute.getTime() + ",";
        Double value = Double.valueOf(values.iterator().next().replace(currentMinuteKey, ""));
        return value;
    }
}
