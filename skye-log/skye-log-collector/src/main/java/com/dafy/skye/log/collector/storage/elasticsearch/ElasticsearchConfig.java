package com.dafy.skye.log.collector.storage.elasticsearch;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class ElasticsearchConfig {
    private List<String> nodes= Lists.newArrayList("10.8.15.79:9200");
    private String index ="skye";
    private String type ="skye-log";
    private Integer indexShards =5;
    private Integer indexReplicas =2;
    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
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
}
