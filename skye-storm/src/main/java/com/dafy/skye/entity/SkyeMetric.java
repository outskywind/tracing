package com.dafy.skye.entity;


import java.io.Serializable;
import java.util.*;

/**
 * Created by quanchengyun on 2017/8/25.
 */
public class SkyeMetric implements Serializable {

    private String serviceName;

    private String spanName;

    private long timestamp;

    private double duration;

    private int success=0;

    private int exception=0;

    private boolean isTraceComplete;


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

    public boolean isTraceComplete() {
        return isTraceComplete;
    }

    public void setTraceComplete(boolean traceComplete) {
        isTraceComplete = traceComplete;
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
}
