package com.dafy.skye.druid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.TimeZone;

/**
 * Created by quanchengyun on 2017/9/21.
 */
public class NormalTest {


    @Test
    public void testJodatime(){
        DateTime dateTime1 = new DateTime(DateTimeZone.UTC);
        System.out.println(dateTime1);

        TimeZone tz = TimeZone.getDefault();
        System.out.println(tz);
    }
}
