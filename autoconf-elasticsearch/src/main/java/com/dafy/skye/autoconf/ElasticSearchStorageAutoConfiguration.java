package com.dafy.skye.autoconf;

import com.dafy.skye.storage.http.ElasticsearchHttpStorage;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.internal.V2StorageComponent;
import zipkin2.elasticsearch.ElasticsearchStorage;
import zipkin2.storage.StorageComponent;

/**
 * Created by quanchengyun on 2018/3/13.
 */
@Configuration
@ConditionalOnProperty(name = "zipkin.storage.type", havingValue = "elasticsearch")
@ConditionalOnMissingBean(StorageComponent.class)
public class ElasticSearchStorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    V2StorageComponent storage(ElasticsearchHttpStorage esStorage) {
        return V2StorageComponent.create(esStorage);
    }


    @Bean
    ElasticsearchHttpStorage elasticsearchHttpStorage(
            ZipkinElasticsearchTransportClientStorageProperties elasticsearch,
            @Qualifier("zipkinElasticsearchHttp") OkHttpClient client,
            @Value("${zipkin.query.lookback:86400000}") int namesLookback,
            @Value("${zipkin.storage.strict-trace-id:true}") boolean strictTraceId,
            @Value("${zipkin.storage.search-enabled:true}") boolean searchEnabled,
            @Value("${zipkin.storage.elastic.index-template:}") String indexTemplate){

        ElasticsearchStorage delegate = ElasticsearchStorage.newBuilder(client).strictTraceId(strictTraceId).searchEnabled(searchEnabled).namesLookback(namesLookback).build();

        ElasticsearchHttpStorage storage = new ElasticsearchHttpStorage(delegate,true,searchEnabled,indexTemplate);
        return storage;
    }


}
