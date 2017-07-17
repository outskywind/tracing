package com.dafy.skye.zipkin.extend.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.autoconfigure.storage.elasticsearch.http.ZipkinElasticsearchHttpStorageProperties;

import java.net.InetAddress;

/**
 * Created by quanchengyun on 2017/7/10.
 */
@Configuration
public class ESClientConfig {
    @Bean
    public TransportClient transportClient(ZipkinExtendESConfig zipkinExtendESConfig) throws Exception{
        Settings settings = Settings.builder()
                .put("cluster.name", zipkinExtendESConfig.getClusterName()).build();
        TransportClient transportClient= new PreBuiltTransportClient(settings);
        for(String host: zipkinExtendESConfig.getTransportHosts()){
            String[] array=host.split(":");
            transportClient
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName(array[0]), Integer.parseInt(array[1])));
        }
        return transportClient;
    }
}


