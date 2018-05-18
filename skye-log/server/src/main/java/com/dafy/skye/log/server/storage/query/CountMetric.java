package com.dafy.skye.log.server.storage.query;

/**
 * Created by quanchengyun on 2018/5/18.
 */
public class CountMetric {

    private long start ;

    private long count;

    public CountMetric(long start , long count){
        this.start=start;
        this.count=count;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
