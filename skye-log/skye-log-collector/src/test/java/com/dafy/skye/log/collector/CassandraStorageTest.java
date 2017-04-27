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
        SkyeLogEvent event=StorageTestHelper.buildEvent();
        storage.save(event);
    }
}
