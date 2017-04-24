package com.dafy.skye.server.klog.dto;

import java.util.Date;
import java.util.Map;

/**
 * Created by Caedmon on 2017/4/24.
 */
public class TraceLogDTO {
    private String tracesId;
    private Date timestamp;
    private String address;
    private String level;
    private String loggerName;
    private Map<String,String> mdc;
    private String message;
    private Integer pid;
    private String serviceName;
    private String thread;

    public String getTracesId() {
        return tracesId;
    }

    public void setTracesId(String tracesId) {
        this.tracesId = tracesId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public Map<String, String> getMdc() {
        return mdc;
    }

    public void setMdc(Map<String, String> mdc) {
        this.mdc = mdc;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }
}
