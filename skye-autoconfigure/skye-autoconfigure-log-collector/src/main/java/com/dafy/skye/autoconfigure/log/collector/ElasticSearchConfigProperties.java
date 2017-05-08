package com.dafy.skye.autoconfigure.log.collector;

import com.dafy.skye.log.collector.storage.elasticsearch.ElasticsearchStorageConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/26.
 */
@ConfigurationProperties("skye.log.collector.storage.elasticsearch")
public class ElasticSearchConfigProperties {
    private List<String> nodes;
    private String indexName="skye";
    private String typeName="skye-log";
    private boolean ensureIndex=true;
    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isEnsureIndex() {
        return ensureIndex;
    }

    public void setEnsureIndex(boolean ensureIndex) {
        this.ensureIndex = ensureIndex;
    }
    public ElasticsearchStorageConfig build(){
        ElasticsearchStorageConfig config=new ElasticsearchStorageConfig();
        config.setNodes(this.nodes);
        config.setIndex(this.indexName);
        config.setType(this.typeName);
        return config;
    }
}
