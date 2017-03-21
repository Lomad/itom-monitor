package com.winning.itom.monitor.machine.test;

import com.winning.transport.netty.NettyServer;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by nicholasyan on 17/3/15.
 */
public class StartupUT {


    @Test
    public void testStartup() {

        ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");

        applicationContext.start();
        applicationContext.registerShutdownHook();

        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.startAndWait();

    }

    @Test
    public void testClearRedis() {

        ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");


        RedisTemplate redisTemplate = (RedisTemplate) applicationContext.getBean(RedisTemplate.class);
        redisTemplate.delete("null.null.system.windows.processor.processorTime.1m.MACalcList");
        redisTemplate.delete("null.null.system.windows.processor.processorTime.5m.MACalcList");
        redisTemplate.delete("null.null.system.windows.processor.processorTime.15m.MACalcList");
        redisTemplate.delete("null.null.system.windows.processor.processorTime.1m.MACalcSum");
        redisTemplate.delete("null.null.system.windows.processor.processorTime.5m.MACalcSum");
        redisTemplate.delete("null.null.system.windows.processor.processorTime.15m.MACalcSum");

    }

}
