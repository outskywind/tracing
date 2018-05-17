package com.dafy.skye.druid.rest;

import org.joda.time.DateTime;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class TimeInterval {

    private static String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
    private DateTime start;
    private DateTime end;

    public static TimeInterval builder(){
        return new TimeInterval();
    }

    public TimeInterval start(DateTime start){
        this.start=start;
        return this;
    }


    public TimeInterval end(DateTime end){
        this.end=end;
        return this;
    }

    public String format(){
        return new StringBuilder(start.toString(pattern)).append("/").append(end.toString(pattern)).toString();
    }

}
