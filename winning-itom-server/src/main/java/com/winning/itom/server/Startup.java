package com.winning.itom.server;

import com.winning.transport.netty.NettyServer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by nicholasyan on 17/3/19.
 */
public class Startup {


    public static void main(String args[]) {

        ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");

        applicationContext.start();
        applicationContext.registerShutdownHook();

        NettyServer nettyServer = applicationContext.getBean(NettyServer.class);
        nettyServer.startAndWait();
    }


}
