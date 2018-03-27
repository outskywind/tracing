package com.dafy.skye.autoconf;

import com.dafy.skye.storage.http.ElasticsearchHttpStorage;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import zipkin.internal.V2StorageComponent;
import zipkin2.elasticsearch.ElasticsearchStorage;
import zipkin2.storage.StorageComponent;

import java.util.concurrent.TimeUnit;

/**
 * Created by quanchengyun on 2018/3/13.
 */
@Configuration
@ConditionalOnProperty(name = "zipkin.storage.type", havingValue = "elasticsearch")
@ConditionalOnMissingBean(StorageComponent.class)
public class ElasticSearchStorageAutoConfiguration {

    @Autowired(required = false)
    @Qualifier("zipkinElasticsearchHttp")
    OkHttpClient.Builder elasticsearchOkHttpClientBuilder;

    @Bean
    @ConditionalOnMissingBean
    V2StorageComponent storage(ElasticsearchHttpStorage esStorage) {
        return V2StorageComponent.create(esStorage);
    }


    @Bean
    @Qualifier("zipkinElasticsearchHttp")
    @ConditionalOnMissingBean
    OkHttpClient elasticsearchOkHttpClient(
            @Value("${zipkin.storage.elasticsearch.timeout:10000}") int timeout
    ) {
        OkHttpClient.Builder builder = elasticsearchOkHttpClientBuilder != null
                ? elasticsearchOkHttpClientBuilder
                : new OkHttpClient.Builder();
        // 定制http拦截器 不过这种很不友好，不能在aop层面统一拦截处理
        //for (Interceptor interceptor : networkInterceptors) {
        //    builder.addNetworkInterceptor(interceptor);
        //}
        builder.connectTimeout(timeout, TimeUnit.MILLISECONDS);
        builder.readTimeout(timeout, TimeUnit.MILLISECONDS);
        builder.writeTimeout(timeout, TimeUnit.MILLISECONDS);
        return builder.build();
    }


    @Bean
    ElasticsearchHttpStorage elasticsearchHttpStorage(
            ZipkinElasticsearchStorageProperties elasticsearch,
            @Qualifier("zipkinElasticsearchHttp") OkHttpClient client,
            @Value("${zipkin.query.lookback:86400000}") int namesLookback,
            @Value("${zipkin.storage.strict-trace-id:true}") boolean strictTraceId,
            @Value("${zipkin.storage.search-enabled:true}") boolean searchEnabled,
            @Value("${zipkin.storage.elasticsearch.index-template}") String indexTemplate){

        ElasticsearchStorage delegate = ElasticsearchStorage.newBuilder(client).hosts(elasticsearch.getHosts())
                .strictTraceId(strictTraceId).searchEnabled(searchEnabled).namesLookback(namesLookback).build();
        ElasticsearchHttpStorage storage = new ElasticsearchHttpStorage(delegate,true,searchEnabled,indexTemplate);
        return storage;
    }


}
