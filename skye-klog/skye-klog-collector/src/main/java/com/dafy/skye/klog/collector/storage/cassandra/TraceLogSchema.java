package com.dafy.skye.klog.collector.storage.cassandra;

import com.alibaba.fastjson.annotation.JSONField;
import com.dafy.skye.klog.core.logback.KLogEvent;
import com.datastax.driver.core.utils.UUIDs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Caedmon on 2017/4/22.
 */
public class TraceLogSchema {
    @JSONField(name = "logger_name")
    private String loggerName;
    @JSONField(name = "trace_id")
    private String traceId;
    @JSONField(name = "service_name")
    private String serviceName;
    @JSONField(name = "logger_name")
    private String address;
    @JSONField(name = "pid")
    private String pid;
    @JSONField(name = "ts")
    private long ts;
    @JSONField(name = "level")
    private String level;
    @JSONField(name = "message")
    private String message;
    @JSONField(name = "mdc")
    private Map<String,String> mdc;
    @JSONField(name = "ts_uuid")
    private String tsUuid;

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
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

    public static TraceLogSchema build(KLogEvent event){
        TraceLogSchema schema=new TraceLogSchema();
        Map<String,String> mdc=event.getMdc();
        if(mdc!=null&&!mdc.isEmpty()){
            String traceId=mdc.get("skyeTraceId");
            schema.setTraceId(traceId);
            schema.setMdc(mdc);
        }
        schema.setAddress(event.getAddress());
        schema.setLevel(event.getLevel().toString());
        schema.setServiceName(event.getServiceName());
        schema.setTs(event.getTimeStamp());
        schema.setLoggerName(event.getLoggerName());
        schema.setMessage(event.getMessage());
        schema.setPid(event.getPid());
        schema.setMdc(event.getMdc());
        schema.setTsUuid(new UUID(
                UUIDs.startOf(0 != event.getTimeStamp() ? (event.getTimeStamp() / 1000) : 0)
                        .getMostSignificantBits(),
                UUIDs.random().getLeastSignificantBits()));
        return schema;
    }
}
