package com.dafy.skye.component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by quanchengyun on 2017/11/2.
 */
public class DateUtil {


    public static String getDateformat(){
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
        Calendar cl  = Calendar.getInstance();
        //cl.add(Calendar.DAY_OF_MONTH,1);
        Date date = cl.getTime();
        String yesterday = sdf.format(date);
        return yesterday;
    }
}
