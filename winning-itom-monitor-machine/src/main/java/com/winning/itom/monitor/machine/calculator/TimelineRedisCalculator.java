package com.winning.itom.monitor.machine.calculator;

import com.winning.itom.monitor.machine.utils.IDCreator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Created by nicholasyan on 17/3/23.
 */
public class TimelineRedisCalculator {

    protected static final long MINUTE = 60000;
    private static final String KEY_EXT = "timeline";

    public static void addTimelineValue(
            RedisTemplate redisTemplate,
            String id, long timestamp, Double value) {

        String valueKey = timestamp + "," + value;
        String key = IDCreator.createId(id, KEY_EXT);
        redisTemplate.opsForZSet().add(key, valueKey, timestamp);
    }


    public static double calcAvg(RedisTemplate redisTemplate,
                                 String id, long startTime, long endTime) {

        String key = IDCreator.createId(id, KEY_EXT);

        Set<ZSetOperations.TypedTuple> values = redisTemplate.opsForZSet().rangeByScoreWithScores(key,
                startTime, endTime);
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


    public static void clearPastTimelineValue(
            RedisTemplate redisTemplate,
            String id, long pastTime) {

        String key = IDCreator.createId(id, KEY_EXT);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, pastTime);
    }

}
