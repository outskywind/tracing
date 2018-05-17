package com.dafy.skye.druid.entity;

import java.util.Map;

/**
 * Created by quanchengyun on 2018/5/17.
 */
public class TimeSeriesQueryResult {

    private String timestamp;

    private Map result;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map getResult() {
        return result;
    }

    public void setResult(Map result) {
        this.result = result;
    }
}
