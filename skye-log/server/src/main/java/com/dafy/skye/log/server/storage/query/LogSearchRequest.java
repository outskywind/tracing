package com.dafy.skye.log.server.storage.query;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class LogSearchRequest {
    public String traceId;
    public String message;
    public Long lookback;
    public Long endTs;
    public List<String> levels;
    public List<String> serviceNames;
    public String mdc;

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

    public Long getLookback() {
        return lookback;
    }

    public void setLookback(Long lookback) {
        this.lookback = lookback;
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
        return "LogSearchRequest{" +
                "traceId='" + traceId + '\'' +
                ", message='" + message + '\'' +
                ", lookback=" + lookback +
                ", endTs=" + endTs +
                ", levels=" + levels +
                ", serviceNames=" + serviceNames +
                ", mdc='" + mdc + '\'' +
                '}';
    }
}
