package com.dafy.skye.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Caedmon on 2017/6/8.
 */
public class JacksonConvert {
    public static final ObjectMapper mapper=new ObjectMapper();
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public static String toJsonString(Object object){
        try{
            return mapper.writeValueAsString(object);
        }catch (JsonProcessingException e){
            throw new IllegalArgumentException(e);
        }

    }
    public static <T>  T readValue(String json,Class<T> type){
        try{
            return mapper.readValue(json,type);
        }catch (IOException e){
            throw new IllegalArgumentException(e);
        }

    }
    public static Object readValue(String json, JavaType type) {
        try{
            return mapper.readValue(json,type);
        }catch (IOException e){
            throw new IllegalArgumentException(e);
        }
    }
    public static ObjectMapper mapper(){
        return mapper;
    }
}
