package com.dafy.skye.log.collector.storage.cassandra.domain;

import com.dafy.skye.log.core.logback.SkyeLogEvent;
import com.datastax.driver.core.utils.UUIDs;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Caedmon on 2017/4/22.
 */
public class CassandraSkyeLogEntity {
    private String loggerName;
    private String traceId;
    private String serviceName;
    private String thread;
    private String address;
    private String pid;
    private long ts;
    private String level;
    private String message;
    private Map<String,String> mdc;
    private UUID tsUuid;

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

    public UUID getTsUuid() {
        return tsUuid;
    }

    public void setTsUuid(UUID tsUuid) {
        this.tsUuid = tsUuid;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public static CassandraSkyeLogEntity build(SkyeLogEvent event){
        CassandraSkyeLogEntity schema=new CassandraSkyeLogEntity();
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
        schema.setMessage(event.getFormattedMessage());
        schema.setPid(event.getPid());
        schema.setMdc(event.getMdc());
        schema.setThread(event.getThreadName());
        Random random = new Random();
        UUID uuid = new UUID(UUIDs.startOf(event.getTimeStamp()).getMostSignificantBits(), random.nextLong());
        schema.setTsUuid(uuid);
        return schema;
    }
}
