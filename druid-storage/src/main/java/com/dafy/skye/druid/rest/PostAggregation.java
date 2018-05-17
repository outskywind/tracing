package com.dafy.skye.druid.rest;

import java.util.Arrays;
import java.util.List;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class PostAggregation {


    private String type="arithmetic";

    private String name;

    private String fn;

    private List fields;

    public static PostAggregation builder(){
        return new PostAggregation();
    }



    public static class PostAggregationField{
        private String type="fieldAccess";
        private String fieldName;

        public PostAggregationField (String fieldName){
            this.fieldName=fieldName;
        }

        public String getType() {
            return type;
        }

        public String getFieldName() {
            return fieldName;
        }
    }


    public PostAggregation type(String type){
        this.type=type;
        return this;
    }

    public PostAggregation name(String name){
        this.name=name;
        return this;
    }

    public PostAggregation fn(Fn fn){
        this.fn=fn.value();
        return this;
    }

    public PostAggregation fields(List<PostAggregationField> fields){
        this.fields= fields;
        return this;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getFn() {
        return fn;
    }

    public List getFields() {
        return fields;
    }
}
