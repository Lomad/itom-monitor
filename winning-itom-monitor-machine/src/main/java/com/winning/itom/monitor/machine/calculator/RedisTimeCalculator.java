package com.winning.itom.monitor.machine.calculator;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Created by nicholasyan on 17/3/17.
 */
public class RedisTimeCalculator {

    private final RedisTemplate redisTemplate;
    private final String key;

    public RedisTimeCalculator(RedisTemplate redisTemplate, String key) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    public void addValue(long time, double value) {
        String valueKey = time + "," + value;
        this.redisTemplate.opsForZSet().add(key, valueKey, time);
    }

    public void addSecondValue(long currentTime, double value) {
        //精确到秒
        long currentSecond = currentTime - currentTime % 1000;
        String valueKey = currentSecond + "," + value;
        this.redisTemplate.opsForZSet().add(key, valueKey, currentSecond);
    }


    public double calcAvg(long startTime, long endTime) {
        Set<ZSetOperations.TypedTuple> values = this.redisTemplate.opsForZSet().rangeByScoreWithScores(key, startTime, endTime);
        BigDecimal sumValue = new BigDecimal(0);
        int num = 0;
        for (ZSetOperations.TypedTuple value : values) {
            String valueKey = value.getValue().toString();
            String time = value.getScore().longValue() + ",";
            sumValue = sumValue.add(new BigDecimal(valueKey.replace(time, "")));
            num++;
        }

        return sumValue.divide(new BigDecimal(num), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    public void clearPastTimeValues(long pastTime) {
        this.redisTemplate.opsForZSet().removeRangeByScore(key, 0, pastTime);
    }

}
