package com.dafy.skye.zipkin.extend;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/14.
 */
@ConfigurationProperties("zipkin.storage.elasticsearch")
public class ZipkinStorageESConfig {
    private List<String> hosts;
    private String pipeline;
    private Integer maxRequests;
    private String dateSeparator;
    private Integer index;
    private String indexShards;
    private String indexReplicas;
    private String username;
    private String password;

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public Integer getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(Integer maxRequests) {
        this.maxRequests = maxRequests;
    }

    public String getDateSeparator() {
        return dateSeparator;
    }

    public void setDateSeparator(String dateSeparator) {
        this.dateSeparator = dateSeparator;
    }

    public String getIndexShards() {
        return indexShards;
    }

    public void setIndexShards(String indexShards) {
        this.indexShards = indexShards;
    }

    public String getIndexReplicas() {
        return indexReplicas;
    }

    public void setIndexReplicas(String indexReplicas) {
        this.indexReplicas = indexReplicas;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
