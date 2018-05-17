package com.dafy.skye.zipkin.extend.query;

/**
 * Created by quanchengyun on 2018/5/10.
 */
public enum ElasticsearchMetric {

    qps("avg_count"),lantency("duration"),success_rate("tags.status");

    String value;

    ElasticsearchMetric(String value){
        this.value=value;
    }
}
