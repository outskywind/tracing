package com.dafy.skye.druid.rest;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public enum Fn {

    div("/"),mult("*"),plus("+"),minus("-");
    private String value;
    Fn(String value){
        this.value=value;
    }

    String value(){
        return this.value;
    }
}
