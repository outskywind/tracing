package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.common.elasticsearch.DefaultOptions;
import com.dafy.skye.common.util.IndexNameFormatter;
import com.dafy.skye.common.util.IntervalTimeUnit;
import com.dafy.skye.common.util.TimestampRange;
import com.dafy.skye.zipkin.extend.config.ZipkinExtendESConfig;
import com.dafy.skye.zipkin.extend.dto.*;
import com.dafy.skye.zipkin.extend.util.TimeUtil;
import com.google.common.base.Strings;
import io.netty.util.internal.StringUtil;
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
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filters.FiltersAggregator;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
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
import org.springframework.util.StringUtils;
import zipkin.Codec;
import zipkin.Span;
import zipkin.autoconfigure.storage.elasticsearch.http.ZipkinElasticsearchHttpStorageProperties;
import zipkin.internal.GroupByTraceId;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Caedmon on 2017/6/15.
 */
@Service
public class ZipkinExtendServiceImpl implements ZipkinExtendService {
    @Autowired
    private ZipkinExtendESConfig zipkinExtendESConfig;
    @Autowired
    private ZipkinElasticsearchHttpStorageProperties zipkinESStorageProperties;

    private IndexNameFormatter indexNameFormatter;
    @Autowired
    private TransportClient transportClient;
    @PostConstruct
    public void init() throws UnknownHostException{
        IndexNameFormatter.Builder formatterBuilder=IndexNameFormatter.builder();
        formatterBuilder.dateSeparator('-');
        final String index=zipkinESStorageProperties.getIndex();
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
            request.interval=1;
        }
        if(request.intervalUnit==null){
            request.intervalUnit= IntervalTimeUnit.HOUR;
        }
    }

    @Override
    public Set<String> getSpans(SpanNameQueryRequest request) {
        request.checkAndSetDefault();
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
        esRequest.setQuery(root);
        esRequest.addAggregation(AggregationBuilders.terms("span_names").field("name"));
        SearchResponse response=esRequest.execute().actionGet();
        //
        if(response.getAggregations()==null){
            return null;
        }
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
        //
        if(response.getAggregations()==null){
            return null;
        }
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
        //AggregationBuilders.terms("trace_terms").size(10000);
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
            requestBuilder.addAggregation(AggregationBuilders.terms("trace_terms").size(zipkinExtendESConfig.getBucketsSize())
                    .field("traceId")
                    .subAggregation(AggregationBuilders.max("trace_duration")
                            .field("duration")));
                    //成功和异常的统计,在es里是key，value列表 嵌套聚合太多有性能问题，es查询要1分钟多
                    //.subAggregation(AggregationBuilders.nested("ba","binaryAnnotations").subAggregation(AggregationBuilders.terms("call_status").size(zipkinExtendESConfig.getBucketsSize()).field("binaryAnnotations.key"))
                    //                .subAggregation(AggregationBuilders.terms("call_result").size(zipkinExtendESConfig.getBucketsSize()).field("binaryAnnotations.value"))));
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
                continue;
            }
            long itemTook=itemResponse.getTook().getMillis();
            totalTook+=itemTook;
            Aggregations aggregations=itemResponse.getAggregations();
            //tracscount,这3个统计值返回到前端列表
            long tracesCount = 0;
            long exceptionsCount = 0;
            long successCount = 0;
            if(aggregations!=null){
                InternalStatsBucket statsBucket=aggregations.get("trace_duration_stats");
                if(statsBucket.getMax()<=0){
                    //System.out.println();
                }
                statsBuilder.maxDuration(TimeUtil.microToMills(statsBucket.getMax()))
                        .minDuration(TimeUtil.microToMills(statsBucket.getMin()))
                        .count(statsBucket.getCount());
                if(statsBucket.getSum()==0){
                    statsBuilder.avgDuration(0);
                }else {
                    statsBuilder.avgDuration(TimeUtil.microToMills(statsBucket.getAvg()));
                }
                //调用成功，异常统计
                /*Terms traces = aggregations.get("trace_terms");
                //tracscount,这3个统计值返回到前端列表
                tracesCount = traces.getBuckets().size();
                //----
                for(int ik=0;ik<tracesCount;ik++){
                    Terms.Bucket bucket  = traces.getBuckets().get(ik);
                    //System.out.print(bucket.getKey());
                    //这里 cscr 与 ss sr 因为已经按照service区分了，所以这2个span不会在一个桶
                    //可以认为 一个桶里的文档数就是span数
                    long spans_in_trace = bucket.getDocCount();
                    Nested ba = bucket.getAggregations().get("ba");
                    //处理Exceptiontong统计
                    Terms call_status_in_traces  =  ba.getAggregations().get("call_status");
                    int exception_for_the_trace = 0;
                    int successCount_for_the_trace = 0;
                    int adjust_result_sssr =  0;
                    if(call_status_in_traces.getBuckets()!=null){
                        for(Terms.Bucket bucket1:call_status_in_traces.getBuckets()){
                            String status = (String)bucket1.getKey();
                            if("exception".equals(status)){
                                exception_for_the_trace = 1;
                            }
                            //之前的sssr没有status,只有result
                            else if("result".equals(status)){
                                adjust_result_sssr+=bucket1.getDocCount();
                            }
                        }
                    }
                    //处理 success+http200 桶的文档个数之和==spances 总数
                    Terms call_result_in_traces =  ba.getAggregations().get("call_result");
                    long success_spans_for_the_trace=0;
                    if(call_result_in_traces.getBuckets()!=null){
                        for(Terms.Bucket bucket1:call_result_in_traces.getBuckets()){
                            String status = (String)bucket1.getKey();
                            if("success".equals(status)){
                                success_spans_for_the_trace+=bucket1.getDocCount();
                            }
                            //之前的sssr没有status,只有result
                            if("200".equals(status)){
                                success_spans_for_the_trace+=bucket1.getDocCount();
                            }
                        }
                    }
                    //注意加上adjust_result_sssr
                    if(success_spans_for_the_trace+adjust_result_sssr>=spans_in_trace){
                        successCount_for_the_trace=1;
                    }
                    //
                    //统计each trace in this duration最终结果计数
                    exceptionsCount+=exception_for_the_trace;
                    successCount+=successCount_for_the_trace;
                }*/
            }else{
                statsBuilder.avgDuration(0).maxDuration(0)
                        .minDuration(0).count(0);
            }
            TraceMetricsResult.TraceMetrics tm = statsBuilder.build();
            tm.setTraceCount(tracesCount);
            tm.setExceptionCount(exceptionsCount);
            tm.setSuccessCount(successCount);
            stats.add(tm);
        }
        TraceMetricsResult result=new TraceMetricsResult();
        result.setMetrics(stats);
        result.setTook(totalTook);
        return result;
    }

    @Override
    public SpanMetricsResult getSpansMetrics(TraceQueryRequest request) {
        request.checkAndSetDefault();
        TraceQueryRequest.Builder builder=request.newBuilder(request);
        SearchRequestBuilder requestBuilder= searchRequestBuilder(builder.build());
        requestBuilder.setSize(0);
        TermsAggregationBuilder spanNameTerms=AggregationBuilders.terms("span_name_terms").size(zipkinExtendESConfig.getBucketsSize())
                .field("name");
        TermsAggregationBuilder spanIdTerms=AggregationBuilders.terms("span_id_terms").size(zipkinExtendESConfig.getBucketsSize())
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
                spanMetrics.avgDuration(TimeUtil.microToMills(statsBucket.getAvg()))
                        .maxDuration(TimeUtil.microToMills(statsBucket.getMax()))
                        .minDuration(TimeUtil.microToMills(statsBucket.getMin()))
                        .sumDuration(TimeUtil.microToMills(statsBucket.getSum()))
                        .count(statsBucket.getCount());
                stats.add(spanMetrics.build());
            }
        }
        SpanMetricsResult result=new SpanMetricsResult();
        result.setData(stats);
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


    /**
     * 批量统计
     * @param request
     * @return
     */
    @Override
    public SpanTimeSeriesResult getMultiSpansTimeSeries(TraceQueryRequest request){
        request.checkAndSetDefault();
        TraceQueryRequest.Builder builder = request.newBuilder(request);
        SpanTimeSeriesResult result = new SpanTimeSeriesResult();
        //没有传指定的spans时，获取全部span列表再查询
        if(request.getSpans()==null || request.getSpans().isEmpty()){
            SpanNameQueryRequest spanNameQueryRequest = new SpanNameQueryRequest();
            spanNameQueryRequest.setServices(request.getServices());
            spanNameQueryRequest.setEndTs(request.getEndTs());
            spanNameQueryRequest.setLookback(request.getLookback());
            Set<String> spans = getSpans(spanNameQueryRequest);
            request.setSpans(Arrays.asList(spans.toArray(new String[0])));
            //return null;
        }
        List<SpanTimeSeriesResult.SpanTimeSeries> sereis = new ArrayList<SpanTimeSeriesResult.SpanTimeSeries>();
        for (String span:request.getSpans()){
            builder.spans(Arrays.asList(span));
            SpanTimeSeriesResult.SpanTimeSeries spanTimeSeriesItem = getSpanTimeSeries(builder.build());
            sereis.add(spanTimeSeriesItem);
            result.addTook(spanTimeSeriesItem.getTook());
        }
        result.setData(sereis);
        result.setSuccess(true);
        return result;
    }


    /**
     * 按照选择的spanName查询时间段内的分段统计平均响应时间
     * @param request
     * @return
     */
    public SpanTimeSeriesResult.SpanTimeSeries getSpanTimeSeries(TraceQueryRequest request){
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
            requestBuilder.addAggregation(AggregationBuilders.terms("span_terms")
                    .field("id")
                    .subAggregation(AggregationBuilders.max("span_duration")
                            .field("duration")));
            requestBuilder.addAggregation(PipelineAggregatorBuilders.
                    statsBucket("span_duration_stats","span_terms>span_duration"));
            multiSearch.add(requestBuilder);
            ranges.add(new TimestampRange(itemStartTs,itemEndTs));
            itemStartTs=itemEndTs;
        }while (itemEndTs<=request.endTs);
        MultiSearchResponse response=multiSearch.execute().actionGet();
        MultiSearchResponse.Item[] items=response.getResponses();
        List<SpanMetricsResult.SpanMetrics> stats=new ArrayList<>(items.length);
        long totalTook=0;
        for(int i=0;i<items.length;i++){
            MultiSearchResponse.Item item=items[i];
            SpanMetricsResult.SpanMetrics.Builder statsBuilder= SpanMetricsResult.SpanMetrics.newBuilder();
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
                InternalStatsBucket spanAvgDuration=aggregations.get("span_duration_stats");
                if(spanAvgDuration.getSum()>0){
                    statsBuilder.avgDuration(TimeUtil.microToMills(spanAvgDuration.getAvg()));
                }else{
                    statsBuilder.avgDuration(0L);
                }
                statsBuilder.count(spanAvgDuration.getCount());
            }else{
                statsBuilder.avgDuration(0);
            }
            stats.add(statsBuilder.build());
        }
        SpanTimeSeriesResult.SpanTimeSeries result=new SpanTimeSeriesResult.SpanTimeSeries();
        result.setSeries(stats);
        result.setTook(totalTook);
        result.setSpanName(request.getSpans().get(0));
        return result;
    }

    public ZipkinExtendESConfig getZipkinExtendESConfig() {
        return zipkinExtendESConfig;
    }

    public void setZipkinExtendESConfig(ZipkinExtendESConfig zipkinExtendESConfig) {
        this.zipkinExtendESConfig = zipkinExtendESConfig;
    }

    public ZipkinElasticsearchHttpStorageProperties getZipkinESStorageProperties() {
        return zipkinESStorageProperties;
    }

    public void setZipkinESStorageProperties(ZipkinElasticsearchHttpStorageProperties zipkinESStorageProperties) {
        this.zipkinESStorageProperties = zipkinESStorageProperties;
    }

    public TransportClient getTransportClient() {
        return transportClient;
    }

    public void setTransportClient(TransportClient transportClient) {
        this.transportClient = transportClient;
    }
}
