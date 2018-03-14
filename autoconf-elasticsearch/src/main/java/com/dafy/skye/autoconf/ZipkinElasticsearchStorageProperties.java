package com.dafy.skye.autoconf;

import com.dafy.skye.storage.ElasticsearchTransportClientStorage;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by quanchengyun on 2018/3/13.
 */
@ConfigurationProperties("zipkin.storage.elasticsearch")
public class ZipkinElasticsearchStorageProperties {

    /** Indicates the ingest pipeline used before spans are indexed. no default */
    private String pipeline;
    /** A List of base urls to connect to. Defaults to http://localhost:9300 */
    private List<String> hosts; // initialize to null to defer default to transport
    /** The index prefix to use when generating daily index names. Defaults to zipkin. */
    private String index = "zipkin";
    /** The date separator used to create the index name. Default to -. */
    private String dateSeparator = "-";
    /** Sets maximum in-flight requests from this process to any Elasticsearch host. Defaults to 64 */
    private int maxRequests = 64;
    /** Number of shards (horizontal scaling factor) per index. Defaults to 5. */
    private int indexShards = 5;
    /** Number of replicas (redundancy factor) per index. Defaults to 1.` */
    private int indexReplicas = 1;
    /** username used for basic auth. Needed when Shield or X-Pack security is enabled */
    private String username;
    /** password used for basic auth. Needed when Shield or X-Pack security is enabled */
    private String password;
    /** When true, Redundantly queries indexes made with pre v1.31 collectors. Defaults to true. */
    private boolean legacyReadsEnabled = true;
    /**
     * Controls the connect, read and write socket timeouts (in milliseconds) for Elasticsearch Api
     * requests. Defaults to 10000 (10 seconds)
     */
    private int timeout = 10_000;

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getDateSeparator() {
        return dateSeparator;
    }

    public void setDateSeparator(String dateSeparator) {
        this.dateSeparator = dateSeparator;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getIndexShards() {
        return indexShards;
    }

    public void setIndexShards(int indexShards) {
        this.indexShards = indexShards;
    }

    public int getIndexReplicas() {
        return indexReplicas;
    }

    public void setIndexReplicas(int indexReplicas) {
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

    public boolean isLegacyReadsEnabled() {
        return legacyReadsEnabled;
    }

    public void setLegacyReadsEnabled(boolean legacyReadsEnabled) {
        this.legacyReadsEnabled = legacyReadsEnabled;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
