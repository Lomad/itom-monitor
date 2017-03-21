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

    public RedisTimeCalculator(String key, RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.key = key;
    }

    public void addValue(long time, double value) {
        String valueKey = time + "," + value;
        this.redisTemplate.opsForZSet().add(key, valueKey, time);
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

//        for (double value : values) {
//            sumValue = sumValue.add(new BigDecimal(String.valueOf(value)));
//            num++;
//        }

        return sumValue.divide(new BigDecimal(num), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    public void clearPastTimeValues(long pastTime) {
        this.redisTemplate.opsForZSet().removeRangeByScore(key, 0, pastTime);
    }

}
