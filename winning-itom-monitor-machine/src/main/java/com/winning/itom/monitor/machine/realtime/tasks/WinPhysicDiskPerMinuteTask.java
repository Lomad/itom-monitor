package com.winning.itom.monitor.machine.realtime.tasks;

import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.realtime.analyzer.WinCounterConstants;
import com.winning.itom.monitor.machine.realtime.report.WinPhysicDiskHourlyReport;
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
public class WinPhysicDiskPerMinuteTask extends AnalyzerTimedTask {

    public final static String TASK_NAME = "com.winning.itom.monitor.machine.realtime.tasks.WinPhysicDiskPerMinuteTask";
    private final static String COLLECTION_NAME = "WinPhysicDiskHourlyReport";

    private final static Logger logger = LoggerFactory.getLogger(WinPhysicDiskPerMinuteTask.class);


    public WinPhysicDiskPerMinuteTask(RedisTemplate redisTemplate,
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
        String physicDisk = args.getArg("physicDisk");

        Double readSecValue = this.calcTimelineMinuteAvg(requestInfo,
                WinCounterConstants.PHYSIC_DISK_READS_PERSEC, physicDisk, start);
        Double writeSecValue = this.calcTimelineMinuteAvg(requestInfo,
                WinCounterConstants.PHYSIC_DISK_WRITES_PERSEC, physicDisk, start);
        Double transferSecValue = this.calcTimelineMinuteAvg(requestInfo,
                WinCounterConstants.PHYSIC_DISK_TRANSFERS_PERSEC, physicDisk, start);

        this.clearTimelineValue(requestInfo, WinCounterConstants.PHYSIC_DISK_READS_PERSEC, physicDisk, 24);
        this.clearTimelineValue(requestInfo, WinCounterConstants.PHYSIC_DISK_WRITES_PERSEC, physicDisk, 24);
        this.clearTimelineValue(requestInfo, WinCounterConstants.PHYSIC_DISK_TRANSFERS_PERSEC, physicDisk, 24);

        Query query = new Query(
                Criteria.where("clientId").is(clientId)
                        .and("machineIP").is(machineIP)
                        .and("hourlyDateText").is(hourlyDateText)
                        .and("physicDisk").is(physicDisk));

        WinPhysicDiskHourlyReport report =
                this.mongoTemplate.findOne(query, WinPhysicDiskHourlyReport.class, COLLECTION_NAME);

        if (report == null) {
            report = new WinPhysicDiskHourlyReport();
            report.setClientId(clientId);
            report.setClientName(clientName);
            report.setMachineIP(machineIP);
            report.setHourlyDateText(hourlyDateText);
            report.setHourlyDate(hourlyDate);
            report.setPhysicDisk(physicDisk);
            report.getReadSecBytes().put(minute, readSecValue);
            report.getWriteSecBytes().put(minute, writeSecValue);
            report.getTransferSec().put(minute, transferSecValue);
            this.mongoTemplate.insert(report, COLLECTION_NAME);
        } else {
            String readSecBytesKey = "readSecBytes." + minute;
            String writeSecBytesKey = "writeSecBytes." + minute;
            String transferSecKey = "transferSec." + minute;
            Update update = new Update()
                    .set(readSecBytesKey, readSecValue)
                    .set(writeSecBytesKey, writeSecValue)
                    .set(transferSecKey, transferSecValue);
            this.mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
        }

        logger.info("PhysicDisk 当前时间{},处理时间{}完毕,共耗时{}ms",
                simpleDateFormat.format(new Date()),
                simpleDateFormat.format(start), System.currentTimeMillis() - current);
    }

}
