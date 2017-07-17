package com.dafy.skye.server.auto.config;

import com.dafy.skye.zipkin.extend.config.ZipkinExtendESConfig;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendService;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendServiceImpl;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.autoconfigure.storage.elasticsearch.http.ZipkinElasticsearchHttpStorageProperties;

/**
 * Created by quanchengyun on 2017/7/17.
 */
@Configuration
//当配置为@ConfigurationProperties时，要被注入bean时，如果是第三方jar包的话，因为不被扫描到，所以只能指定class
@EnableConfigurationProperties({ZipkinExtendESConfig.class,ZipkinElasticsearchHttpStorageProperties.class})
public class ZipkinExtendAutoConfig {

    @Bean
    ZipkinExtendService zipkinExtendService(ZipkinExtendESConfig zipkinExtendESConfig,
                                            ZipkinElasticsearchHttpStorageProperties zipkinESStorageProperties,
                                            TransportClient transportClient){
        ZipkinExtendServiceImpl service = new ZipkinExtendServiceImpl();
        service.setTransportClient(transportClient);
        service.setZipkinESStorageProperties(zipkinESStorageProperties);
        service.setZipkinExtendESConfig(zipkinExtendESConfig);
        return service;
    }

}
