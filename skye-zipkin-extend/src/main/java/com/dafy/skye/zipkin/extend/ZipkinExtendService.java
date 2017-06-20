package com.dafy.skye.zipkin.extend;

import com.dafy.skye.common.elasticsearch.ESearchResponse;
import com.dafy.skye.common.util.IndexNameFormatter;
import com.dafy.skye.common.util.TimestampRange;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.InternalStatsBucket;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import zipkin.Span;
import zipkin.autoconfigure.storage.elasticsearch.http.ZipkinElasticsearchHttpStorageProperties;
import zipkin.storage.SpanStore;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Caedmon on 2017/6/15.
 */
@Service
public class ZipkinExtendService {
    @Autowired
    private ZipkinElasticsearchHttpStorageProperties elasticsearchHttpStorageProperties;
    private IndexNameFormatter indexNameFormatter;
    private TransportClient transportClient;
    private SpanStore spanStore;
    @PostConstruct
    public void init() throws UnknownHostException{
        Settings settings = Settings.builder()
                .put("cluster.name", "skye").build();
        this.transportClient= new PreBuiltTransportClient(settings);
        for(String host: elasticsearchHttpStorageProperties.getHosts()){
            this.transportClient
                        .addTransportAddress(new InetSocketTransportAddress(
                                InetAddress.getByName("10.8.15.79"), 9300));
        }
        IndexNameFormatter.Builder formatterBuilder=IndexNameFormatter.builder();
        formatterBuilder.dateSeparator('-');
        final String index=elasticsearchHttpStorageProperties.getIndex();
        this.indexNameFormatter=formatterBuilder.index(index).build();
    }
    String[] indices(long endTs,long lookup){
        List<String> indices = indexNameFormatter.indexNamePatternsForRange(endTs-lookup, endTs);
        String[] result=new String[indices.size()];
        indices.toArray(result);
        return result;
    }
    static QueryBuilder timeRangeQuery(long endTs,long lookback){
        QueryBuilder timeRangeQuery= QueryBuilders.rangeQuery("timestamp_millis")
                .from(endTs-lookback).to(endTs);
        return timeRangeQuery;
    }
    public Set<String> getServiceNames(long endTs,long lookback){
        String[] indices=indices(endTs,lookback);
        SearchRequestBuilder searchRequestBuilder=transportClient.prepareSearch(indices)
                .setIndicesOptions(defaultIndicesOptions());
        BoolQueryBuilder rootQueryBuilder=new BoolQueryBuilder();
        searchRequestBuilder.addAggregation(AggregationBuilders
                .nested("binaryAnnotations","binaryAnnotations")
                .subAggregation(AggregationBuilders.terms("service_names")
                        .field("binaryAnnotations.endpoint.serviceName")));
        searchRequestBuilder.addAggregation(AggregationBuilders
                .nested("annotations","annotations")
                .subAggregation(AggregationBuilders.terms("service_names")
                        .field("annotations.endpoint.serviceName")));
        rootQueryBuilder.filter(timeRangeQuery(endTs,lookback));
        SearchResponse response=searchRequestBuilder.execute().actionGet();
        InternalNested binaryAggregationNested=response.getAggregations().get("binaryAnnotations");
        InternalNested annotationsNested=response.getAggregations().get("annotations");
        Terms binaryTerms=binaryAggregationNested.getAggregations().get("service_names");
        Terms terms=annotationsNested.getAggregations().get("service_names");
        Set<String> set=new HashSet<>();
        List<Terms.Bucket> binaryTermsBuckets=binaryTerms.getBuckets();
        for(Terms.Bucket bucket:binaryTermsBuckets){
            set.add(bucket.getKeyAsString());
        }
        List<Terms.Bucket> termsBucket=terms.getBuckets();
        for(Terms.Bucket bucket:termsBucket){
            set.add(bucket.getKeyAsString());
        }
        return set;
    }
    ESearchResponse<List<List<Span>>> getTraces(TraceQueryRequest request){
        SearchRequestBuilder search=shareQueryBuilder(request);
        search.addSort("timestamp_millis", SortOrder.DESC);
        AggregationBuilders.terms("traceId").size(request.limit);
        return null;
    }
    TraceQueryResult getTraceStats(TraceQueryRequest request){
        MultiSearchRequestBuilder multiSearch=transportClient.prepareMultiSearch()
                .setIndicesOptions(defaultIndicesOptions());
        long itemStartTs=request.endTs-request.lookback;
        long itemEndTs;
        List<TimestampRange> rangs=new ArrayList<>();

        do {
            final long itemLookback=request.interval*request.intervalUnit.getMills();
            itemEndTs=itemStartTs+itemLookback;
            TraceQueryRequest.Builder builder=request.newBuilder(request);
            builder.endTs(itemStartTs+itemLookback).lookback(itemLookback);
            SearchRequestBuilder requestBuilder=shareQueryBuilder(builder.build());
            requestBuilder.addAggregation(AggregationBuilders.terms("trace_terms")
                    .field("traceId")
                    .subAggregation(AggregationBuilders.max("trace_duration")
                            .field("duration")));
            requestBuilder.addAggregation(PipelineAggregatorBuilders.
                    statsBucket("trace_duration_stats","trace_terms>trace_duration"));
            multiSearch.add(requestBuilder);
            rangs.add(new TimestampRange(itemStartTs,itemEndTs));
            itemStartTs=itemEndTs;
        }while (itemEndTs<=request.endTs);
        MultiSearchResponse response=multiSearch.execute().actionGet();
        MultiSearchResponse.Item[] items=response.getResponses();
        List<TraceIntervalStats> stats=new ArrayList<>(items.length);
        for(int i=0;i<items.length;i++){
            MultiSearchResponse.Item item=items[i];
            TraceIntervalStats.Builder statsBuilder=TraceIntervalStats.newBuilder();
            statsBuilder.startTs(rangs.get(i).startTs)
                    .endTs(rangs.get(i).endTs);
            SearchResponse itemResponse=item.getResponse();
            itemResponse.getTook();
            Aggregations aggregations=itemResponse.getAggregations();
            if(aggregations!=null){
                InternalStatsBucket statsBucket=aggregations.get("trace_duration_stats");
                statsBuilder.avgDuration(statsBucket.getAvg())
                        .maxDuration(statsBucket.getMax())
                        .minDuration(statsBucket.getMin())
                        .count(statsBucket.getCount());
            }else{
                statsBuilder.avgDuration(0).maxDuration(0)
                        .minDuration(0).count(0);
            }
            stats.add(statsBuilder.build());
        }
        TraceQueryResult result=new TraceQueryResult();
        result.setStats(stats);
//        result.setTraces();
        return result;
    }
    SearchRequestBuilder shareQueryBuilder(TraceQueryRequest request){
        String[] indices=indices(request.endTs,request.lookback);
        SearchRequestBuilder requestBuilder=transportClient.prepareSearch(indices)
                .setIndicesOptions(defaultIndicesOptions());
        BoolQueryBuilder root=QueryBuilders.boolQuery();
        root.filter(QueryBuilders.rangeQuery("timestamp_millis")
                .gt(request.endTs-request.lookback).lte(request.endTs));
        if(!CollectionUtils.isEmpty(request.spanNames)){
            root.filter(QueryBuilders.termsQuery("name",request.spanNames));
        }
        if(!CollectionUtils.isEmpty(request.serviceNames)){
            root.filter(QueryBuilders.nestedQuery("annotations",
                    QueryBuilders.termsQuery("annotations.endpoint.serviceName",request.serviceNames),
                    ScoreMode.None));
            root.filter(QueryBuilders.nestedQuery("binaryAnnotations",
                    QueryBuilders.termsQuery("binaryAnnotations.endpoint.serviceName",request.serviceNames),
                    ScoreMode.None));
        }
        if(request.minDuration!=null){
            root.filter(QueryBuilders.rangeQuery("duration").gte(request.minDuration));
        }
        if(request.maxDuration!=null){
            root.filter(QueryBuilders.rangeQuery("duration").lte(request.maxDuration));
        }
        return requestBuilder;
    }
    static IndicesOptions defaultIndicesOptions(){
        return IndicesOptions.fromOptions(true,true,true,true);
    }
}
