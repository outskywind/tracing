package com.dafy.skye.zipkin.extend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import zipkin.autoconfigure.storage.elasticsearch.http.ZipkinElasticsearchHttpStorageProperties;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/20.
 */
@Component
@ConfigurationProperties("zipkin-extend.elasticsearch")
public class ZipkinExtendESConfig {
    @Autowired
    private ZipkinElasticsearchHttpStorageProperties zipkinESStorageProperties;
    private List<String> transportHosts;
    private String type;
    private String clusterName;
    private Long defaultLookback=604800000L;
    public ZipkinElasticsearchHttpStorageProperties getZipkinESStorageProperties() {
        return zipkinESStorageProperties;
    }

    public void setZipkinESStorageProperties(ZipkinElasticsearchHttpStorageProperties zipkinESStorageProperties) {
        this.zipkinESStorageProperties = zipkinESStorageProperties;
    }

    public List<String> getTransportHosts() {
        return transportHosts;
    }

    public void setTransportHosts(List<String> transportHosts) {
        this.transportHosts = transportHosts;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Long getDefaultLookback() {
        return defaultLookback;
    }

    public void setDefaultLookback(Long defaultLookback) {
        this.defaultLookback = defaultLookback;
    }
}