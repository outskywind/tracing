package com.dafy.skye.druid.rest;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class Aggregation {

    private AggregationType type;

    private String name;

    private String fieldName;

    public static Aggregation builder(){
        return new Aggregation();
    }

    public Aggregation type(AggregationType type){
        this.type=type;
        return this;
    }

    public Aggregation name(String name){
        this.name=name;
        return this;
    }

    public Aggregation fieldName(String fieldName){
        this.fieldName=fieldName;
        return this;
    }

    public AggregationType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return fieldName;
    }
}
