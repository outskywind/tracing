package com.dafy.skye.zipkin.extend.enums;

/**
 * Created by quanchengyun on 2018/4/23.
 */
public enum Stat {

    green(0),yellow(1),red(2);
    int value;

    Stat(int value){
        this.value = value;
    }

    public int value(){
        return this.value;
    }

}
