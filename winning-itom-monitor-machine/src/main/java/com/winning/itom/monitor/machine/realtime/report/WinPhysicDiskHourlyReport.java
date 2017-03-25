package com.winning.itom.monitor.machine.realtime.report;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by nicholasyan on 17/3/18.
 */
public class WinPhysicDiskHourlyReport {

    private String clientId;
    private String clientName;
    private String machineIP;
    private Date hourlyDate;
    private String hourlyDateText;
    private String physicDisk;

    private LinkedHashMap<Integer, Double> readSecBytes = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Double> writeSecBytes = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Double> transferSec = new LinkedHashMap<>();

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getMachineIP() {
        return machineIP;
    }

    public void setMachineIP(String machineIP) {
        this.machineIP = machineIP;
    }

    public Date getHourlyDate() {
        return hourlyDate;
    }

    public void setHourlyDate(Date hourlyDate) {
        this.hourlyDate = hourlyDate;
    }

    public String getHourlyDateText() {
        return hourlyDateText;
    }

    public void setHourlyDateText(String hourlyDateText) {
        this.hourlyDateText = hourlyDateText;
    }

    public LinkedHashMap<Integer, Double> getReadSecBytes() {
        return readSecBytes;
    }

    public void setReadSecBytes(LinkedHashMap<Integer, Double> readSecBytes) {
        this.readSecBytes = readSecBytes;
    }

    public LinkedHashMap<Integer, Double> getWriteSecBytes() {
        return writeSecBytes;
    }

    public void setWriteSecBytes(LinkedHashMap<Integer, Double> writeSecBytes) {
        this.writeSecBytes = writeSecBytes;
    }

    public String getPhysicDisk() {
        return physicDisk;
    }

    public void setPhysicDisk(String physicDisk) {
        this.physicDisk = physicDisk;
    }

    public LinkedHashMap<Integer, Double> getTransferSec() {
        return transferSec;
    }

    public void setTransferSec(LinkedHashMap<Integer, Double> transferSec) {
        this.transferSec = transferSec;
    }
}
