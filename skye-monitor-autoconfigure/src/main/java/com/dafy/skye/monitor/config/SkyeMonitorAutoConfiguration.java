package com.dafy.skye.monitor.config;

import com.dafy.skye.zipkin.config.elasticsearch.ZipkinElasticsearchStorageProperties;
import com.dafy.skye.zipkin.extend.config.ZipkinExtendESConfigurationProperties;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendService;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendServiceImpl;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Created by quanchengyun on 2017/7/17.
 */
@Configuration
//当配置为@ConfigurationProperties时，要被注入bean时，如果是第三方jar包的话，因为不被扫描到，所以只能指定class
@EnableConfigurationProperties({ZipkinExtendESConfigurationProperties.class})
@ComponentScan(basePackages = {"com.dafy.skye.zipkin.extend"},excludeFilters = @ComponentScan.Filter(type= FilterType.REGEX,pattern="com.dafy.skye.zipkin.extend.SkyeMonitorApplication"))
public class SkyeMonitorAutoConfiguration {

    @Bean
    ZipkinExtendService zipkinExtendService(ZipkinExtendESConfigurationProperties zipkinExtendESConfigurationProperties,
                                            ZipkinElasticsearchStorageProperties zipkinESStorageProperties,
                                            TransportClient transportClient){
        ZipkinExtendServiceImpl service = new ZipkinExtendServiceImpl();
        service.setTransportClient(transportClient);
        service.setZipkinESStorageProperties(zipkinESStorageProperties);
        service.setZipkinExtendESConfigurationProperties(zipkinExtendESConfigurationProperties);
        return service;
    }

}
