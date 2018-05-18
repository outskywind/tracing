package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.common.elasticsearch.DefaultOptions;
import com.dafy.skye.common.util.IndexNameFormatter;
import com.dafy.skye.druid.entity.GroupbyQueryResult;
import com.dafy.skye.druid.entity.TimeSeriesQueryResult;
import com.dafy.skye.druid.rest.*;
import com.dafy.skye.zipkin.config.elasticsearch.ZipkinElasticsearchStorageProperties;
import com.dafy.skye.zipkin.extend.config.ZipkinExtendESConfigurationProperties;
import com.dafy.skye.zipkin.extend.converter.DruidResultConverter;
import com.dafy.skye.zipkin.extend.dto.*;

import com.dafy.skye.zipkin.extend.query.ZipkinElasticsearchQuery;
import com.dafy.skye.zipkin.extend.util.TimeUtil;
import com.google.common.base.Strings;

import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.cardinality.ParsedCardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by Caedmon on 2017/6/15.
 */
@Service
public class ZipkinExtendServiceImpl implements ZipkinExtendService {

    Logger log = LoggerFactory.getLogger(ZipkinExtendServiceImpl.class);
    @Autowired
    private ZipkinExtendESConfigurationProperties zipkinExtendESConfigurationProperties;
    @Autowired
    private ZipkinElasticsearchStorageProperties zipkinESStorageProperties;

    private IndexNameFormatter indexNameFormatter;
    @Autowired
    private TransportClient transportClient;

    @Autowired
    RestDruidClient restDruidClient;

    @Autowired
    private RestHighLevelClient restClient;


    @PostConstruct
    public void init() {
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





    //服务列表
    @Override
    public Set<String> getServices(BasicQueryRequest request){
        Set<String> set=new HashSet<>();
        try{
            String[] indices=indices(request.endTs,request.lookback);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            SearchSourceBuilder searchSourceBuilder = ZipkinElasticsearchQuery.searchSourceBuilder(request);
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
        SearchSourceBuilder searchSourceBuilder = ZipkinElasticsearchQuery.searchSourceBuilder(request);
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
    public Collection<TimeSeriesResult> getServiceSeries(ServiceSeriesRequest request){
        //druid 一次最多5000个时序点因此如果时间序列250个点，只能一次返回最多20条线
        //时间粒度需要后端重新计算校验
        GroupByQueryBuilder builder = GroupByQueryBuilder.builder();
        builder.dataSource("service-span-metric").dimensions(new String[]{"spanName"})
                .filter(Filter.builder().type(LogicType.selector).dimension("serviceName").value(request.getService()))
                .granularity(TimeUtil.parseTimeInterval(request.getTimeInterval()),new DateTime(request.getStart()))
                .timeRange(TimeInterval.builder().start(new DateTime(request.getStart())).end(new DateTime(request.getEnd())))
                .addAggregation(com.dafy.skye.druid.rest.Aggregation.builder().name("latency").type(AggregationType.longSum).fieldName("duration"))
                .addAggregation(com.dafy.skye.druid.rest.Aggregation.builder().name("_count").type(AggregationType.longSum).fieldName("count"))
                .addPostAggregation(PostAggregation.builder().name("avg_latency").fn(Fn.div)
                        .fields(Arrays.asList(new PostAggregation.PostAggregationField("latency"),new PostAggregation.PostAggregationField("_count"))));
        List<GroupbyQueryResult> queryResult =  restDruidClient.groupby(builder);

        return DruidResultConverter.convertSeriesMetricGroupby(queryResult,"spanName",request.getTimeInterval());
    }

    @Override
    public Collection<MonitorMetric> getInterfacesMonitorMetric(ServiceSeriesRequest request) {
        //druid 一次最多5000个时序点因此如果时间序列250个点，一次返回最多20条线
        //时间粒度需要后端重新计算校验
        GroupByQueryBuilder builder = GroupByQueryBuilder.builder();
        builder.dataSource("service-span-metric").dimensions(new String[]{"spanName"})
                .filter(Filter.builder().type(LogicType.selector).dimension("serviceName").value(request.getService()))
                .granularity(Granularity.all)
                .timeRange(TimeInterval.builder().start(new DateTime(request.getStart())).end(new DateTime(request.getEnd())))
                .addAggregation(com.dafy.skye.druid.rest.Aggregation.builder().name("latency").type(AggregationType.longSum).fieldName("duration"))
                .addAggregation(com.dafy.skye.druid.rest.Aggregation.builder().name("success").type(AggregationType.longSum).fieldName("successCount"))
                .addAggregation(com.dafy.skye.druid.rest.Aggregation.builder().name("peak_qps").type(AggregationType.longMax).fieldName("count"))
                .addAggregation(com.dafy.skye.druid.rest.Aggregation.builder().name("_count").type(AggregationType.longSum).fieldName("count"))
                .addPostAggregation(PostAggregation.builder().name("avg_latency").fn(Fn.div)
                        .fields(Arrays.asList(new PostAggregation.PostAggregationField("latency"),new PostAggregation.PostAggregationField("_count"))))
                .addPostAggregation(PostAggregation.builder().name("success_rate").fn(Fn.div)
                        .fields(Arrays.asList(new PostAggregation.PostAggregationField("success"),new PostAggregation.PostAggregationField("_count"))));
        List<GroupbyQueryResult> queryResult =  restDruidClient.groupby(builder);
        return DruidResultConverter.convertMetric(queryResult,"spanName");
    }

    @Override
    public Collection<SeriesMetric> getInterfaceSeries(InterfaceSeriesRequest request) {
        TimeSeriesQueryBuilder builder = TimeSeriesQueryBuilder.builder();
        builder.dataSource("service-span-metric")
                .filter(Filter.builder().type(LogicType.and).fields(
                        Field.builder().type(Field.FieldType.selector).dimension("serviceName").value(request.getService()),
                        Field.builder().type(Field.FieldType.selector).dimension("spanName").value(request.getName())))
                .granularity(TimeUtil.parseTimeInterval(request.getTimeInterval()),new DateTime(request.getStart()))
                .timeRange(TimeInterval.builder().start(new DateTime(request.getStart())).end(new DateTime(request.getEnd())))
                .addAggregation(com.dafy.skye.druid.rest.Aggregation.builder().name("latency").type(AggregationType.longSum).fieldName("duration"))
                .addAggregation(com.dafy.skye.druid.rest.Aggregation.builder().name("_count").type(AggregationType.longSum).fieldName("count"))
                .addPostAggregation(PostAggregation.builder().name("avg_latency").fn(Fn.div)
                        .fields(Arrays.asList(new PostAggregation.PostAggregationField("latency"),new PostAggregation.PostAggregationField("_count"))));
        List<TimeSeriesQueryResult> queryResult =  restDruidClient.timeseries(builder);
        return DruidResultConverter.convertSeriesMetricTimeseries(queryResult,request.getTimeInterval());
    }

    @Override
    public List<Trace> getInterfaceTraces(BasicQueryRequest request) {
        List<Trace> result = new ArrayList<>();
        try{
            String[] indices=indices(request.endTs,request.lookback);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchSourceBuilder searchSourceBuilder = ZipkinElasticsearchQuery.searchSourceBuilder(request);
            searchSourceBuilder.fetchSource(new String[]{"traceId","duration","timestamp_millis","localEndpoint.ipv4","tags.status"},new String[]{});
            searchRequest.source(searchSourceBuilder);
            SearchResponse  searchResponse = restClient.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                // do something with the SearchHit
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                Trace trace = new Trace();
                trace.setTraceId((String)sourceAsMap.get("traceId"));
                trace.setLatency(Math.round((Integer)sourceAsMap.get("duration")/1000));//zipkin 原始值 microseconds
                trace.setTimestamp((Long)sourceAsMap.get("timestamp_millis"));
                trace.setHost((String)(((Map)sourceAsMap.get("localEndpoint")).get("ipv4")));

                trace.setSuccess(sourceAsMap.get("tags")!=null && "success".equals(((Map)sourceAsMap.get("tags")).get("status")));
                result.add(trace);
            }
        }catch (Exception e){
            log.error("error:",e);
        }
        return result;
    }


    @Override
    public ServiceInfo getserviceinfo(String serviceName) {
        ServiceInfo result = new ServiceInfo();
        try{
            BasicQueryRequest request = new BasicQueryRequest();
            request.endTs=System.currentTimeMillis();
            request.setServices(Arrays.asList(serviceName));
            String[] indices=indices(request.endTs,request.lookback);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
            SearchSourceBuilder searchSourceBuilder = ZipkinElasticsearchQuery.searchSourceBuilder(request);
            searchSourceBuilder.aggregation(AggregationBuilders.terms("interfaces").field("name").order(BucketOrder.count(false)))
                                .aggregation(AggregationBuilders.terms("hosts").field("localEndpoint.ipv4").order(BucketOrder.count(false)));
            searchRequest.source(searchSourceBuilder);
            //
            SearchResponse  response = restClient.search(searchRequest);
            ParsedStringTerms interfaces = response.getAggregations().get("interfaces");
            for(Terms.Bucket bucket: interfaces.getBuckets()){
                String interfaceName = (String)bucket.getKey();
                result.addInterface(interfaceName);
            }
            ParsedStringTerms hosts = response.getAggregations().get("hosts");
            for(Terms.Bucket bucket: interfaces.getBuckets()){
                String host = (String)bucket.getKey();
                result.addHost(host);
            }
        }catch (Exception e){
            log.error("服务接口查询异常",e);
        }
        return result;
    }


//-------------deprecated --------------------------------------------------
    SearchRequestBuilder searchRequestBuilder(BasicQueryRequest request){
        //checkAndSetDefault(request);
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

    public ZipkinExtendESConfigurationProperties getZipkinExtendESConfigurationProperties() {
        return zipkinExtendESConfigurationProperties;
    }

    public void setZipkinExtendESConfigurationProperties(ZipkinExtendESConfigurationProperties zipkinExtendESConfigurationProperties) {
        this.zipkinExtendESConfigurationProperties = zipkinExtendESConfigurationProperties;
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
