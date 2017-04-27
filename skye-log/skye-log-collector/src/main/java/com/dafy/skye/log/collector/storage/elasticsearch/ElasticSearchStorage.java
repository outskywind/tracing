package com.dafy.skye.log.collector.storage.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.collector.storage.elasticsearch.domain.ESSkyeLogEntity;
import com.dafy.skye.log.collector.util.ResourceUtil;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class ElasticSearchStorage implements StorageComponent {
    private RestClient restClient;
    private ElasticsearchConfig elasticsearchConfig;
    private static final Logger log= LoggerFactory.getLogger(ElasticSearchStorage.class);
    public ElasticSearchStorage(ElasticsearchConfig elasticsearchConfig){
        this.elasticsearchConfig=elasticsearchConfig;
        HttpHost[] hosts=new HttpHost[elasticsearchConfig.getNodes().size()];
        int i=0;
        for(String address:elasticsearchConfig.getNodes()){
            String[] array=address.split(":");
            String host=array[0];
            int port=Integer.parseInt(array[1]);
            hosts[i]=new HttpHost(host,port);
            i++;
        }
        this.restClient=RestClient.builder(hosts).build();

    }
    @Override
    public void start() {
        final String indexName=elasticsearchConfig.getIndexName();
        final String typeName=elasticsearchConfig.getTypeName();

        try {
            Response existsIndexResp=restClient.performRequest("HEAD","/"+elasticsearchConfig.getIndexName());
            if(existsIndexResp.getStatusLine().getStatusCode()== HttpStatus.SC_NOT_FOUND){
                restClient.performRequest("PUT","/"+elasticsearchConfig.getIndexName());
            }
            Response existsMapping=restClient.performRequest("HEAD","/"+indexName+"/"+typeName);
            if(existsMapping.getStatusLine().getStatusCode()== HttpStatus.SC_NOT_FOUND){
                String createMappingStr= ResourceUtil.readString("es-mapping.json");
                HttpEntity entity=new NStringEntity(createMappingStr);
                Response putMappingResp=restClient.performRequest("PUT",
                        "/"+indexName+"/_mapping/"+typeName, Collections.EMPTY_MAP,entity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        try {
            restClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(SkyeLogEvent event) {
        final String index=elasticsearchConfig.getIndexName();
        final String type=elasticsearchConfig.getTypeName();
        String endpoint="/"+index+"/"+type;
        String txt=JSON.toJSONString(ESSkyeLogEntity.build(event));
        try {
            HttpEntity entity=new NStringEntity(txt);
            Response response=restClient.performRequest("POST",endpoint,Collections.EMPTY_MAP,entity);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void batchSave(Collection<SkyeLogEvent> events) {
        for(SkyeLogEvent event:events){
            save(event);
        }

    }
}
