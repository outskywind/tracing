package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.zipkin.extend.enums.Stat;

/**
 * Created by quanchengyun on 2018/4/23.
 */
public class MonitorMetric {
    // service 或者是 host 或者是 spanName=interface
    public String name;
    public String success_rate;
    private double successPercent;
    public double qps;
    public long latency;
    public Stat stat=Stat.GREEN;

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
}
