package com.winning.itom.monitor.machine.realtime.tasks;

import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.realtime.analyzer.WinCounterConstants;
import com.winning.itom.monitor.machine.realtime.report.WinProcessorTimeHourlyReport;
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
public class WinProcessorTimePerMinuteTask extends AnalyzerTimedTask {

    public final static String TASK_NAME = "com.winning.itom.monitor.machine.realtime.tasks.WinProcessorTimePerMinuteTask";
    private final static String COLLECTION_NAME = "WinProcessorHourlyReport";


    private final static Logger logger = LoggerFactory.getLogger(WinProcessorTimePerMinuteTask.class);

    public WinProcessorTimePerMinuteTask(RedisTemplate redisTemplate,
                                         MongoTemplate mongoTemplate) {
        super(redisTemplate, mongoTemplate);
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }


    @Override
    public void doTask(AnalyzerTimedTaskArgs args) {

        long current = System.currentTimeMillis();

        Date hourlyDate = this.getHourlyDate(args.getTime());
        String hourlyDateText = this.getHourlyDateText(args.getTime());
        int minute = this.getMinuteValue(args.getTime());
        long start = args.getTime();
        RequestInfo requestInfo = args.getRequestInfo();
        String clientId = requestInfo.getClientId();
        String clientName = requestInfo.getClientName();
        String machineIP = requestInfo.getIpAddress();

        //开始计算一分钟平均值
        Double processTimeValue = this.calcTimelineMinuteAvg(requestInfo, WinCounterConstants.PROCESSOR_TIME, start);
        Double userTimeValue = this.calcTimelineMinuteAvg(requestInfo, WinCounterConstants.USER_TIME, start);
        this.clearTimelineValue(requestInfo, WinCounterConstants.PROCESSOR_TIME, 24);
        this.clearTimelineValue(requestInfo, WinCounterConstants.USER_TIME, 24);

        Query query = new Query(
                Criteria.where("clientId").is(clientId)
                        .and("machineIP").is(machineIP)
                        .and("hourlyDateText").is(hourlyDateText));

        WinProcessorTimeHourlyReport report =
                this.mongoTemplate.findOne(query, WinProcessorTimeHourlyReport.class, COLLECTION_NAME);

        if (report == null) {
            report = new WinProcessorTimeHourlyReport();
            report.setClientId(clientId);
            report.setClientName(clientName);
            report.setMachineIP(machineIP);
            report.setHourlyDateText(hourlyDateText);
            report.setHourlyDate(hourlyDate);
            report.getProcessTime().put(minute, processTimeValue);
            report.getUserTime().put(minute, userTimeValue);
            this.mongoTemplate.insert(report, COLLECTION_NAME);
        } else {
            String processTimeKey = "processTime." + minute;
            String userTimeKey = "userTime." + minute;
            Update update = new Update()
                    .set(processTimeKey, processTimeValue)
                    .set(userTimeKey, userTimeValue);
            this.mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
        }

        logger.info("CPU 当前时间{},处理时间{}完毕,共耗时{}ms",
                simpleDateFormat.format(new Date()),
                simpleDateFormat.format(start), System.currentTimeMillis() - current);
    }

}
