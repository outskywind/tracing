package com.dafy.skye.druid.entity;

import java.util.Map;

/**
 * Created by quanchengyun on 2018/5/16.
 */
public class GroupbyQueryResult {

    private String timestamp;

    private Map event;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map getEvent() {
        return event;
    }

    public void setEvent(Map event) {
        this.event = event;
    }

}
