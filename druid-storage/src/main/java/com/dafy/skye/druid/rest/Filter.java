package com.dafy.skye.druid.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class Filter {


    private LogicType type ;

    private List fields = new ArrayList<>();

    private String dimension;

    private Object value;

    public static Filter builder(){
        return  new Filter();
    }

    public Filter type(LogicType type){
        this.type=type;
        return this;
    }

    public Filter dimension(String dimension){
        this.dimension=dimension;
        return this;
    }

    public Filter value(Object value){
        this.value=value;
        return this;
    }

    public Filter fields(Field... field){
        fields.addAll(Arrays.asList(field));
        return this;
    }

    public Filter subFields(Filter subfields){
        fields.add(subfields);
        return this;
    }

    public LogicType getType() {
        return type;
    }

    public List getFields() {
        return fields;
    }

    public String getDimension() {
        return dimension;
    }

    public Object getValue() {
        return value;
    }
}
