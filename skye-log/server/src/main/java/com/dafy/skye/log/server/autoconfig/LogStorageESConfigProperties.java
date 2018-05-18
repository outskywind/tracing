package com.dafy.skye.log.server.autoconfig;

import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/26.
 */
@ConfigurationProperties("skye.log.server.storage.elasticsearch")
public class LogStorageESConfigProperties {
    //ES节点地址
    private List<String> transportHosts ;
    //索引前缀,实际是按天存储
    private String index ="skye";
    private String type ="skye-log";
    private boolean ensureIndex=true;
    private Integer indexShards=3;
    private Integer indexReplicas=1;
    private Long defaultLookback=86400000L;
    private String clusterName="skye";
    public List<String> getTransportHosts() {
        return transportHosts;
    }

    public void setTransportHosts(List<String> transportHosts) {
        this.transportHosts = transportHosts;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnsureIndex() {
        return ensureIndex;
    }

    public void setEnsureIndex(boolean ensureIndex) {
        this.ensureIndex = ensureIndex;
    }

    public Integer getIndexShards() {
        return indexShards;
    }

    public void setIndexShards(Integer indexShards) {
        this.indexShards = indexShards;
    }

    public Integer getIndexReplicas() {
        return indexReplicas;
    }

    public void setIndexReplicas(Integer indexReplicas) {
        this.indexReplicas = indexReplicas;
    }

    public Long getDefaultLookback() {
        return defaultLookback;
    }

    public void setDefaultLookback(Long defaultLookback) {
        this.defaultLookback = defaultLookback;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
