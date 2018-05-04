package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.autoconf.ZipkinElasticsearchStorageProperties;
import com.dafy.skye.common.elasticsearch.DefaultOptions;
import com.dafy.skye.common.util.IndexNameFormatter;
import com.dafy.skye.common.util.IntervalTimeUnit;
import com.dafy.skye.zipkin.extend.cache.ServiceRefreshHolder;
import com.dafy.skye.zipkin.extend.config.ZipkinExtendESConfig;
import com.dafy.skye.zipkin.extend.dto.*;

import com.google.common.base.Strings;

import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.cardinality.ParsedCardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import zipkin.Codec;
import zipkin.Span;
import zipkin.internal.GroupByTraceId;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by Caedmon on 2017/6/15.
 */
@Service
public class ZipkinExtendServiceImpl implements ZipkinExtendService {

    Logger log = LoggerFactory.getLogger(ZipkinExtendServiceImpl.class);
    @Autowired
    private ZipkinExtendESConfig zipkinExtendESConfig;
    @Autowired
    private ZipkinElasticsearchStorageProperties zipkinESStorageProperties;

    private IndexNameFormatter indexNameFormatter;
    @Autowired
    private TransportClient transportClient;

    @Autowired
    private RestHighLevelClient restClient;


    @PostConstruct
    public void init() throws UnknownHostException{
        IndexNameFormatter.Builder formatterBuilder=IndexNameFormatter.builder();
        formatterBuilder.dateSeparator('-');
        final String index=zipkinESStorageProperties.getIndex();
        this.indexNameFormatter=formatterBuilder.index(index).build();
    }

    String[] indices(long endTs,long lookup){
        List<String> indices = indexNameFormatter.formatTypeAndRange("span",endTs-lookup, endTs);
        String[] result=new String[indices.size()];
        indices.toArray(result);
        return result;
    }

    void checkAndSetDefault(BasicQueryRequest request){
        if(request.interval==null){
            request.interval=1;
        }
        if(request.intervalUnit==null){
            request.intervalUnit= IntervalTimeUnit.HOUR;
        }
    }

    SearchSourceBuilder searchSourceBuilder(BasicQueryRequest request){
        checkAndSetDefault(request);
        SearchSourceBuilder requestBuilder= new SearchSourceBuilder();
        BoolQueryBuilder root=QueryBuilders.boolQuery();
        root.must(QueryBuilders.rangeQuery("timestamp_millis")
                .gte(request.endTs-request.lookback).lt(request.endTs).format("epoch_millis"));
        if(!CollectionUtils.isEmpty(request.spans)){
            root.filter(QueryBuilders.termsQuery("name",request.spans));
        }
        if(!CollectionUtils.isEmpty(request.services)){
            root.filter(QueryBuilders.termsQuery("localEndpoint.serviceName",request.services));
        }
        root.filter(QueryBuilders.termQuery("kind","SERVER"));

        if(!Strings.isNullOrEmpty(request.sortField)&&!Strings.isNullOrEmpty(request.sortField)){
            requestBuilder.sort(request.sortField,SortOrder.fromString(request.sortOrder));
        }
        requestBuilder.size(0);
        requestBuilder.query(root);

        return requestBuilder;
    }

    //服务列表
    @Override
    public Set<String> getServices(BasicQueryRequest request){
        Set<String> set=new HashSet<>();
        try{
            String[] indices=indices(request.endTs,request.lookback);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            SearchSourceBuilder searchSourceBuilder = searchSourceBuilder(request);
            searchSourceBuilder.aggregation(AggregationBuilders.terms("localServiceName").field("localEndpoint.serviceName"));
            searchRequest.source(searchSourceBuilder);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchResponse response =  restClient.search(searchRequest);
            if(response.getAggregations()==null){
                return null;
            }
            Terms localServiceName=response.getAggregations().get("localServiceName");

            for(Terms.Bucket bucket:localServiceName.getBuckets()){
                set.add(bucket.getKeyAsString());
            }
        }catch (Exception e){
            log.error("ES统计服务失败：",e);
        }
        return set;
    }


    //服务详细
    @Override
    public ServiceTimeSeriesResult getServiceTimeSeries(BasicQueryRequest request) {
        String[] indices=indices(request.endTs,request.lookback);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indices);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        SearchSourceBuilder searchSourceBuilder = searchSourceBuilder(request);
        searchSourceBuilder.aggregation(AggregationBuilders.dateHistogram("timeSplit").
                dateHistogramInterval(new DateHistogramInterval(request.timeInterval)).field("timestamp_millis").timeZone(DateTimeZone.forID("Asia/Shanghai"))
                .subAggregation(AggregationBuilders.terms("services").field("name").order(BucketOrder.count(false))
                        .subAggregation(AggregationBuilders.cardinality("count").field("id"))
                        .subAggregation(AggregationBuilders.avg("time").field("duration")))
        );
        searchRequest.source(searchSourceBuilder);
        ServiceTimeSeriesResult result = new ServiceTimeSeriesResult();
       try{
           SearchResponse  response = restClient.search(searchRequest);
           ParsedDateHistogram timeSplit = response.getAggregations().get("timeSplit");
           Map<String,ServiceTimeSeriesResult.ServiceInterfaceTimeSeries> data = new HashMap<>();
           for(Histogram.Bucket timeBucket:timeSplit.getBuckets()){
                Terms services = timeBucket.getAggregations().get("services");
                for(Terms.Bucket serviceBucket:services.getBuckets()){
                    String service = (String)serviceBucket.getKey();
                    ParsedCardinality count = serviceBucket.getAggregations().get("count");
                    ParsedAvg time = serviceBucket.getAggregations().get("time");
                    ServiceTimeSeriesResult.ServiceInterfaceTimeSeries series = data.get(service);
                    if(series==null){
                        series = new ServiceTimeSeriesResult.ServiceInterfaceTimeSeries();
                        series.setSpanName(service);
                        data.put(service,series);
                    }
                    SpanMetricsResult.InterfaceMetrics metric = new SpanMetricsResult.InterfaceMetrics();
                    metric.setAvgDuration(Math.round(time.getValue())/1000);
                    metric.setCount(count.getValue());
                    //metric.setSpanName(service);
                    metric.setStartTs(Long.parseLong(timeBucket.getKeyAsString()));
                    //metric.setEndTs(metric.getStartTs()); 不设置结束时间
                    series.getSeries().add(metric);
                }
           }
           //
           result.setData(new ArrayList<ServiceTimeSeriesResult.ServiceInterfaceTimeSeries>());
           for(ServiceTimeSeriesResult.ServiceInterfaceTimeSeries series:data.values()){
               result.getData().add(series);
           }
       }catch (Exception e){
           log.error("查询异常:",e);
       }
        return result;
    }

    /**
     * 服务面板数据展示
     * @param request
     * @return
     */
    @Override
    public List<ServiceMonitorMetric> getServiceMonitorMetrics(BasicQueryRequest request) {
        String[] indices=indices(request.endTs,request.lookback);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indices);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        SearchSourceBuilder searchSourceBuilder = searchSourceBuilder(request);
        searchSourceBuilder.aggregation(AggregationBuilders.terms("services").field("localEndpoint.serviceName")
                .subAggregation(AggregationBuilders.avg("time").field("duration"))
                .subAggregation(AggregationBuilders.cardinality("count").field("id"))
                .subAggregation(AggregationBuilders.filter("success",QueryBuilders.termQuery("tags.status","success")).subAggregation(AggregationBuilders.cardinality("count").field("id")))
                .subAggregation(AggregationBuilders.terms("host").field("localEndpoint.ipv4")
                        .subAggregation(AggregationBuilders.avg("time").field("duration"))
                        .subAggregation(AggregationBuilders.cardinality("count").field("id"))
                        .subAggregation(AggregationBuilders.filter("success",QueryBuilders.termQuery("tags.status","success")).subAggregation(AggregationBuilders.cardinality("count").field("id")))));

        searchRequest.source(searchSourceBuilder);
        List<ServiceMonitorMetric> result = new ArrayList<>();
        try{
            SearchResponse  response = restClient.search(searchRequest);
            ParsedStringTerms services = response.getAggregations().get("services");
            for(Terms.Bucket serviceBucket: services.getBuckets()){
                //
                ServiceMonitorMetric service = new ServiceMonitorMetric();
                result.add(service);
                String serviceName = (String)serviceBucket.getKey();
                ParsedCardinality count = serviceBucket.getAggregations().get("count");
                ParsedAvg time = serviceBucket.getAggregations().get("time");
                ParsedFilter success = serviceBucket.getAggregations().get("success");
                ParsedCardinality success_count = success.getAggregations().get("count");
                service.setName(serviceName);
                service.setLatency(Math.round(time.getValue())/1000);
                service.setQps(Math.round(count.getValue()*1000/(double)request.lookback));
                service.setSuccess_rate(Math.round(success_count.getValue()*100/count.getValue())+"%");
                service.setSuccessPercent(Math.round(success_count.getValue()*100/count.getValue()));
                //host
                ParsedStringTerms hosts = serviceBucket.getAggregations().get("host");
                for(Terms.Bucket host: hosts.getBuckets()){
                    MonitorMetric metric = new MonitorMetric();
                    service.getServers().add(metric);
                    metric.setName((String)host.getKey());
                    count = host.getAggregations().get("count");
                    time = host.getAggregations().get("time");
                    success = host.getAggregations().get("success");
                    success_count = success.getAggregations().get("count");
                    //service.setName(serviceName);
                    metric.setLatency(Math.round(time.getValue())/1000);
                    metric.setQps(Math.round(count.getValue()*1000/(double)request.lookback));
                    metric.setSuccess_rate(Math.round(success_count.getValue()*100/count.getValue())+"%");
                    metric.setSuccessPercent(Math.round(success_count.getValue()*100/count.getValue()));
                }
            }
        }catch(Exception e){
            log.error("服务面板数据查询异常:",e);
        }
        return result;
    }

    @Override
    public List<String> getServiceInterfaces(String serviceName) {
        List<String> result = new ArrayList<>();
        try{
            BasicQueryRequest request = new BasicQueryRequest();
            request.endTs=System.currentTimeMillis();
            request.setServices(Arrays.asList(serviceName));
            String[] indices=indices(request.endTs,request.lookback);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchSourceBuilder searchSourceBuilder = searchSourceBuilder(request);
            searchSourceBuilder.aggregation(AggregationBuilders.terms("interfaces").field("name").order(BucketOrder.count(false)));
            searchRequest.source(searchSourceBuilder);
            //
            SearchResponse  response = restClient.search(searchRequest);
            ParsedStringTerms interfaces = response.getAggregations().get("interfaces");
            for(Terms.Bucket bucket: interfaces.getBuckets()){
                String interfaceName = (String)bucket.getKey();
                result.add(interfaceName);
            }
        }catch (Exception e){
            log.error("服务接口查询异常",e);
        }
        return result;
    }


//-------------deprecated --------------------------------------------------
    SearchRequestBuilder searchRequestBuilder(BasicQueryRequest request){
        checkAndSetDefault(request);
        String[] indices=indices(request.endTs,request.lookback);
        SearchRequestBuilder requestBuilder=transportClient.prepareSearch(indices)
                .setIndicesOptions(DefaultOptions.defaultIndicesOptions());
        BoolQueryBuilder root=QueryBuilders.boolQuery();
        root.filter(QueryBuilders.rangeQuery("timestamp_millis")
                .gte(request.endTs-request.lookback).lt(request.endTs));
        if(!CollectionUtils.isEmpty(request.spans)){
            root.filter(QueryBuilders.termsQuery("name",request.spans));
        }
        if(!CollectionUtils.isEmpty(request.services)){
            root.must(QueryBuilders.termsQuery("localEndpoint.serviceName",request.services));
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
        root.minimumShouldMatch(1);
        requestBuilder.setSize(request.limit);
        requestBuilder.setQuery(root);

        return requestBuilder;
    }

    public TraceQueryResult getTraces(BasicQueryRequest request){
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

    public ZipkinExtendESConfig getZipkinExtendESConfig() {
        return zipkinExtendESConfig;
    }

    public void setZipkinExtendESConfig(ZipkinExtendESConfig zipkinExtendESConfig) {
        this.zipkinExtendESConfig = zipkinExtendESConfig;
    }

    public ZipkinElasticsearchStorageProperties getZipkinESStorageProperties() {
        return zipkinESStorageProperties;
    }

    public void setZipkinESStorageProperties(ZipkinElasticsearchStorageProperties zipkinESStorageProperties) {
        this.zipkinESStorageProperties = zipkinESStorageProperties;
    }

    public TransportClient getTransportClient() {
        return transportClient;
    }

    public void setTransportClient(TransportClient transportClient) {
        this.transportClient = transportClient;
    }
}
