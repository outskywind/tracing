package com.dafy.skye.zipkin.extend.enums;

/**
 * Created by quanchengyun on 2018/5/3.
 */
public enum Demension {

    SUCCESS_RATE("success_rate"),LATENCY("latency");
    String value;

    Demension(String value){
        this.value=value;
    }

    public String value(){
        return value;
    }

}
