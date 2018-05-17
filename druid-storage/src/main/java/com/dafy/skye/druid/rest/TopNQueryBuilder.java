package com.dafy.skye.druid.rest;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class TopNQueryBuilder {

    private Map query = new HashMap<>();

    private ObjectMapper json = new ObjectMapper();

    public TopNQueryBuilder queryType(String queryType){
        query.put("queryType",queryType);
        return this;
    }


    public TopNQueryBuilder dataSource(String dataSource){
        query.put("dataSource",dataSource);
        return this;
    }

    public TopNQueryBuilder dimension(String dimension){
        query.put("dimension",dimension);
        return this;
    }

    public TopNQueryBuilder granularity(Granularity granularity){
        query.put("granularity",granularity);
        return this;
    }

    public TopNQueryBuilder threshold(int threshold){
        query.put("threshold",threshold);
        return this;
    }

    public TopNQueryBuilder metric(String metric){
        query.put("metric",metric);
        return this;
    }

    public TopNQueryBuilder filter(Filter filter){
        query.put("filter",filter);
        return this;
    }

    public TopNQueryBuilder aggregation(Aggregation aggregation){
        List<Aggregation> aggregations = query.get("aggregations")==null?null:(List<Aggregation>)query.get("aggregations");
        if(aggregations==null){
            aggregations = new ArrayList<>();
            query.put("aggregations",aggregations);
        }
        aggregations.add(aggregation);
        return this;
    }

    public TopNQueryBuilder intervals(TimeInterval intervals){
        query.put("intervals",intervals.format());
        return this;
    }

    /**
     * {
     "queryType": "topN",
     "dataSource": "sample_data",
     "dimension": "sample_dim",
     "threshold": 5,
     "metric": "count",
     "granularity": "all",
     "filter": {
     "type": "and",
     "fields": [
     {
     "type": "selector",
     "dimension": "dim1",
     "value": "some_value"
     },
     {
     "type": "selector",
     "dimension": "dim2",
     "value": "some_other_val"
     }
     ]
     },
     "aggregations": [
     {
     "type": "longSum",
     "name": "count",
     "fieldName": "count"
     },
     {
     "type": "doubleSum",
     "name": "some_metric",
     "fieldName": "some_metric"
     }
     ],
     "postAggregations": [
     {
     "type": "arithmetic",
     "name": "average",
     "fn": "/",
     "fields": [
     {
     "type": "fieldAccess",
     "name": "some_metric",
     "fieldName": "some_metric"
     },
     {
     "type": "fieldAccess",
     "name": "count",
     "fieldName": "count"
     }
     ]
     }
     ],
     "intervals": [
     "2013-08-31T00:00:00.000/2013-09-03T00:00:00.000"
     ]
     }
     */


    public String build() throws Exception{
        return json.writeValueAsString(query);
    }


}
