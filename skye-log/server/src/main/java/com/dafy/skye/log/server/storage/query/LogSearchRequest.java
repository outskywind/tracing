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
    public String serviceName;
    public String mdc;
    public Integer pageIndex;
    public Integer pageSize;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceNames(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMdc() {
        return mdc;
    }

    public void setMdc(String mdc) {
        this.mdc = mdc;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    public int getFrom(){
        if(pageIndex<=0){
            pageIndex=1;
        }
        if(pageSize<=0){
            pageSize=100;
        }
        return (pageIndex-1)*pageSize;
    }
    public int getTo(){
        if(pageIndex<=0){
            pageIndex=1;
        }
        if(pageSize<=0){
            pageSize=100;
        }
        return pageIndex*pageSize;
    }
    @Override
    public String toString() {
        return "LogSearchRequest{" +
                "traceId='" + traceId + '\'' +
                ", message='" + message + '\'' +
                ", lookback=" + lookback +
                ", endTs=" + endTs +
                ", levels=" + levels +
                ", serviceNames=" + serviceName +
                ", mdc='" + mdc + '\'' +
                ", pageIndex=" + pageIndex +
                ", pageSize=" + pageSize +
                '}';
    }
}
