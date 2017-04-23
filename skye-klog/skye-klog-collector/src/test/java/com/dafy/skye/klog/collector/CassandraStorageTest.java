package com.dafy.skye.klog.collector;

import ch.qos.logback.classic.Level;
import com.dafy.skye.klog.collector.storage.cassandra.CassandraConfigProperties;
import com.dafy.skye.klog.collector.storage.cassandra.CassandraStorage;
import com.dafy.skye.klog.core.logback.KLogEvent;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Caedmon on 2017/4/22.
 */
public class CassandraStorageTest {

    public CassandraConfigProperties buildConfigProperties(){
        CassandraConfigProperties configProperties=new CassandraConfigProperties();
        return configProperties;
    }
    public static KLogEvent buildEvent(){
        KLogEvent event=new KLogEvent();
        Map<String,String> mdc=new HashMap<>();
        mdc.put("skyeTraceId","4654b2928153ee49");
        event.setServiceName("skye-collector");
        event.setAddress("localhost");
        event.setLevel(Level.DEBUG);
        event.setMessage("Skye build the event ");
        event.setLoggerName("com.dafy.skye.Builder");
        event.setPid("11581");
        event.setThreadName("Thread-1");
        event.setMdcPropertyMap(mdc);
        event.setTimeStamp(System.currentTimeMillis());
        return event;
    }
    public CassandraStorage startStorage(){
        CassandraConfigProperties configProperties=buildConfigProperties();
        CassandraStorage cassandraStorage=new CassandraStorage(configProperties);
        cassandraStorage.start();
        return cassandraStorage;
    }
    @Test
    public void testCreateTable(){
        startStorage();
    }
    @Test
    public void testSaveLog(){
        CassandraStorage storage=startStorage();
        KLogEvent event=buildEvent();
        storage.save(event);
    }
}
