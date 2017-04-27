package com.dafy.skye.log.collector.storage.elasticsearch;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class ElasticsearchConfig {
    private List<String> nodes= Lists.newArrayList("10.8.15.79:9200");
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
}
