package com.dafy.skye.log.collector.storage.elasticsearch.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Caedmon on 2017/6/5.
 */
public class Aggregation {
    transient final String field;
    AggTerms terms;
    Map<String, String> nested;
    Map<String, String> min;
    Map<String, Aggregation> aggs;

    public Aggregation(String field) {
        this.field = field;
    }

    public static Aggregation nestedTerms(String field) {
        Aggregation result = new Aggregation(field);
        result.nested = Collections.singletonMap("path", field.substring(0, field.indexOf('.')));
        result.addSubAggregation(terms(field, Integer.MAX_VALUE));
        return result;
    }

    public static Aggregation terms(String field, int size) {
        Aggregation result = new Aggregation(field);
        result.terms = new AggTerms(field, size);
        return result;
    }

    public Aggregation orderBy(String subAgg, String direction) {
        terms.order(subAgg, direction);
        return this;
    }

    public static Aggregation min(String field) {
        Aggregation result = new Aggregation(field);
        result.min = Collections.singletonMap("field", field);
        return result;
    }

    static class AggTerms {
        AggTerms(String field, int size) {
            this.field = field;
            this.size = size;
        }

        final String field;
        int size;
        Map<String, String> order;

        AggTerms order(String agg, String direction) {
            order = Collections.singletonMap(agg, direction);
            return this;
        }
    }

    public Aggregation addSubAggregation(Aggregation agg) {
        if (aggs == null) aggs = new LinkedHashMap<>();
        aggs.put(agg.field, agg);
        return this;
    }
}
