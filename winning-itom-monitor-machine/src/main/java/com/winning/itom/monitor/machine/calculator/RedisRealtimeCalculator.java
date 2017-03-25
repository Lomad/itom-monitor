package com.winning.itom.monitor.machine.calculator;

import com.winning.itom.monitor.machine.utils.IDCreator;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by nicholasyan on 17/3/23.
 */
public class RedisRealtimeCalculator {

    public static void putRealtimeValue(RedisTemplate redisTemplate, String type, String name, Double value) {
        String realtimeReceiveKey = IDCreator.createId(type, "realtime");
        redisTemplate.opsForHash().put(realtimeReceiveKey, name, value);
    }

    public static void putRealtimeValue(RedisTemplate redisTemplate, String id, Double value) {
        String realtimeReceiveKey = IDCreator.createId(id, "realtime");
        redisTemplate.opsForValue().set(realtimeReceiveKey, value);
    }


}
