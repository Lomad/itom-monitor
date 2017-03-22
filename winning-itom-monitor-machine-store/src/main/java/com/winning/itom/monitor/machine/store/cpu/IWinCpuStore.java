package com.winning.itom.monitor.machine.store.cpu;


import com.winning.itom.monitor.machine.store.cpu.entity.CpuValues;

import java.util.Map;

/**
 * Created by nicholasyan on 17/3/18.
 */
public interface IWinCpuStore {

    CpuValues findCurrentProcessorCpuValues(String clientId, String ip);

    CpuValues findLastProcessorCpuValuesInCache(String clientId, String ip, long start);

    Map<String, CpuValues> findMinuteCpuValues(String clientId, String ip, long startHour, long endHour);


}
