package com.winning.itom.monitor.machine.calculator;

import com.winning.itom.monitor.machine.utils.IDCreator;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by nicholasyan on 17/3/23.
 */
public class MinuteRedisCalculator {

    protected static final long MINUTE = 60000;

    public static void addMinuteValue(
            RedisTemplate redisTemplate,
            String id, long timestamp, Double value) {

        Long currentMinute = timestamp - timestamp % MINUTE;
        String sumkey = IDCreator.createId(id, "minute", "calculator", currentMinute.toString(), "sum");
        String numkey = IDCreator.createId(id, "minute", "calculator", currentMinute.toString(), "num");

        redisTemplate.opsForValue().increment(sumkey, value);
        redisTemplate.opsForValue().increment(numkey, 1);
    }


}
