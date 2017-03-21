package com.winning.itom.monitor.api.entity;

/**
 * Created by nicholasyan on 17/3/15.
 */
public class CollectData {

    private String name;

    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
