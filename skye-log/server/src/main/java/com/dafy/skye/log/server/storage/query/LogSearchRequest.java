package com.dafy.skye.log.server.storage.query;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class LogSearchRequest {

    public String service;
    public List<String> hosts;
    public List<String> level;
    public String traceId;
    public String mdc;
    public Long start;
    public Long end;
    public String keyword;
    public String exception;

    public String timeInterval;

    public Integer page;
    public Integer size;
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMdc() {
        return mdc;
    }

    public void setMdc(String mdc) {
        this.mdc = mdc;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
    public int getFrom(){
        if(page <=0){
            page =1;
        }
        if(size <=0){
            size =100;
        }
        return (page -1)* size;
    }
    public int getTo(){
        if(page <=0){
            page =1;
        }
        if(size <=0){
            size =100;
        }
        return page * size;
    }

    public List<String> getLevel() {
        return level;
    }

    public void setLevel(List<String> level) {
        this.level = level;
    }

    public String getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "LogSearchRequest{" +
                "service='" + service + '\'' +
                ", hosts=" + hosts +
                ", level=" + level +
                ", traceId='" + traceId + '\'' +
                ", mdc='" + mdc + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", keyword='" + keyword + '\'' +
                ", exception='" + exception + '\'' +
                ", timeInterval='" + timeInterval + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
