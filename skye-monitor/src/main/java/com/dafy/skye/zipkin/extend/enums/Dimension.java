package com.dafy.skye.zipkin.extend.enums;

/**
 * Created by quanchengyun on 2018/5/3.
 */
public enum Dimension {

    SUCCESS_RATE("success_rate"),LATENCY("latency");
    String value;

    Dimension(String value){
        this.value=value;
    }

    public String value(){
        return value;
    }

}
