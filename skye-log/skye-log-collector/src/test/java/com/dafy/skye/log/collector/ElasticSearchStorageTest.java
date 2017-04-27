package com.dafy.skye.log.collector;

import com.dafy.skye.log.collector.storage.cassandra.CassandraConfig;
import com.dafy.skye.log.collector.storage.cassandra.CassandraStorage;
import com.dafy.skye.log.collector.storage.elasticsearch.ElasticSearchStorage;
import com.dafy.skye.log.collector.storage.elasticsearch.ElasticsearchConfig;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import com.google.common.collect.Lists;
import org.junit.Test;

/**
 * Created by Caedmon on 2017/4/27.
 */
public class ElasticSearchStorageTest {
    public ElasticSearchStorage startStorage(){
        ElasticsearchConfig config=new ElasticsearchConfig();
        ElasticSearchStorage storage=new ElasticSearchStorage(config);
        storage.start();
        return storage;
    }
    @Test
    public void testBatchSave(){
        SkyeLogEvent event=StorageTestHelper.buildEvent();
        ElasticSearchStorage storage=startStorage();
        storage.batchSave(Lists.newArrayList(event));
    }
    @Test
    public void testSave(){
        SkyeLogEvent event=StorageTestHelper.buildEvent();
        ElasticSearchStorage storage=startStorage();
        storage.save(event);
    }
}
