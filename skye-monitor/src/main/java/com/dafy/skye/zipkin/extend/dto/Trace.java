package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.zipkin.extend.enums.Stat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by quanchengyun on 2018/5/17.
 */
public class Trace {

    private String traceId;

    private String host;

    private long latency;

    private long timestamp;

    private boolean isSuccess;

    private Map<String,Stat> stat=new HashMap<>();

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Map<String, Stat> getStat() {
        return stat;
    }

    public void setStat(Map<String, Stat> stat) {
        this.stat = stat;
    }
}
