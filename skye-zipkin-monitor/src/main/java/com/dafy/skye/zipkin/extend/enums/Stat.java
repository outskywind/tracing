package com.dafy.skye.zipkin.extend.enums;

/**
 * Created by quanchengyun on 2018/4/23.
 */
public enum Stat {

    green("green"),yellow("yellow"),red("red");
    String value;

    Stat(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
