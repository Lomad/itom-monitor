package com.winning.itom.monitor.machine.calculator;

import com.winning.itom.monitor.machine.utils.IDCreator;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Created by nicholasyan on 17/3/23.
 */
public class RealtimeRedisCalculator {

//    public static void setCurrentValue(
//            RedisTemplate redisTemplate,
//            String id, Double value, int expireSecond) {
//        setValue(redisTemplate, id, "current", value, expireSecond);
//    }
//
//    public static void setValue(
//            RedisTemplate redisTemplate,
//            String id, String time, Double value, int expireSecond) {
//
//        String realtimeKey = IDCreator.createId(id, "realtime", time);
//        redisTemplate.opsForValue().set(realtimeKey, value);
//        redisTemplate.expire(realtimeKey, expireSecond, TimeUnit.SECONDS);
//    }

    public static void putRealtimeValue(RedisTemplate redisTemplate, String type, String name, Double value) {
        putRealtimeValue(redisTemplate, type, name, value, 10);
    }

    public static void putRealtimeValue(RedisTemplate redisTemplate, String type, String name, Double value, int expireMinutes) {
        String realtimeReceiveKey = IDCreator.createId(type, "realtime");
        redisTemplate.opsForHash().put(realtimeReceiveKey, name, value);
        redisTemplate.expire(realtimeReceiveKey, expireMinutes, TimeUnit.MINUTES);
    }

    public static void putRealtimeValue(RedisTemplate redisTemplate, String id, Double value) {
        putRealtimeValue(redisTemplate, id, value, 10);
    }

    public static void putRealtimeValue(RedisTemplate redisTemplate, String id, Double value, int expireMinutes) {
        String realtimeReceiveKey = IDCreator.createId(id, "realtime");
        redisTemplate.opsForValue().set(realtimeReceiveKey, value, expireMinutes, TimeUnit.MINUTES);
    }

}
