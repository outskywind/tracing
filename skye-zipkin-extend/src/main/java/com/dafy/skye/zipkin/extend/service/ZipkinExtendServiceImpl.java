package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.common.elasticsearch.DefaultOptions;
import com.dafy.skye.common.util.IndexNameFormatter;
import com.dafy.skye.common.util.IntervalTimeUnit;
import com.dafy.skye.common.util.TimestampRange;
import com.dafy.skye.zipkin.extend.config.ZipkinExtendESConfig;
import com.dafy.skye.zipkin.extend.dto.*;
import com.google.common.base.Strings;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.InternalStatsBucket;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.StatsBucket;
import org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.StatsBucketPipelineAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import zipkin.Codec;
import zipkin.Span;
import zipkin.internal.GroupByTraceId;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by Caedmon on 2017/6/15.
 */
@Service
public class ZipkinExtendServiceImpl implements ZipkinExtendService {
    @Autowired
    private ZipkinExtendESConfig zipkinExtendESConfig;
    private IndexNameFormatter indexNameFormatter;
    private TransportClient transportClient;
    @PostConstruct
    public void init() throws UnknownHostException{
        Settings settings = Settings.builder()
                .put("cluster.name", zipkinExtendESConfig.getClusterName()).build();
        this.transportClient= new PreBuiltTransportClient(settings);
        for(String host: zipkinExtendESConfig.getTransportHosts()){
            String[] array=host.split(":");
            this.transportClient
                        .addTransportAddress(new InetSocketTransportAddress(
                                InetAddress.getByName(array[0]), Integer.parseInt(array[1])));
        }
        IndexNameFormatter.Builder formatterBuilder=IndexNameFormatter.builder();
        formatterBuilder.dateSeparator('-');
        final String index=zipkinExtendESConfig.getZipkinESStorageProperties().getIndex();
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
    void checkAndSetDefault(TraceQueryRequest request){
        if(request.interval==null){
            request.interval=100;
        }
        if(request.intervalUnit==null){
            request.intervalUnit= IntervalTimeUnit.HOUR;
        }
    }

    @Override
    public Set<String> getSpans(SpanNameQueryRequest request) {
        String[] indices=indices(request.endTs,request.lookback);
        SearchRequestBuilder esRequest=transportClient.prepareSearch(indices)
                .setIndicesOptions(DefaultOptions.defaultIndicesOptions());
        BoolQueryBuilder root=new BoolQueryBuilder();
        root.filter(QueryBuilders.rangeQuery("timestamp_millis")
                .gt(request.endTs-request.lookback).lte(request.endTs));
        if(!CollectionUtils.isEmpty(request.services)){
            root.filter(QueryBuilders.nestedQuery("annotations",
                    QueryBuilders.termsQuery("annotations.endpoint.serviceName",request.services),
                    ScoreMode.None));
        }
        esRequest.addAggregation(AggregationBuilders.terms("span_names").field("name"));
        SearchResponse response=esRequest.execute().actionGet();
        Terms terms=response.getAggregations().get("span_names");
        Set<String> spanNames=new HashSet<>();
        for(Terms.Bucket bucket:terms.getBuckets()){
            spanNames.add(bucket.getKeyAsString());
        }
        return spanNames;
    }

    public Set<String> getServices(ServiceNameQueryRequest request){
        request.checkAndSetDefault();
        String[] indices=indices(request.endTs,request.lookback);
        SearchRequestBuilder searchRequestBuilder=transportClient.prepareSearch(indices)
                .setIndicesOptions(DefaultOptions.defaultIndicesOptions());
        BoolQueryBuilder rootQueryBuilder=new BoolQueryBuilder();
        searchRequestBuilder.addAggregation(AggregationBuilders
                .nested("binaryAnnotations","binaryAnnotations")
                .subAggregation(AggregationBuilders.terms("service_names")
                        .field("binaryAnnotations.endpoint.serviceName")));
        searchRequestBuilder.addAggregation(AggregationBuilders
                .nested("annotations","annotations")
                .subAggregation(AggregationBuilders.terms("service_names")
                        .field("annotations.endpoint.serviceName")));
        rootQueryBuilder.filter(timeRangeQuery(request.endTs,request.lookback));
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
    public TraceQueryResult getTraces(TraceQueryRequest request){
        SearchRequestBuilder search= searchRequestBuilder(request);
        AggregationBuilders.terms("trace_terms").size(request.limit);
        SearchResponse response=search.execute().actionGet();
        SearchHit[] hits=response.getHits().getHits();
        List<Span> spans=new LinkedList<>();
        if(hits!=null){
            for(SearchHit hit:hits){
                String source=hit.getSourceAsString();
                Span span=Codec.JSON.readSpan(source.getBytes());
                spans.add(span);
            }
        }
        List<List<Span>> traces=GroupByTraceId.apply(spans,false,true);
        TraceQueryResult result=new TraceQueryResult();
        result.setTook(response.getTook().seconds());
        result.setError(response.getShardFailures().toString());
        result.setTraces(traces);
        result.setSuccess(true);
        return result;
    }
    public TraceMetricsResult getTracesMetrics(TraceQueryRequest request){
        checkAndSetDefault(request);
        MultiSearchRequestBuilder multiSearch=transportClient.prepareMultiSearch()
                .setIndicesOptions(DefaultOptions.defaultIndicesOptions());
        long itemStartTs=request.endTs-request.lookback;
        long itemEndTs;
        List<TimestampRange> ranges=new ArrayList<>();
        do {
            final long itemLookback=request.interval*request.intervalUnit.getMills();
            itemEndTs=itemStartTs+itemLookback;
            TraceQueryRequest.Builder builder=request.newBuilder(request);
            builder.endTs(itemStartTs+itemLookback).lookback(itemLookback);
            SearchRequestBuilder requestBuilder= searchRequestBuilder(builder.build());
            requestBuilder.setSize(0);
            requestBuilder.addAggregation(AggregationBuilders.terms("trace_terms")
                    .field("traceId")
                    .subAggregation(AggregationBuilders.max("trace_duration")
                            .field("duration")));
            requestBuilder.addAggregation(PipelineAggregatorBuilders.
                    statsBucket("trace_duration_stats","trace_terms>trace_duration"));
            multiSearch.add(requestBuilder);
            ranges.add(new TimestampRange(itemStartTs,itemEndTs));
            itemStartTs=itemEndTs;
        }while (itemEndTs<=request.endTs);
        MultiSearchResponse response=multiSearch.execute().actionGet();
        MultiSearchResponse.Item[] items=response.getResponses();
        List<TraceMetricsResult.TraceMetrics> stats=new ArrayList<>(items.length);
        long totalTook=0;
        for(int i=0;i<items.length;i++){
            MultiSearchResponse.Item item=items[i];
            TraceMetricsResult.TraceMetrics.Builder statsBuilder= TraceMetricsResult.TraceMetrics.newBuilder();
            statsBuilder.startTs(ranges.get(i).startTs)
                    .endTs(ranges.get(i).endTs);
            SearchResponse itemResponse=item.getResponse();
            //响应时间
            if(itemResponse==null||itemResponse.getTook()==null){
                System.out.println(itemResponse);
            }
            long itemTook=itemResponse.getTook().getMillis();
            totalTook+=itemTook;
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
        TraceMetricsResult result=new TraceMetricsResult();
        result.setMetrics(stats);
        result.setTook(totalTook);
        return result;
    }

    @Override
    public SpanMetricsResult getSpansMetrics(TraceQueryRequest request) {
        TraceQueryRequest.Builder builder=request.newBuilder(request);
        SearchRequestBuilder requestBuilder= searchRequestBuilder(builder.build());
        requestBuilder.setSize(0);
        TermsAggregationBuilder spanNameTerms=AggregationBuilders.terms("span_name_terms")
                .field("name");
        TermsAggregationBuilder spanIdTerms=AggregationBuilders.terms("span_id_terms")
                .field("id");
        spanNameTerms.subAggregation(spanIdTerms);
        AggregationBuilder spanDuration=AggregationBuilders.max("span_duration").field("duration");
        spanIdTerms.subAggregation(spanDuration);
        spanIdTerms.order(Terms.Order.aggregation("span_duration","value",false));
//            spanIdTerms.order(Terms.Order.aggregation("span_duration","value",false));
        StatsBucketPipelineAggregationBuilder spanNameStats=PipelineAggregatorBuilders
                .statsBucket("span_name_stats","span_id_terms>span_duration");
        spanNameTerms.subAggregation(spanNameStats);
        requestBuilder.addAggregation(spanNameTerms);

        SearchResponse searchResponse=requestBuilder.execute().actionGet();
        //响应时间
        long took=searchResponse.getTook().getMillis();
        Aggregations aggregations=searchResponse.getAggregations();
        TreeSet<SpanMetricsResult.SpanMetrics> stats=new TreeSet<>();
        if(aggregations!=null){
            StringTerms spanTermsAgg=aggregations.get("span_name_terms");
            List<Terms.Bucket> termsBuckets=spanTermsAgg.getBuckets();
            for(Terms.Bucket termsBucket:termsBuckets){
                SpanMetricsResult.SpanMetrics.Builder spanMetrics=SpanMetricsResult.SpanMetrics.newBuilder();
                String spanName=termsBucket.getKeyAsString();
                spanMetrics.spanName(spanName);
                StatsBucket statsBucket=termsBucket.getAggregations().get("span_name_stats");
                spanMetrics.avgDuration(statsBucket.getAvg())
                        .maxDuration(statsBucket.getMax())
                        .minDuration(statsBucket.getMin())
                        .sumDuration(statsBucket.getSum())
                        .count(statsBucket.getCount());
                stats.add(spanMetrics.build());
            }
        }
        SpanMetricsResult result=new SpanMetricsResult();
        result.setMetrics(stats);
        result.setTook(took);
        return result;
    }

    SearchRequestBuilder searchRequestBuilder(TraceQueryRequest request){
        checkAndSetDefault(request);
        String[] indices=indices(request.endTs,request.lookback);
        SearchRequestBuilder requestBuilder=transportClient.prepareSearch(indices)
                .setIndicesOptions(DefaultOptions.defaultIndicesOptions());
        BoolQueryBuilder root=QueryBuilders.boolQuery();
        root.filter(QueryBuilders.rangeQuery("timestamp_millis")
                .gt(request.endTs-request.lookback).lte(request.endTs));
        if(!CollectionUtils.isEmpty(request.spans)){
            root.filter(QueryBuilders.termsQuery("name",request.spans));
        }
        if(!CollectionUtils.isEmpty(request.services)){
            root.filter(QueryBuilders.nestedQuery("annotations",
                    QueryBuilders.termsQuery("annotations.endpoint.serviceName",request.services),
                    ScoreMode.None));
            root.filter(QueryBuilders.nestedQuery("binaryAnnotations",
                    QueryBuilders.termsQuery("binaryAnnotations.endpoint.serviceName",request.services),
                    ScoreMode.None));
        }
        if(request.minDuration!=null){
            root.filter(QueryBuilders.rangeQuery("duration").gte(request.minDuration));
        }
        if(request.maxDuration!=null){
            root.filter(QueryBuilders.rangeQuery("duration").lte(request.maxDuration));
        }
        if(!Strings.isNullOrEmpty(request.sortField)&&!Strings.isNullOrEmpty(request.sortField)){
            requestBuilder.addSort(request.sortField,SortOrder.fromString(request.sortOrder));
        }
        requestBuilder.setSize(request.limit);
        requestBuilder.setQuery(root);

        return requestBuilder;
    }

}
