package com.dafy.skye.druid.entity;


import java.io.Serializable;

/**
 * Created by quanchengyun on 2017/8/25.
 */
public class SkyeMetric implements Serializable {

    private String serviceName;

    private String spanName;

    private String host;

    private long timestamp;

    private double duration;

    private int success=0;

    private int exception=0;


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String servicename) {
        this.serviceName = servicename;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getException() {
        return exception;
    }

    public void setException(int exception) {
        this.exception = exception;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
