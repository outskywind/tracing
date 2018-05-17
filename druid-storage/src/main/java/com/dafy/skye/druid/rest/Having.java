package com.dafy.skye.druid.rest;

/**
 * Created by quanchengyun on 2018/5/14.
 */
public class Having {

    private String type;
    private String aggregation;
    private long value;

    public Having(String type ,String aggregationName,long value){
        this.type=type;
        this.aggregation=aggregationName;
        this.value=value;
    }

    public String getType() {
        return type;
    }

    public String getAggregation() {
        return aggregation;
    }

    public long getValue() {
        return value;
    }
}
