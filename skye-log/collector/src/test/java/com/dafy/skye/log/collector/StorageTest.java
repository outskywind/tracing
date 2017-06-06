package com.dafy.skye.log.collector;

import com.dafy.skye.log.collector.storage.elasticsearch.ElasticSearchStorage;
import com.dafy.skye.log.collector.storage.elasticsearch.ElasticsearchStorageConfig;
import com.dafy.skye.log.collector.storage.entity.SkyeLogEntity;
import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
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
        String start="2016-01-01 00:00:00";
        String end="2018-01-01 00:00:00";
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate=format.parse(start);
        Date endDate=format.parse(end);
        LogQueryRequest request=new LogQueryRequest();
        request.setStartTs(startDate);
        request.setEndTs(endDate);
        ElasticSearchStorage storage=new ElasticSearchStorage(buildESConfig());
        List<SkyeLogEntity> entityList=storage.query(request);
    }
}
