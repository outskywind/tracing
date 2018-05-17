package com.dafy.skye.zipkin.extend.dto;

/**
 * Created by quanchengyun on 2018/5/16.
 */
public class SeriesMetric {

    private long start;
    private double qps;
    private long latency;
    private long count;

    public SeriesMetric ( long start, double qps,long latency,long count){
        this.start=start;
        this.qps = qps;
        this.latency = latency;
        this.count = count;
    }

    public long getStart() {
        return start;
    }
    public double getQps() {
        return qps;
    }
    public long getLatency() {
        return latency;
    }

    public long getCount() {
        return count;
    }
}
