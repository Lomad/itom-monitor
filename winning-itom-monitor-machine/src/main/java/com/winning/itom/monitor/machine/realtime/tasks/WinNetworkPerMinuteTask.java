package com.winning.itom.monitor.machine.realtime.tasks;

import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.realtime.analyzer.WinCounterConstants;
import com.winning.itom.monitor.machine.realtime.report.WinNetworkHourlyReport;
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
public class WinNetworkPerMinuteTask extends AnalyzerTimedTask {

    public final static String TASK_NAME = "com.winning.itom.monitor.machine.realtime.tasks.WinNetworkPerMinuteTask";
    private final static String COLLECTION_NAME = "WinNetworkHourlyReport";

    private final static Logger logger = LoggerFactory.getLogger(WinNetworkPerMinuteTask.class);


    public WinNetworkPerMinuteTask(RedisTemplate redisTemplate,
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
        String clientId = requestInfo.getClientId();
        String clientName = requestInfo.getClientName();
        String machineIP = requestInfo.getIpAddress();
        String networkInterface = args.getArg("networkInterface");

        Double networkReceiveValue = this.calcTimelineMinuteAvg(requestInfo,
                WinCounterConstants.NETWORK_RECEIVED_PERSEC, networkInterface, start);
        Double networkSentValue = this.calcTimelineMinuteAvg(requestInfo,
                WinCounterConstants.NETWORK_SENT_PERSEC, networkInterface, start);

        this.clearTimelineValue(requestInfo, WinCounterConstants.NETWORK_RECEIVED_PERSEC, networkInterface, 24);
        this.clearTimelineValue(requestInfo, WinCounterConstants.NETWORK_SENT_PERSEC, networkInterface, 24);

        Query query = new Query(
                Criteria.where("clientId").is(clientId)
                        .and("machineIP").is(machineIP)
                        .and("hourlyDateText").is(hourlyDateText)
                        .and("networkInterface").is(networkInterface));

        WinNetworkHourlyReport report =
                this.mongoTemplate.findOne(query, WinNetworkHourlyReport.class, COLLECTION_NAME);

        if (report == null) {
            report = new WinNetworkHourlyReport();
            report.setClientId(clientId);
            report.setClientName(clientName);
            report.setMachineIP(machineIP);
            report.setHourlyDateText(hourlyDateText);
            report.setHourlyDate(hourlyDate);
            report.setNetworkInterface(networkInterface);
            report.getReceivedBytes().put(minute, networkReceiveValue);
            report.getSentBytes().put(minute, networkSentValue);
            this.mongoTemplate.insert(report, COLLECTION_NAME);
        } else {
            String receivedBytesKey = "receivedBytes." + minute;
            String sentBytesKey = "sentBytes." + minute;
            Update update = new Update()
                    .set(receivedBytesKey, networkReceiveValue)
                    .set(sentBytesKey, networkSentValue);
            this.mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
        }

        logger.info("Network 当前时间{},处理时间{}完毕,共耗时{}ms",
                simpleDateFormat.format(new Date()),
                simpleDateFormat.format(start), System.currentTimeMillis() - current);
    }

}
