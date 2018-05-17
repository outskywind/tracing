package com.dafy.skye.zipkin.extend.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by quanchengyun on 2017/7/10.
 */
@Configuration
@EnableConfigurationProperties({ZipkinExtendESConfigurationProperties.class})
public class ESClientConfig {
    @Bean
    public TransportClient transportClient(ZipkinExtendESConfigurationProperties zipkinExtendESConfigurationProperties) throws Exception{
        Settings settings = Settings.builder()
                .put("cluster.name", zipkinExtendESConfigurationProperties.getClusterName()).build();
        TransportClient transportClient= new PreBuiltTransportClient(settings);
        for(String host: zipkinExtendESConfigurationProperties.getTransportHosts()){
            String[] array=host.split(":");
            transportClient
                    .addTransportAddress(new TransportAddress(
                            InetAddress.getByName(array[0]), Integer.parseInt(array[1])));
        }
        return transportClient;
    }


    @Bean
    public RestHighLevelClient restClient(ZipkinExtendESConfigurationProperties zipkinExtendESConfigurationProperties){
        List<HttpHost> httpHostList = new ArrayList<>();
        for(String host: zipkinExtendESConfigurationProperties.getRestHosts()){
            String[] array=host.split(":");
            HttpHost httpHost = new HttpHost(array[0],Integer.parseInt(array[1]));
            httpHostList.add(httpHost);
        }
        RestClientBuilder builder =  RestClient.builder(httpHostList.toArray(new HttpHost[0]));
        return new RestHighLevelClient(builder);
    }


}


