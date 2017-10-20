package com.dafy.skye.druid.entity;

import com.dafy.skye.component.GranularitySimple;
import io.druid.query.aggregation.Aggregator;
import io.druid.query.aggregation.PostAggregator;

/**
 * Created by quanchengyun on 2017/9/21.
 */
public class QueryParam {

    private String dataSource;

    private String groupBy;

    private long startTimestamp;

    private long endTimestamp;

    private GranularitySimple granularity;

    private String metric;

    private Aggregator aggregator;

    private PostAggregator postAggregator;


    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public GranularitySimple getGranularity() {
        return granularity;
    }

    public void setGranularity(GranularitySimple granularity) {
        this.granularity = granularity;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Aggregator getAggregator() {
        return aggregator;
    }

    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
    }

    public PostAggregator getPostAggregator() {
        return postAggregator;
    }

    public void setPostAggregator(PostAggregator postAggregator) {
        this.postAggregator = postAggregator;
    }
}
