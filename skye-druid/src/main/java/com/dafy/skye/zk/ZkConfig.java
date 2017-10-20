package com.dafy.skye.zk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by quanchengyun on 2017/9/18.
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(ZKConfigurationProperties.class)
public class ZkConfig {

    @Bean
    public CuratorFramework curatorFramework(ZKConfigurationProperties config){
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        CuratorFramework zkClient = builder.connectString(config.getHost())
                .sessionTimeoutMs(config.getSessionTimeouts())
                .connectionTimeoutMs(config.getConnectTimeouts())
                .canBeReadOnly(false)
                .retryPolicy(new ExponentialBackoffRetry(config.getRetryConnectIntervalMs(), Integer.MAX_VALUE))
                .namespace(config.getNamespace())
                .defaultData(null)
                .build();
        zkClient.start();
        return zkClient;
    }

    @Bean
    public PathChildrenCache pathChildrenCache(CuratorFramework zkClient,ZKConfigurationProperties zkConfig) throws Exception{
        PathChildrenCache cache= new PathChildrenCache(zkClient, zkConfig.getPath(), true);
        cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        return  cache;
    }

    @Bean
    public PathChildrenCacheListener zkListener(){
        ZkListener listener = new ZkListener();
        return listener;
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper initJSONMapper(){
        return new ObjectMapper();
    }

    /**
     * bean factory will inject the fields of this bean instance  created by this @Bean method
     * @return
     */
    @Bean
    public ZkSerivceDiscovery zkSerivceDiscovery(){
        ZkSerivceDiscovery instance = new ZkSerivceDiscovery();
        return instance;
    }




}
