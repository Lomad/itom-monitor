package com.winning.itom.monitor.machine.store.test;

import com.winning.itom.monitor.machine.store.cpu.IWinCpuStore;
import com.winning.itom.monitor.machine.store.cpu.entity.CpuValues;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.math.BigDecimal;

/**
 * Created by nicholasyan on 17/3/18.
 */
@ContextConfiguration(locations = {"classpath*:META-INF/spring/*-context.xml"})
public class WinCpuStoreUT extends
        AbstractJUnit4SpringContextTests {

    private static final Logger logger = LoggerFactory.getLogger(WinCpuStoreUT.class);

    @Autowired
    private IWinCpuStore winCpuStore;


    @Test
    public void testCurrentCpuValues() throws InterruptedException {

        while (true) {
            long current = System.currentTimeMillis();
            CpuValues cpuValues = this.winCpuStore.findCurrentCpuValues("ITOM.Client.Test", "192.168.0.115");
            if (cpuValues.size() == 0) {
                logger.info("未找到数据!");
                return;
            }
            BigDecimal bigDecimal = new BigDecimal(cpuValues.get("current"));
            bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            logger.info("{} ,{} ,{} ,{} ,共用时:{}ms",
                    bigDecimal.doubleValue(), cpuValues.get("last1m"),
                    cpuValues.get("last5m"), cpuValues.get("last15m"),
                    System.currentTimeMillis() - current);

//            logger.info("当前cpu:{},最近1分钟cpu:{},最近5分钟cpu:{},最近15分钟cpu:{},共用时:{}ms",
//                    cpuValues.get("current"), cpuValues.get("last1m"),
//                    cpuValues.get("last5m"), cpuValues.get("last15m"),
//                    System.currentTimeMillis() - current);
            Thread.sleep(6000);
        }
    }


    @Test
    public void testLast2HourCpuValuesInCache() throws InterruptedException {
        while (true) {
            long current = System.currentTimeMillis();
            long startTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000;
            CpuValues cpuValues =
                    this.winCpuStore.findLastCpuValuesInCache("ITOM.Client.Test", "192.168.0.115", startTime);

            logger.info("共{}条数据,共用时{}ms",
                    cpuValues.size(),
                    System.currentTimeMillis() - current);

            Thread.sleep(6000);
        }
    }


    @Test
    public void testLast2HourCpuValuesFromDb() throws InterruptedException {
        while (true) {
            long current = System.currentTimeMillis();
            long startTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000;
            CpuValues cpuValues =
                    this.winCpuStore.findMinuteCpuValues("ITOM.Client.Test", "192.168.0.115", startTime, current);

            logger.info("共{}条数据,共用时{}ms",
                    cpuValues.size(),
                    System.currentTimeMillis() - current);

            Thread.sleep(6000);
        }
    }


}
