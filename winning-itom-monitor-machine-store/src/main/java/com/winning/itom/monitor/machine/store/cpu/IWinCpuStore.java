package com.winning.itom.monitor.machine.store.cpu;


import com.winning.itom.monitor.machine.store.cpu.entity.CpuValues;

/**
 * Created by nicholasyan on 17/3/18.
 */
public interface IWinCpuStore {

    CpuValues findCurrentCpuValues(String clientId, String ip);

    CpuValues findLastCpuValuesInCache(String clientId, String ip, long start);

    CpuValues findMinuteCpuValues(String clientId, String ip, long startHour, long endHour);


}
