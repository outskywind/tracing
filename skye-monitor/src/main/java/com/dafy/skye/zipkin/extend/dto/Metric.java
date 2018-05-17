package com.dafy.skye.zipkin.extend.dto;

/**
 * Created by quanchengyun on 2018/5/16.
 */
public class Metric {

    private String name;
    private long qps;
    private long peak_qps;
    private long latency;
    private long count;
    //百分比
    private String success_rate;


    public long getQps() {
        return qps;
    }

    public void setQps(long qps) {
        this.qps = qps;
    }

    public long getPeak_qps() {
        return peak_qps;
    }

    public void setPeak_qps(long peak_qps) {
        this.peak_qps = peak_qps;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getSuccess_rate() {
        return success_rate;
    }

    public void setSuccess_rate(String success_rate) {
        this.success_rate = success_rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
