package com.dafy.skye.zipkin.config.druid;

import com.dafy.skye.zk.ZKConfigurationProperties;
import com.dafy.skye.zk.ZkListener;
import com.dafy.skye.zk.ZkServiceDiscovery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by quanchengyun on 2017/9/18.
 */
//be careful with @EnableAutoConfiguration ， only can be used in root Configuration class
@Configuration
@ConditionalOnProperty(prefix="druid.zk",name={"host","path"})
public class ZkConfiguration {

    @Bean
    @ConfigurationProperties(prefix="druid.zk",ignoreInvalidFields=true,exceptionIfInvalid=false)
    public ZKConfigurationProperties zkConfigurationProperties(){
        return  new ZKConfigurationProperties();
    }

    @Bean
    public CuratorFramework curatorFramework(ZKConfigurationProperties config){
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        CuratorFramework zkClient = builder.connectString(config.getHost())
                .sessionTimeoutMs(config.getSessionTimeouts())
                .connectionTimeoutMs(config.getConnectTimeouts())
                .canBeReadOnly(false)
                .retryPolicy(new ExponentialBackoffRetry(config.getRetryConnectIntervalMs(), 100))
                //.namespace(config.getNamespace()) 不能设置否则会导致无法获子路径数据
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
    @ConditionalOnMissingBean
    public PathChildrenCacheListener zkListener(ZkServiceDiscovery zkServiceDiscovery){
        ZkListener listener = new ZkListener();
        listener.setZkServiceDiscovery(zkServiceDiscovery);
        return listener;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper initJSONMapper(){
        return new ObjectMapper();
    }

    @Bean
    public ZkServiceDiscovery zkServiceDiscovery(ZKConfigurationProperties zkConfig,PathChildrenCache zkCache,ObjectMapper jsonMapper){
        ZkServiceDiscovery instance = new ZkServiceDiscovery();
        instance.setZkCache(zkCache);
        instance.setZkConfig(zkConfig);
        instance.setJsonMapper(jsonMapper);
        return instance;
    }

}
