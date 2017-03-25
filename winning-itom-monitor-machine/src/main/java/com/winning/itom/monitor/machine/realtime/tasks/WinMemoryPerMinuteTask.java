package com.winning.itom.monitor.machine.realtime.tasks;

import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.realtime.analyzer.WinCounterConstants;
import com.winning.itom.monitor.machine.realtime.report.WinMemoryHourlyReport;
import com.winning.itom.monitor.machine.realtime.tasks.entity.AnalyzerTimedTaskArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;

/**
 * Created by nicholasyan on 17/3/17.
 */
public class WinMemoryPerMinuteTask extends AnalyzerTimedTask {

    public final static String TASK_NAME = "com.winning.itom.monitor.machine.realtime.tasks.WinMemoryPerMinuteTask";
    private final static String COLLECTION_NAME = "WinMemoryHourlyReport";

    private final static Logger logger = LoggerFactory.getLogger(WinMemoryPerMinuteTask.class);

    public WinMemoryPerMinuteTask(RedisTemplate redisTemplate,
                                  MongoTemplate mongoTemplate) {
        super(redisTemplate, mongoTemplate);
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }


    @Override
    protected void doTask(AnalyzerTimedTaskArgs args) {
        long current = System.currentTimeMillis();
        Date hourlyDate = this.getHourlyDate(args.getTime());
        String hourlyDateText = this.getHourlyDateText(args.getTime());
        int minute = this.getMinuteValue(args.getTime());
        long start = args.getTime();
        RequestInfo requestInfo = args.getRequestInfo();

        Double memoryAvailableValue = this.calcTimelineMinuteAvg(requestInfo, WinCounterConstants.MEMORY_AVAILABLE, start);
        Double memoryPagesValue = this.calcTimelineMinuteAvg(requestInfo, WinCounterConstants.MEMORY_PAGES_PERSEC, start);

        this.clearTimelineValue(requestInfo, WinCounterConstants.MEMORY_AVAILABLE, 24);
        this.clearTimelineValue(requestInfo, WinCounterConstants.MEMORY_PAGES_PERSEC, 24);

        Query query = new Query(
                Criteria.where("clientId").is(args.getRequestInfo().getClientId())
                        .and("machineIP").is(args.getRequestInfo().getIpAddress())
                        .and("hourlyDateText").is(hourlyDateText));

        WinMemoryHourlyReport report =
                this.mongoTemplate.findOne(query, WinMemoryHourlyReport.class, COLLECTION_NAME);

        if (report == null) {
            report = new WinMemoryHourlyReport();
            report.setClientId(args.getRequestInfo().getClientId());
            report.setClientName(args.getRequestInfo().getClientName());
            report.setMachineIP(args.getRequestInfo().getIpAddress());
            report.setHourlyDateText(hourlyDateText);
            report.setHourlyDate(hourlyDate);
            report.getMemoryAvailable().put(minute, memoryAvailableValue);
            report.getMemoryPages().put(minute, memoryPagesValue);
            this.mongoTemplate.insert(report, COLLECTION_NAME);
        } else {
            String memoryAvailableKey = "memoryAvailable." + minute;
            String memoryPageKey = "memoryPages." + minute;
            Update update = new Update()
                    .set(memoryAvailableKey, memoryAvailableValue)
                    .set(memoryPageKey, memoryPagesValue);
            this.mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
        }

        logger.info("Memory 当前时间{},处理时间{}完毕,共耗时{}ms",
                simpleDateFormat.format(new Date()),
                simpleDateFormat.format(start), System.currentTimeMillis() - current);
    }

}
