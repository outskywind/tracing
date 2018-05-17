package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.zipkin.extend.enums.Stat;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by quanchengyun on 2018/4/23.
 */
public class MonitorMetric {
    // service 或者是 host 或者是 spanName=interface
    private String name;
    private String success_rate;
    //后台计算判断用，不给页面,数据库规则保留的是整数百分数
    @JsonIgnore
    private double successPercent;
    private double qps;
    private long peak_qps;
    private long latency;
    private long count;
    //jackson 序列化枚举类型时，使用的是枚举类型的名字
    private Stat stat=Stat.green;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuccess_rate() {
        return success_rate;
    }

    public void setSuccess_rate(String success_rate) {
        this.success_rate = success_rate;
    }

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }

    public double getSuccessPercent() {
        return successPercent;
    }

    public void setSuccessPercent(double successPercent) {
        this.successPercent = successPercent;
    }

    public long getPeak_qps() {
        return peak_qps;
    }

    public void setPeak_qps(long peak_qps) {
        this.peak_qps = peak_qps;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
