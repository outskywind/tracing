package com.dafy.skye.log.collector.storage.query;

import org.slf4j.MDC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class LogQueryRequest {
    private String traceId;
    private String message;
    private Long startTs;
    private Long endTs;
    private List<String> levels;
    private List<String> serviceNames;
    private String mdc;
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getStartTs() {
        return startTs;
    }

    public void setStartTs(Long startTs) {
        this.startTs = startTs;
    }

    public Long getEndTs() {
        return endTs;
    }

    public void setEndTs(Long endTs) {
        this.endTs = endTs;
    }

    public List<String> getLevels() {
        return levels;
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }

    public String getMdc() {
        return mdc;
    }

    public void setMdc(String mdc) {
        this.mdc = mdc;
    }

    @Override
    public String toString() {
        return "LogQueryRequest{" +
                "traceId='" + traceId + '\'' +
                ", message='" + message + '\'' +
                ", startTs=" + startTs +
                ", endTs=" + endTs +
                ", levels='" + levels + '\'' +
                ", serviceNames='" + serviceNames + '\'' +
                ", mdc=" + mdc +
                '}';
    }
}
