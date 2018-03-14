package com.dafy.skye.log.server;

import ch.qos.logback.classic.Level;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Caedmon on 2017/4/27.
 */
public class StorageTestHelper {

    @Value("")
    public String code;


    public static SkyeLogEvent buildEvent(){
        SkyeLogEvent event=new SkyeLogEvent();
        Map<String,String> mdc=new HashMap<>();
        mdc.put("skyeTraceId","4654b2928153ee49");
        event.setServiceName("skye-collector");
        event.setAddress("localhost");
        event.setLevel(Level.DEBUG);
        event.setMessage("陈龙 build the event ");
        event.setLoggerName("com.dafy.skye.Builder");
        event.setPid("11581");
        event.setThreadName("Thread-1");
        event.setMdcPropertyMap(mdc);
        event.setTimeStamp(System.currentTimeMillis());
        return event;
    }
}
