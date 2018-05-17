package com.dafy.skye.zipkin.extend.dto;

/**
 * Created by quanchengyun on 2018/5/15.
 */
public class ServiceSeriesRequest {

    private String service;

    private long start;

    private long end;

    private String timeInterval;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
    }
}
