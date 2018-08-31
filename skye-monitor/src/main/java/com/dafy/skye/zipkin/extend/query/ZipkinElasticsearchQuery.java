package com.dafy.skye.zipkin.extend.query;

import com.dafy.skye.common.util.IntervalTimeUnit;
import com.dafy.skye.zipkin.extend.dto.BasicQueryRequest;
import com.google.common.base.Strings;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by quanchengyun on 2018/5/10.
 */
public class ZipkinElasticsearchQuery {


    public static void  checkAndSetDefault(BasicQueryRequest request){
        if(request.interval==null){
            request.interval=1;
        }
        if(request.intervalUnit==null){
            request.intervalUnit= IntervalTimeUnit.HOUR;
        }
    }


    public static SearchSourceBuilder searchSourceBuilder(BasicQueryRequest request){
        checkAndSetDefault(request);
        SearchSourceBuilder requestBuilder= new SearchSourceBuilder();
        BoolQueryBuilder root= QueryBuilders.boolQuery();
        root.must(QueryBuilders.rangeQuery("timestamp_millis")
                .gte(request.endTs-request.lookback).lt(request.endTs).format("epoch_millis"));
        if(!CollectionUtils.isEmpty(request.spans)){
            root.filter(QueryBuilders.termsQuery("name",request.spans));
        }
        if(!CollectionUtils.isEmpty(request.services)){
            root.filter(QueryBuilders.termsQuery("localEndpoint.serviceName",request.services));
        }
        root.filter(QueryBuilders.termQuery("kind","SERVER"));

        if(!Strings.isNullOrEmpty(request.sortField)){
            requestBuilder.sort(request.sortField, SortOrder.fromString(request.sortOrder));
        }
        requestBuilder.size(0);
        if(request.getLimit()>0){
            requestBuilder.size(request.getLimit());
        }
        requestBuilder.query(root);

        return requestBuilder;
    }

    /**
     * 通用接口 未完成  TODO
     * @param indices 索引
     * @param parameter 过滤条件
     * @param dimensions 统计维度
     * @param metrics 统计指标
     * @return
     */
    public static List<ElasticsearchMetric[]> getTimeSeries(String indices,BasicQueryRequest parameter,List<Dimension> dimensions,ElasticsearchMetric[] metrics){
        SearchSourceBuilder builder = searchSourceBuilder(parameter);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indices);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        //
        //parseDimension
        sort(dimensions);
        AggregationBuilder parent = AggregationBuilders.dateHistogram("timeSplit").
                dateHistogramInterval(new DateHistogramInterval(parameter.timeInterval)).field("timestamp_millis");
        for(Dimension dimension:dimensions){
            AggregationBuilder thisDimension=null;
            if(Dimension.service.equals(dimension)){
                thisDimension = AggregationBuilders.terms("services").field(dimension.value()).order(BucketOrder.count(false));
            }
            else if(Dimension.interface_name.equals(dimension)){
                thisDimension = AggregationBuilders.terms("interfaces").field(dimension.value()).order(BucketOrder.count(false));
            }
            else if(Dimension.host.equals(dimension)){
                thisDimension = AggregationBuilders.terms("hosts").field(dimension.value()).order(BucketOrder.count(false));
            }
            if(thisDimension==null){
                break;
            }
            parent.subAggregation(thisDimension);
            parent=thisDimension;
        }


        builder.aggregation(AggregationBuilders.dateHistogram("timeSplit").
                dateHistogramInterval(new DateHistogramInterval(parameter.timeInterval)).field("timestamp_millis")
                .subAggregation(AggregationBuilders.terms("services").field("name").order(BucketOrder.count(false))
                        .subAggregation(AggregationBuilders.cardinality("count").field("id"))
                        .subAggregation(AggregationBuilders.avg("time").field("duration")))
        );

        searchRequest.source(builder);


        return null;
    }

    protected  static void sort(List<Dimension> dimensions){
        Collections.sort(dimensions, new Comparator<Dimension>() {
            @Override
            public int compare(Dimension o1, Dimension o2) {
                return o1.order()-o2.order();
            }
        });
    }



}
