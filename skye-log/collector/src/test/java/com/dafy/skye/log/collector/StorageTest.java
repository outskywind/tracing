package com.dafy.skye.log.collector;

import com.dafy.skye.log.collector.storage.elasticsearch.ElasticSearchStorage;
import com.dafy.skye.log.collector.storage.elasticsearch.ElasticsearchStorageConfig;
import com.dafy.skye.log.collector.storage.entity.SkyeLogEntity;
import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
import com.dafy.skye.log.collector.storage.query.LogQueryResult;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/22.
 */
public class StorageTest {
    public ElasticsearchStorageConfig buildESConfig(){
        ElasticsearchStorageConfig config=new ElasticsearchStorageConfig();
        return config;
    }
    @Test
    public void testQuery() throws Exception{
        LogQueryRequest request=new LogQueryRequest();
        ElasticSearchStorage storage=new ElasticSearchStorage(buildESConfig());
        LogQueryResult entityList=storage.query(request);
    }
}
