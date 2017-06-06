package com.dafy.skye.log.collector.storage.entity;

import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import com.datastax.driver.core.utils.UUIDs;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class SkyeLogEntity {
    private String tsUuid;
    private String traceId;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date timestamp;
    private String serviceName;
    private String address;
    private String pid;
    private String thread;
    private String loggerName;
    private String level;
    private Map<String,String> mdc;
    private String message;
    private Long seqNo;
    private String exception;
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getMdc() {
        return mdc;
    }

    public void setMdc(Map<String, String> mdc) {
        this.mdc = mdc;
    }

    public String getTsUuid() {
        return tsUuid;
    }

    public void setTsUuid(String tsUuid) {
        this.tsUuid = tsUuid;
    }

    public Long getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Long seqNo) {
        this.seqNo = seqNo;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public static SkyeLogEntity build(SkyeLogEvent event){
        SkyeLogEntity entity=new SkyeLogEntity();
        entity.setTraceId(event.getMdc().get("skyeTraceId"));
        Random random=new Random();
        UUID uuid = new UUID(UUIDs.startOf(event.getTimeStamp()).getMostSignificantBits(), random.nextLong());
        entity.setTsUuid(uuid.toString());
        entity.setTimestamp(new Date(event.getTimeStamp()));
        entity.setServiceName(event.getServiceName());
        entity.setAddress(event.getAddress());
        entity.setPid(event.getPid());
        entity.setThread(event.getThreadName());
        entity.setLoggerName(event.getLoggerName());
        entity.setLevel(event.getLevel().toString());
        entity.setMdc(event.getMdc());
        entity.setMessage(event.getFormattedMessage());
        entity.setSeqNo(event.getSeqNo());
        if(event.getThrowableProxy()!=null){
            String exception=ThrowableProxyUtil.asString(event.getThrowableProxy());
            entity.setException(exception);
        }
        return entity;
    }
}
