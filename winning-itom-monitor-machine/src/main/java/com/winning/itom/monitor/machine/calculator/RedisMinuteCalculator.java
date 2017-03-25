package com.winning.itom.monitor.machine.calculator;

import com.winning.itom.monitor.machine.utils.IDCreator;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.Set;

/**
 * Created by nicholasyan on 17/3/23.
 */
public class RedisMinuteCalculator {

    private final static long HOURLY = 60 * 60000;
    private final static long MINUTE = 60000;

    private final RedisTemplate redisTemplate;
    private final String collectDataType;

    public RedisMinuteCalculator(RedisTemplate redisTemplate, String collectDataType) {
        this.redisTemplate = redisTemplate;
        this.collectDataType = collectDataType;
    }


    public void addMinuteValue(String id, String clientId, String machineIP, long currentTime) {
        String minuteCalcKey = IDCreator.createId(clientId, machineIP, id, "minutes.calc");


    }


    public Double getMinuteValue(String clientId, String machineIP, Date currentMinute) {
        String minuteKey = IDCreator.createId(clientId, machineIP, collectDataType, "minutes");
        Set<String> values = this.redisTemplate.opsForZSet().rangeByScore(minuteKey,
                currentMinute.getTime(), currentMinute.getTime());

        long expiredTime = new Date().getTime() - 24 * HOURLY;
        this.redisTemplate.opsForZSet().removeRangeByScore(minuteKey, 0, expiredTime);

        if (values.size() == 0)
            return null;

        String currentMinuteKey = currentMinute.getTime() + ",";
        Double value = Double.valueOf(values.iterator().next().replace(currentMinuteKey, ""));
        return value;
    }


}
