package com.winning.itom.monitor.machine.realtime.analyzer;

import com.winning.calc.api.IMACalculator;
import com.winning.calc.api.ISimpleCalculator;
import com.winning.calc.core.MemorySimpleCalculator;
import com.winning.calculator.redis.RedisMACalculator;
import com.winning.itom.monitor.api.ICollectDataAnalyzer;
import com.winning.itom.monitor.api.entity.CollectData;
import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.calculator.RedisTimeCalculator;
import com.winning.itom.monitor.machine.realtime.tasks.WinProcessorTimePerMinuteTask;
import com.winning.itom.task.core.ITimedTaskFactory;
import com.winning.itom.task.core.entity.TimedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by nicholasyan on 17/3/15.
 */
public class WinProcessorTimeAnalyzer implements ICollectDataAnalyzer {

    public static final String COLLECT_DATA_NAME = "system.windows.processor.processorTime";
    public static final String COLLECT_DATA_USER_TIME = "system.windows.processor.userTime";

    private final static Logger logger = LoggerFactory.getLogger(WinProcessorTimeAnalyzer.class);
    private final RedisTemplate redisTemplate;
    private final ITimedTaskFactory timedTaskFactory;
    private Map<String, IMACalculator> mapCalculator = new Hashtable<>();
    private Map<String, ISimpleCalculator> mapSimpleCalculator = new Hashtable<>();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

    private Map<String, RedisTimeCalculator> redisTimeCalculatorMap = new Hashtable<>();

    public WinProcessorTimeAnalyzer(RedisTemplate redisTemplate, ITimedTaskFactory timedTaskFactory) {
        this.redisTemplate = redisTemplate;
        this.timedTaskFactory = timedTaskFactory;
    }


    @Override
    public String getCollectDataName() {
        return COLLECT_DATA_NAME;
    }

    @Override
    public void analyzeCollectData(RequestInfo requestInfo, CollectDataMap collectDataMap) {
        long current = new Date().getTime();

        this.analyzeCollectData(requestInfo, collectDataMap, COLLECT_DATA_NAME);
        this.analyzeCollectData(requestInfo, collectDataMap, COLLECT_DATA_USER_TIME);

        logger.info("计算用时{}ms", new Date().getTime() - current);

//        CollectData collectData = collectDataMap.getCollectData(COLLECT_DATA_NAME);
//        double currentValue = Double.parseDouble(collectData.getValue().toString());
//
//        String id = requestInfo.getClientId() + "." +
//                requestInfo.getIpAddress() + "." + COLLECT_DATA_NAME;

//        IMACalculator calculator1m = this.addDataCollectValue(id, "1m", 10, currentValue, 60);
//        IMACalculator calculator5m = this.addDataCollectValue(id, "5m", 10 * 5, currentValue, 60);
//        IMACalculator calculator15m = this.addDataCollectValue(id, "15m", 10 * 15, currentValue, 60);
//        logger.info("最新CPU值{}", currentValue);
//        logger.info("1分钟CPU平均值{}", calculator1m.calcAvg(2, BigDecimal.ROUND_HALF_UP));
//        logger.info("5分钟CPU平均值{}", calculator5m.calcAvg(2, BigDecimal.ROUND_HALF_UP));
//        logger.info("15分钟CPU平均值{}", calculator15m.calcAvg(2, BigDecimal.ROUND_HALF_UP));


//        RedisTimeCalculator redisTimeCalculator = this.getRedisTimeCalculator(id);
//        long currentTime = requestInfo.getTimestamp(); //new Date().getTime();
//        //精确到秒
//        long currentSecond = currentTime - currentTime % 1000;
//        long currentMinute = currentTime - currentTime % 60000;
//        redisTimeCalculator.addValue(currentSecond, currentValue);
//        long past1m = currentSecond - 60000 + 1;
//        long past5m = currentSecond - 60000 * 5 + 1;
//        long past15m = currentSecond - 60000 * 15 + 1;
//
//        double last1mAvg = redisTimeCalculator.calcAvg(past1m, currentTime);
//        double last5mAvg = redisTimeCalculator.calcAvg(past5m, currentTime);
//        double last15mAvg = redisTimeCalculator.calcAvg(past15m, currentTime);
//
//        logger.info("最新CPU值{}", currentValue);
//        logger.info("1分钟CPU平均值{}", last1mAvg);
//        logger.info("5分钟CPU平均值{}", last5mAvg);
//        logger.info("15分钟CPU平均值{}", last15mAvg);
//
//        //long currentTime = new Date().getTime();
//
//        String period = this.simpleDateFormat.format(currentSecond);
////        ISimpleCalculator simpleCalculator = this.addDataCollectValue(id, period, currentValue);
////        logger.info("{}:CPU平均值{}", period, simpleCalculator.calcAvg(2, BigDecimal.ROUND_HALF_UP));
//        double currentMinuteAvg = redisTimeCalculator.calcAvg(currentMinute, currentTime);
//        logger.info("{}:CPU平均值{}", period, currentMinuteAvg);
//
//        String realtimeKey = id + ".realtime";
//        Map<String, Double> values = new HashMap<>();
//        values.put("current", currentValue);
//        values.put("last1m", last1mAvg);
//        values.put("last5m", last5mAvg);
//        values.put("last15m", last15mAvg);
//        this.redisTemplate.opsForHash().putAll(realtimeKey, values);
//        this.redisTemplate.expire(realtimeKey, 1, TimeUnit.MINUTES);
//
//        String minuteKey = id + ".minutes";
//        this.redisTemplate.opsForZSet().removeRangeByScore(minuteKey, currentMinute, currentMinute);
//        String currentMinuteAvgKey = currentMinute + "," + currentMinuteAvg;
//        this.redisTemplate.opsForZSet().add(minuteKey, currentMinuteAvgKey, currentMinute);
//
//        this.addStoreMinuteValueTask(requestInfo, id, currentMinute);
    }

    private void analyzeCollectData(RequestInfo requestInfo,
                                    CollectDataMap collectDataMap,
                                    String collectDataName) {

        CollectData collectData = collectDataMap.getCollectData(collectDataName);
        double currentValue = Double.parseDouble(collectData.getValue().toString());

        String id = requestInfo.getClientId() + "." +
                requestInfo.getIpAddress() + "." + collectDataName;

        RedisTimeCalculator redisTimeCalculator = this.getRedisTimeCalculator(id);
        long currentTime = requestInfo.getTimestamp();
        //精确到秒
        long currentSecond = currentTime - currentTime % 1000;
        long currentMinute = currentTime - currentTime % 60000;
        redisTimeCalculator.addValue(currentSecond, currentValue);
        long past1m = currentSecond - 60000 + 1;
        long past5m = currentSecond - 60000 * 5 + 1;
        long past15m = currentSecond - 60000 * 15 + 1;

        double last1mAvg = redisTimeCalculator.calcAvg(past1m, currentTime);
        double last5mAvg = redisTimeCalculator.calcAvg(past5m, currentTime);
        double last15mAvg = redisTimeCalculator.calcAvg(past15m, currentTime);

        //String period = this.simpleDateFormat.format(currentSecond);
        double currentMinuteAvg = redisTimeCalculator.calcAvg(currentMinute, currentTime);

        String realtimeKey = id + ".realtime";
        Map<String, Double> values = new HashMap<>();
        values.put("current", currentValue);
        values.put("last1m", last1mAvg);
        values.put("last5m", last5mAvg);
        values.put("last15m", last15mAvg);
        this.redisTemplate.opsForHash().putAll(realtimeKey, values);
        this.redisTemplate.expire(realtimeKey, 1, TimeUnit.MINUTES);

        String minuteKey = id + ".minutes";
        this.redisTemplate.opsForZSet().removeRangeByScore(minuteKey, currentMinute, currentMinute);
        String currentMinuteAvgKey = currentMinute + "," + currentMinuteAvg;
        this.redisTemplate.opsForZSet().add(minuteKey, currentMinuteAvgKey, currentMinute);

        this.addStoreMinuteValueTask(requestInfo, id, currentMinute, collectDataName);

    }


    private IMACalculator addDataCollectValue(String id, String key, int maxSize, double value, int expireTime) {
        String name = id + "." + key;
        IMACalculator calculator = this.mapCalculator.get(name);
        if (calculator == null) {
            calculator = this.createMACalculator(name, maxSize, expireTime);
            this.mapCalculator.put(name, calculator);
        }
        calculator.addValue(value);

        return calculator;
    }

    private ISimpleCalculator addDataCollectValue(String id, String key, double value) {
        String name = id + "." + key;
        ISimpleCalculator calculator = this.mapSimpleCalculator.get(name);
        if (calculator == null) {
            calculator = new MemorySimpleCalculator(name);
            this.mapSimpleCalculator.put(name, calculator);
        }
        calculator.addValue(value);

        return calculator;
    }


    private IMACalculator createMACalculator(String key, int maxSize, int expireTime) {
        return new RedisMACalculator(key, this.redisTemplate, maxSize, expireTime);
//        return new MemoryMACalculator(key, maxSize);
    }


    private RedisTimeCalculator getRedisTimeCalculator(String key) {
        if (!this.redisTimeCalculatorMap.containsKey(key))
            this.redisTimeCalculatorMap.put(key, new RedisTimeCalculator(key, this.redisTemplate));

        return this.redisTimeCalculatorMap.get(key);
    }


    private void addStoreMinuteValueTask(RequestInfo requestInfo, String id, long currentMinute,
                                         String collectDataName) {
        String taskId = id + "." + String.valueOf(currentMinute);
        TimedTask task = new TimedTask(taskId);
        long nextTime = currentMinute + 61000;
        task.setRunTime(nextTime);
        task.setTodoTaskName(WinProcessorTimePerMinuteTask.TASK_NAME);
        task.put("id", id);
        task.put("time", String.valueOf(currentMinute));
        task.put("clientId", requestInfo.getClientId());
        task.put("clientName", requestInfo.getClientName());
        task.put("machineIP", requestInfo.getIpAddress());
        task.put("collectDataName", collectDataName);

        this.timedTaskFactory.addTimedTask(task);
    }

}
