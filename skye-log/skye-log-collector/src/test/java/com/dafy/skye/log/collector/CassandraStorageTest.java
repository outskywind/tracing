package com.dafy.skye.log.collector;

import ch.qos.logback.classic.Level;
import com.dafy.skye.log.collector.storage.cassandra.CassandraConfig;
import com.dafy.skye.log.collector.storage.cassandra.CassandraStorage;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Caedmon on 2017/4/22.
 */
public class CassandraStorageTest {

    public CassandraConfig buildConfigProperties(){
        CassandraConfig configProperties=new CassandraConfig();
        return configProperties;
    }
    public static SkyeLogEvent buildEvent(){
        SkyeLogEvent event=new SkyeLogEvent();
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
        CassandraConfig configProperties=buildConfigProperties();
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
        SkyeLogEvent event=buildEvent();
        storage.save(event);
    }
}
