package com.dafy.skye.druid.rest;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class Field {

    private FieldType type;

    private String dimension;

    private Object value;


    public static enum FieldType{
        selector();
    }


    public static Field builder(){
        return new Field();
    }


    public Field type(FieldType type){
        this.type=type;
        return this;
    }

    public Field dimension(String dimension){
        this.dimension=dimension;
        return this;
    }

    public Field value(Object value){
        this.value=value;
        return this;
    }


    public FieldType getType() {
        return type;
    }

    public String getDimension() {
        return dimension;
    }

    public Object getValue() {
        return value;
    }
}
