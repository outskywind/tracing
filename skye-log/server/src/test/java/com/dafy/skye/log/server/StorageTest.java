package com.dafy.skye.log.server;

import com.dafy.skye.log.server.storage.elasticsearch.ElasticSearchStorage;
import com.dafy.skye.log.server.storage.elasticsearch.ElasticsearchStorageConfig;
import com.dafy.skye.log.server.storage.query.LogQueryRequest;
import com.dafy.skye.log.server.storage.query.LogQueryResult;
import org.junit.Test;

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
