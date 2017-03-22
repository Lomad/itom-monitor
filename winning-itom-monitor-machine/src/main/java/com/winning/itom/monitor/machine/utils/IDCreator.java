package com.winning.itom.monitor.machine.utils;

import com.winning.itom.monitor.api.entity.RequestInfo;

/**
 * Created by nicholasyan on 17/3/21.
 */
public class IDCreator {

    public static String createId(RequestInfo requestInfo, String name) {
        return requestInfo.getClientId() + "." +
                requestInfo.getIpAddress() + "." + name;
    }

    public static String createId(RequestInfo requestInfo, String name, String time) {
        return requestInfo.getClientId() + "." +
                requestInfo.getIpAddress() + "." + name + "." + time;
    }


    public static String createId(String clientId, String ip, String name, String time) {
        return clientId + "." + ip + "." + name + "." + time;
    }

}
