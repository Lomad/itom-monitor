package com.winning.itom.monitor.machine.utils;

import com.winning.itom.monitor.api.entity.RequestInfo;

/**
 * Created by nicholasyan on 17/3/21.
 */
public class IDCreator {

    public static String createId(RequestInfo requestInfo, String key) {
        return requestInfo.getClientId() + "." +
                requestInfo.getIpAddress() + "." + key;
    }

    public static String createId(RequestInfo requestInfo, String key1, String key2) {
        return requestInfo.getClientId() + "." +
                requestInfo.getIpAddress() + "." + key1 + "." + key2;
    }


    public static String createId(RequestInfo requestInfo, String key1, String key2, String key3) {
        return requestInfo.getClientId() + "." +
                requestInfo.getIpAddress() + "." + key1 + "." + key2 + "." + key3;
    }

    public static String createId(String key1, String key2) {
        return key1 + "." + key2;
    }


    public static String createId(String key1, String key2, String key3) {
        return key1 + "." + key2 + "." + key3;
    }

    public static String createId(String key1, String key2, String key3, String key4) {
        return key1 + "." + key2 + "." + key3 + "." + key4;
    }

    public static String createId(String key1, String key2, String key3, String key4, String key5) {
        return key1 + "." + key2 + "." + key3 + "." + key4 + "." + key5;
    }
}
