package com.dafy.skye.zipkin.extend.query;

/**
 * Created by quanchengyun on 2018/5/10.
 */
public enum Dimension {

    service("localEndpoint.serviceName",1),interface_name("name",2),host("localEndpoint.ipv4",3);
    private String value;
    private int order;

    Dimension( String value,int order){
        this.value = value;
        this.order = order;
    }

    String value(){
        return  this.value;
    }
    int order(){
        return this.order;
    }
}
