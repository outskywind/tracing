package com.dafy.skye.qps.consumer;

import com.dafy.skye.qps.Qps;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.elasticsearch.ElasticsearchStorage;
import zipkin2.elasticsearch.internal.HttpBulkIndexer;
import zipkin2.elasticsearch.internal.IndexNameFormatter;
import zipkin2.elasticsearch.internal.client.HttpCall;

/**
 * Created by quanchengyun on 2018/5/8.
 */
public class BulkQpsIndexer {

     Logger log = LoggerFactory.getLogger(BulkQpsIndexer.class);
     HttpBulkIndexer indexer;
     IndexNameFormatter indexNameFormatter;

     ObjectMapper json = new ObjectMapper();

    public void setIndexNameFormatter(IndexNameFormatter indexNameFormatter) {
        this.indexNameFormatter = indexNameFormatter;
        this.json.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    public BulkQpsIndexer(ElasticsearchStorage es) {
        this.indexer = new HttpBulkIndexer("qps", es);
    }

    void add(long indexTimestamp, Qps qps) {
        String index = indexNameFormatter.formatTypeAndTimestamp(null, indexTimestamp);
        try{
            byte[] document = json.writeValueAsBytes(qps);
            String timestamp_s = String.valueOf(qps.getTimestamp()/1000);
            indexer.add(index, null, document, timestamp_s);
        }catch(Exception e){
            log.error("exception: ",e);
        }
    }

    HttpCall<Void> newCall() {
        return indexer.newCall();
    }
}
