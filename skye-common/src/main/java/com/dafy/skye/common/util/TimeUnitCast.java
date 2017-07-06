package com.dafy.skye.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by quanchengyun on 2017/7/5.
 */
public class TimeUnitCast {
    public static Map<String,IntervalTimeUnit> timeUnitMap = new HashMap<String,IntervalTimeUnit>();
    static {
        timeUnitMap.put("m",IntervalTimeUnit.MINUTES);
        timeUnitMap.put("h",IntervalTimeUnit.HOUR);
        timeUnitMap.put("d",IntervalTimeUnit.DAY);
    }

    public static IntervalTimeUnit cast(String key){
        return timeUnitMap.get(key);
    }
}
