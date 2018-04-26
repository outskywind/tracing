package com.dafy.skye.zipkin.extend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Caedmon on 2017/7/3.
 */
public class TimeUtil {
    public static long microToMills(double microSeconds){

        if(microSeconds==Double.POSITIVE_INFINITY
                ||microSeconds==Double.NEGATIVE_INFINITY
                ||microSeconds==0){
            return 0;
        }
        return new BigDecimal(microSeconds)
                .divide(new BigDecimal(1000))
                .setScale(0, RoundingMode.HALF_UP).longValue();
    }


    public static int parseTimeUnit(String unit){
        String timeUnit = unit.substring(unit.length()-1);
        int unitInt=0;
        switch (timeUnit){
            case "s": unitInt = 1000;break;
            case "m": unitInt =60000;break;
            case "h": unitInt =3600000;break;
        }
        return Integer.parseInt(unit.substring(0,unit.length()-1))*unitInt;
    }
}
