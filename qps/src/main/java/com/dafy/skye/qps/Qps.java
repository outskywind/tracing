package com.dafy.skye.qps;

import zipkin2.Endpoint;

/**
 * Created by quanchengyun on 2018/5/8.
 */
public class Qps {

    private String name;
    private long timestamp;
    private long qps;
    private Endpoint localEndpoint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getQps() {
        return qps;
    }

    public void setQps(long qps) {
        this.qps = qps;
    }

    public Endpoint getLocalEndpoint() {
        return localEndpoint;
    }

    public void setLocalEndpoint(Endpoint localEndpoint) {
        this.localEndpoint = localEndpoint;
    }
}
