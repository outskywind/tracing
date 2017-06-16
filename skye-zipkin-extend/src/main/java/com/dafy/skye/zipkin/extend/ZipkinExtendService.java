package com.dafy.skye.zipkin.extend;

import com.dafy.skye.common.elasticsearch.ESSearchRequest;
import com.dafy.skye.common.elasticsearch.ESearchResponse;
import com.dafy.skye.common.elasticsearch.EasyRestClient;
import com.dafy.skye.common.util.IndexNameFormatter;
import com.dafy.skye.common.util.JacksonConvert;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import zipkin.DependencyLink;
import zipkin.Span;
import zipkin.autoconfigure.storage.elasticsearch.http.ZipkinElasticsearchHttpStorageProperties;
import zipkin.storage.QueryRequest;
import zipkin.storage.SpanStore;
import zipkin.storage.elasticsearch.http.internal.client.Aggregation;
import zipkin.storage.elasticsearch.http.internal.client.SearchRequest;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Caedmon on 2017/6/15.
 */
public class ZipkinExtendService {
    @Autowired
    private SpanStore spanStore;
    private ZipkinElasticsearchHttpStorageProperties elasticsearchHttpStorageProperties;
    private EasyRestClient easyRestClient;
    private IndexNameFormatter indexNameFormatter;
    @PostConstruct
    public void init(){
        HttpHost[] hosts=new HttpHost[elasticsearchHttpStorageProperties.getHosts().size()];
        int i=0;
        for(String address: elasticsearchHttpStorageProperties.getHosts()){
            String[] array=address.split(":");
            String host=array[0];
            int port=Integer.parseInt(array[1]);
            hosts[i]=new HttpHost(host,port);
            i++;
        }
        this.easyRestClient =new EasyRestClient(RestClient.builder(hosts).build());
        IndexNameFormatter.Builder formatterBuilder=IndexNameFormatter.builder();
        formatterBuilder.dateSeparator('-');
        this.indexNameFormatter=formatterBuilder.index(elasticsearchHttpStorageProperties.getIndex()).build();
    }
    ESearchResponse<List<List<Span>>> getTraces(TraceQueryRequest request){
        //TODO 基本条件过滤
        //根据traceId聚合,获取最大duration,再聚合 就是trace的聚合
        long startTs=request.startTs;
        long endTs=request.endTs;
        List<String> indices=indexNameFormatter.indexNamePatternsForRange(startTs,endTs);
        ESSearchRequest esSearchRequest=ESSearchRequest.forIndicesAndType(indices,"zipkin");
        ESSearchRequest.Filters filters=new ESSearchRequest.Filters();
        if (Strings.isNullOrEmpty(request.serviceName)) {
            filters.addTerm("serviceName",request.serviceName);
        }
        if(Strings.isNullOrEmpty(request.spanName)){
            filters.addTerm("spanName",request.spanName);
        }
        for (String annotation : request.annotations) {
            Map<String, String> nestedTerms = new LinkedHashMap<>();
            nestedTerms.put("annotations.value", annotation);
            if (request.serviceName != null) {
                nestedTerms.put("annotations.endpoint.serviceName", request.serviceName);
            }
            filters.addNestedTerms(nestedTerms);
        }
        for (Map.Entry<String, String> kv : request.binaryAnnotations.entrySet()) {
            Map<String, String> nestedTerms = new LinkedHashMap<>();
            nestedTerms.put("binaryAnnotations.key", kv.getKey());
            nestedTerms.put("binaryAnnotations.value", kv.getValue());
            if (request.serviceName != null) {
                nestedTerms.put("binaryAnnotations.endpoint.serviceName", request.serviceName);
            }
            filters.addNestedTerms(nestedTerms);
        }
        if (request.minDuration != null) {
            filters.addRange("duration", request.minDuration, request.maxDuration);
        }
        Aggregation traceIdTimestamp = Aggregation.terms("trace_overview", request.limit)
                .addSubAggregation(Aggregation.min("timestamp_millis"))
                .orderBy("timestamp_millis", "desc");
        Aggregation spanIdTimestamp= Aggregation.terms("spanId", request.limit)
                .addSubAggregation(Aggregation.min("timestamp_millis"))
                .orderBy("timestamp_millis", "desc");
        EasyRestClient.EasyRequestBuilder builder= new EasyRestClient.EasyRequestBuilder();
        StringBuilder endpointBuilder=new StringBuilder();
        endpointBuilder.append("/").append(StringUtils.arrayToDelimitedString(indices.toArray(),","))
                .append("/")
                .append("zipkin")
                .append("/_search");
        Map<String,String> params=new HashMap<>();
        params.put("allow_no_indices","true");
        params.put("expand_wildcards","open");
        params.put("ignore_unavailable","true");
        return null;
    }

}
