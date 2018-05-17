package com.dafy.skye.druid.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class TimeSeriesQueryBuilder implements QueryBuilder {

    private Map query = new HashMap<>();

    private ObjectMapper json = new ObjectMapper();


    public static TimeSeriesQueryBuilder builder(){
        TimeSeriesQueryBuilder builder = new TimeSeriesQueryBuilder();
        builder.json.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        builder.query.put("queryType","timeseries");
        return builder;
    }


    public TimeSeriesQueryBuilder dataSource(String dataSource){
        query.put("dataSource",dataSource);
        return this;
    }

    public TimeSeriesQueryBuilder granularity(Granularity granularity){
        query.put("granularity",granularity);
        return this;
    }
    /**
     *
     * @param ptTime  格式为 PT1H,PT1M,PT1S 时间粒度为 1 小时，1分钟，1秒 ，数字可为小数，整数
     * @param timeZone 返回的时间点时区   "Asia/Shanghai"
     * @param start 开始的时间点
     * @return
     */
    public TimeSeriesQueryBuilder granularity(String ptTime ,String timeZone, DateTime start){
        Map m = new HashMap();
        m.put("period",ptTime);
        m.put("type","period");
        m.put("timezone",timeZone);
        m.put("start",start.toString("yyyy-MM-dd'T'HH:mm:ssZ"));
        query.put("granularity",m);
        return this;
    }

    /**
     *
     * @param timeInterval  时间粒度毫秒单位
     * @param start 时间点开始时间 UTC
     * @return
     */
    public TimeSeriesQueryBuilder granularity(long  timeInterval , DateTime start){
        Map m = new HashMap();
        m.put("duration",timeInterval);
        m.put("type","duration");
        m.put("timezone","Asia/Shanghai");
        m.put("start",start.toString("yyyy-MM-dd'T'HH:mm:ssZ"));
        query.put("granularity",m);
        return this;
    }

    public TimeSeriesQueryBuilder descending(boolean descending){
        query.put("descending",descending);
        return this;
    }

    public TimeSeriesQueryBuilder filter(Filter filter){
        query.put("filter",filter);
        return this;
    }

    public TimeSeriesQueryBuilder addAggregation(Aggregation aggregation){
        List<Aggregation> aggregations = query.get("aggregations")==null?null:(List<Aggregation>)query.get("aggregations");
        if(aggregations==null){
            aggregations = new ArrayList<>();
            query.put("aggregations",aggregations);
        }
        aggregations.add(aggregation);
        return this;
    }


    public TimeSeriesQueryBuilder addPostAggregation(PostAggregation aggregation){
        List<PostAggregation> aggregations = query.get("postAggregations")==null?null:(List<PostAggregation>)query.get("postAggregations");
        if(aggregations==null){
            aggregations = new ArrayList<>();
            query.put("postAggregations",aggregations);
        }
        aggregations.add(aggregation);
        return this;
    }



    public TimeSeriesQueryBuilder timeRange(TimeInterval intervals){
        query.put("intervals",intervals.format());
        return this;
    }



    /**
     * {
     "queryType": "timeseries",
     "dataSource": "sample_datasource",
     "granularity": "day",
     "descending": "true",
     "filter": {
     "type": "and",
     "fields": [
     { "type": "selector", "dimension": "sample_dimension1", "value": "sample_value1" },
     { "type": "or",
     "fields": [
     { "type": "selector", "dimension": "sample_dimension2", "value": "sample_value2" },
     { "type": "selector", "dimension": "sample_dimension3", "value": "sample_value3" }
     ]
     }
     ]
     },
     "aggregations": [
     { "type": "longSum", "name": "sample_name1", "fieldName": "sample_fieldName1" },
     { "type": "doubleSum", "name": "sample_name2", "fieldName": "sample_fieldName2" }
     ],
     "postAggregations": [
     { "type": "arithmetic",
     "name": "sample_divide",
     "fn": "/",
     "fields": [
     { "type": "fieldAccess", "name": "postAgg__sample_name1", "fieldName": "sample_name1" },
     { "type": "fieldAccess", "name": "postAgg__sample_name2", "fieldName": "sample_name2" }
     ]
     }
     ],
     "intervals": [ "2012-01-01T00:00:00.000/2012-01-03T00:00:00.000" ]
     }
     */

    @Override
    public String build() throws Exception{
        return json.writeValueAsString(query);
    }


}
