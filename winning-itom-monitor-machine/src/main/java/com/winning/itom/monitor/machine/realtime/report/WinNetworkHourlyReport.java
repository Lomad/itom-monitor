package com.winning.itom.monitor.machine.realtime.report;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by nicholasyan on 17/3/18.
 */
public class WinNetworkHourlyReport {

    private String clientId;
    private String clientName;
    private String machineIP;
    private Date hourlyDate;
    private String hourlyDateText;
    private String networkInterface;

    private LinkedHashMap<Integer, Double> receivedBytes = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Double> sentBytes = new LinkedHashMap<>();

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

    public LinkedHashMap<Integer, Double> getReceivedBytes() {
        return receivedBytes;
    }

    public void setReceivedBytes(LinkedHashMap<Integer, Double> receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    public LinkedHashMap<Integer, Double> getSentBytes() {
        return sentBytes;
    }

    public void setSentBytes(LinkedHashMap<Integer, Double> sentBytes) {
        this.sentBytes = sentBytes;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }
}
