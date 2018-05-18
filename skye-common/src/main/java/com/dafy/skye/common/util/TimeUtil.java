package com.dafy.skye.common.util;

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


    public static int parseTimeInterval(String unit){
        String timeUnit = unit.substring(unit.length()-1);
        int unitInt=0;
        switch (timeUnit){
            case "s": unitInt = 1000;break;
            case "m": unitInt =60000;break;
            case "h": unitInt =3600000;break;
        }
        return Integer.parseInt(unit.substring(0,unit.length()-1))*unitInt;
    }


    public static int parseTimeIntervalSeconds(String unit){
        String timeUnit = unit.substring(unit.length()-1);
        int unitInt=0;
        switch (timeUnit){
            case "s": unitInt = 1;break;
            case "m": unitInt =60;break;
            case "h": unitInt =3600;break;
        }
        return Integer.parseInt(unit.substring(0,unit.length()-1))*unitInt;
    }


    private static double div(long div1, long div2, int scale){
        BigDecimal bigDecimal = new BigDecimal(div1);
        BigDecimal bigDecimal2 = new BigDecimal(div2);
        return bigDecimal.divide(bigDecimal2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String  adaptTimeInterval(long timeRangeMillis , int maxPixes){

        long duration_hour = timeRangeMillis/3600000; //范围换算成小时
        int[] MINUTEINTERVAL=new int[]{1,5,10,30};
        int[] HOURINTERVAL=new int[]{1,2,3,6,12};
        int[] DAYINTERVAL=new int[]{1,7,15,30};
        //时间间隔小时
        double interval_hour = div(duration_hour,maxPixes,1);
        //二分查找
        //大于半小时间隔就要从1小时以上开始
        //大于半个小时，从1小时开始
        String adaptedTimeInterval = null;

        if (interval_hour>0.5){
            //时间间隔大于12小时，从1天开始判断
            if(interval_hour>12){
                //向上取整
                double interval_day = Math.ceil(interval_hour/24);
                //按天计算可能会超出
                for(int i=0;i<DAYINTERVAL.length;i++){
                    if(interval_day>DAYINTERVAL[i]){
                        continue;
                    }
                    adaptedTimeInterval = DAYINTERVAL[i]+"d";
                }
            }else{
                //不大于12小时间隔，就从1小时开始判断
                //向上取整
                interval_hour = Math.ceil(interval_hour);
                for(int i=0;i<HOURINTERVAL.length;i++){
                    if(interval_hour>HOURINTERVAL[i]){
                        continue;
                    }
                    adaptedTimeInterval = HOURINTERVAL[i]+"h";
                }
            }
        }else{
            //不大于半小时
            double interval_minute = Math.ceil(interval_hour*30);
            for(int i=0;i<MINUTEINTERVAL.length;i++){
                if(interval_minute>MINUTEINTERVAL[i]){
                    continue;
                }
                adaptedTimeInterval = MINUTEINTERVAL[i]+"m";
            }
        }
        return adaptedTimeInterval;
    }


}
