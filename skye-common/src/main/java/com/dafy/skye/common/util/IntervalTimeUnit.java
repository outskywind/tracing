package com.dafy.skye.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Caedmon on 2017/6/16.
 */
public enum IntervalTimeUnit {
    MINUTES(1),HOUR(2),DAY(3),WEEK(4);
    public int value;
    public static Map<Integer,IntervalTimeUnit> map=new HashMap<>();
    static {
        map.put(MINUTES.value,MINUTES);
        map.put(HOUR.value,HOUR);
        map.put(DAY.value,DAY);
        map.put(WEEK.value,WEEK);
    }
    IntervalTimeUnit(int value){
        this.value=value;
    }
    public static IntervalTimeUnit valueOf(int value){
        return map.get(value);
    }

    public long getMills(){
        switch (this){
            case MINUTES:
                return 600000;
            case HOUR:
                return 3600000;
            case DAY:
                return 86400000;
            case WEEK:
                return 604800000;
        }
        throw new NullPointerException("unit is null");
    }
}
