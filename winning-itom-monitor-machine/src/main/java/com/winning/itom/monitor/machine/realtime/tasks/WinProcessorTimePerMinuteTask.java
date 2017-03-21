package com.winning.itom.monitor.machine.realtime.tasks;

import com.winning.itom.monitor.machine.realtime.report.WinProcessorTimeHourlyReport;
import com.winning.itom.task.core.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
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
public class WinProcessorTimePerMinuteTask implements ITask {

    public final static String TASK_NAME = "com.winning.itom.monitor.machine.realtime.tasks.WinProcessorTimePerMinuteTask";
    private final static Logger logger = LoggerFactory.getLogger(WinProcessorTimePerMinuteTask.class);
    private final static long HOURLY = 60 * 60000;
    private final static long MINUTE = 60000;
    private final RedisTemplate redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public WinProcessorTimePerMinuteTask(RedisTemplate redisTemplate,
                                         MongoDbFactory mongoDbFactory) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = new MongoTemplate(mongoDbFactory);
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }


    @Override
    public void doTask(Map<String, String> args) {
        logger.info("当前时间{},正在处理时间{}",
                simpleDateFormat.format(new Date()),
                simpleDateFormat.format(Long.parseLong(args.get("time"))));

        String id = args.get("id");
        Date currentMinute = new Date(Long.parseLong(args.get("time")));
        Date hourlyDate = new Date(currentMinute.getTime() - currentMinute.getTime() % HOURLY);
        Long minute = (currentMinute.getTime() % HOURLY) / MINUTE;

        String minuteKey = id + ".minutes";
        Set<String> values = this.redisTemplate.opsForZSet().rangeByScore(minuteKey,
                currentMinute.getTime(), currentMinute.getTime());

//        long twoHourvsAgo = new Date().getTime() - 2 * HOURLY;
        long expiredTime = new Date().getTime() - 24 * HOURLY;
        this.redisTemplate.opsForZSet().removeRangeByScore(minuteKey, 0, expiredTime);

        if (values.size() == 0)
            return;

        String currentMinuteKey = currentMinute.getTime() + ",";
        Double value = Double.valueOf(values.iterator().next().replace(currentMinuteKey, ""));
        String clientId = args.get("clientId");
        String clientName = args.get("clientName");
        String machineIP = args.get("machineIP");
        String hourlyDateText = this.simpleDateFormat.format(hourlyDate);

        Query query = new Query(
                Criteria.where("clientId").is(clientId)
                        .and("clientName").is(clientName)
                        .and("machineIP").is(machineIP)
                        .and("hourlyDateText").is(hourlyDateText));


        String collectionName = "";
        String collectDataName = args.get("collectDataName");
        if ("WinProcessorTimeHourlyReport".equals(collectDataName)) {
            collectionName = "WinProcessorTimeHourlyReport";
        } else if ("WinProcessorUserTimeHourlyReport".equals(collectDataName)) {
            collectionName = "WinProcessorUserTimeHourlyReport";
        }

        WinProcessorTimeHourlyReport report =
                this.mongoTemplate.findOne(query, WinProcessorTimeHourlyReport.class, collectionName);

        if (report == null) {
            report = new WinProcessorTimeHourlyReport();
            report.setClientId(clientId);
            report.setClientName(clientName);
            report.setMachineIP(machineIP);
            report.setHourlyDateText(hourlyDateText);
            report.setHourlyDate(hourlyDate);
            report.getValues().put(minute.intValue(), value);
            this.mongoTemplate.insert(report, collectionName);
        } else {
            Update update = Update.update("values", report.getValues());
            report.getValues().put(minute.intValue(), value);
            this.mongoTemplate.updateFirst(query, update, collectionName);
        }


    }
}
