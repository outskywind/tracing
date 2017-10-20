package com.dafy.skye.zk;

/**
 * Created by quanchengyun on 2017/9/15.
 */

import com.dafy.skye.druid.DruidClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * 负责监听zk上broker节点列表，同步更新本地的broker列表
 */
public class ZkSerivceDiscovery {

    private static Logger LOG = LoggerFactory.getLogger(ZkSerivceDiscovery.class);
    //broker hosts
    Set<String> brokers = new HashSet<>();
    @Autowired
    ZKConfigurationProperties zkConfig;
    @Autowired
    PathChildrenCache zkCache;
    @Autowired
    @NotNull
    ObjectMapper jsonMapper;


    public String[] getBrokers(){
        if(brokers.isEmpty()){
            refresh();
        }
        return brokers.toArray(new String[0]);
    }

    public void refresh(){
        try{
            //从远程获取数据刷新本地缓存
            zkCache.rebuild();
            //从本地cache拿的
            List<ChildData> brokerList = zkCache.getCurrentData();
            brokers.clear();
            for(ChildData data:brokerList){
                try {
                    byte[] bytes = data.getData();
                    String broker = new String(bytes,"UTF-8");
                    Map<String,Object> retObj = jsonMapper.readValue(broker, new TypeReference<Map<String,Object>>(){});
                    String address = (String)retObj.get("address");
                    String port = Integer.toString((Integer)retObj.get("port"));
                    brokers.add(address+":"+port);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            LOG.error("rebuid zk cache failed.",e);
        }
        LOG.info("---brokers refresh---:{}",brokers);
        if(brokers.isEmpty()){
            LOG.error("no druid brokers found in zk connect:{}",zkConfig.getHost());
        }
    }

    public PathChildrenCache getZkCache() {
        return zkCache;
    }

    public void setZkCache(PathChildrenCache zkCache) {
        this.zkCache = zkCache;
    }

    public ZKConfigurationProperties getZkConfig() {
        return zkConfig;
    }

    public void setZkConfig(ZKConfigurationProperties zkConfig) {
        this.zkConfig = zkConfig;
    }
}
