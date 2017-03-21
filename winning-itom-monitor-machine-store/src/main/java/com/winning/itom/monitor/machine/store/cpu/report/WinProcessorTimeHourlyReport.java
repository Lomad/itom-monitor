package com.winning.itom.monitor.machine.store.cpu.report;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by nicholasyan on 17/3/18.
 */
public class WinProcessorTimeHourlyReport {

    private String clientId;
    private String clientName;
    private String machineIP;
    private Date hourlyDate;
    private String hourlyDateText;
    private LinkedHashMap<Integer, Double> values = new LinkedHashMap<>();

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

    public LinkedHashMap<Integer, Double> getValues() {
        return values;
    }

    public void setValues(LinkedHashMap<Integer, Double> values) {
        this.values = values;
    }
}
