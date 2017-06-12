package com.dafy.skye.log.collector.storage.elasticsearch.internal;


import java.util.*;

/**
 * Created by Caedmon on 2017/6/5.
 */
public final class ESearchRequest {
    /**
     * The maximum results returned in a query. This only affects non-aggregation requests.
     *
     * <p>Not configurable as it implies adjustments to the index template (index.max_result_window)
     *
     * <p> See https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-from-size.html
     */
    static final int MAX_RESULT_WINDOW = 10000; // the default elasticsearch allowed limit

    transient final List<String> indices;
    transient final String type;

    Integer size = MAX_RESULT_WINDOW;
    Boolean _source;
    Object query;
    Map<String, Aggregation> aggs;
    List<Map<String,Object>> sort;
    ESearchRequest(List<String> indices, String type) {
        this.indices = indices;
        this.type = type;
        this._source=true;
    }

    public static class Filters extends LinkedList<Object> {
        public Filters addRange(String field, long from, Long to) {
            add(new Range(field, from, to));
            return this;
        }
        public Filters addDateRange(String field,String gte,String lte,String format){
            add(new FormatDateRange(field,format,gte,lte));
            return this;
        }

        public Filters addTerm(String field, String value) {
            add(new Term(field, value));
            return this;
        }
        public Filters addTerms(String field,List<String> values){
            add(new Terms(field, values));
            return this;
        }
        public Filters addNestedTerms(Collection<String> nestedFields, String value) {
            add(_nestedTermsEqual(nestedFields, value));
            return this;
        }

        public Filters addNestedTerms(Map<String, String> nestedTerms) {
            List<ESearchRequest.Term> terms = new ArrayList<>();
            String field = null;
            for (Map.Entry<String, String> nestedTerm : nestedTerms.entrySet()) {
                terms.add(new Term(field = nestedTerm.getKey(), nestedTerm.getValue()));
            }
            add(new NestedBoolQuery(field.substring(0, field.indexOf('.')), "must", terms));
            return this;
        }
    }

    public ESearchRequest filters(Filters filters) {
        return query(new BoolQuery("must", filters));
    }

    static ESearchRequest.BoolQuery _nestedTermsEqual(Collection<String> nestedFields, String value) {
        List<ESearchRequest.NestedBoolQuery> conditions = new ArrayList<>();
        for (String nestedField : nestedFields) {
            conditions.add(new NestedBoolQuery(nestedField.substring(0, nestedField.indexOf('.')), "must",
                    new ESearchRequest.Term(nestedField, value)));
        }
        return new ESearchRequest.BoolQuery("should", conditions);
    }

    public static ESearchRequest forIndicesAndType(List<String> indices, String type) {
        return new ESearchRequest(indices, type);
    }

    public ESearchRequest term(String field, String value) {
        return query(new Term(field, value));
    }

    public ESearchRequest terms(String field, List<String> values) {
        return query(new Terms(field, values));
    }

    public ESearchRequest addAggregation(Aggregation agg) {
        size = null; // we return aggs, not source data
        _source = false;
        if (aggs == null) aggs = new LinkedHashMap<>();
        aggs.put(agg.field, agg);
        return this;
    }

    String tag() {
        return aggs != null ? "aggregation" : "search";
    }

    ESearchRequest query(Object filter) {
        query = Collections.singletonMap("bool", Collections.singletonMap("filter", filter));
        return this;
    }
    public ESearchRequest addSort(String field, SortOrder order){
        if(this.sort==null){
            this.sort=new ArrayList<>();
        }
        Map<String,Object> map=new HashMap<>();
        map.put(field,order);
        this.sort.add(map);
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean get_source() {
        return _source;
    }

    public void set_source(Boolean _source) {
        this._source = _source;
    }

    public Object getQuery() {
        return query;
    }

    public void setQuery(Object query) {
        this.query = query;
    }

    public Map<String, Aggregation> getAggs() {
        return aggs;
    }

    public void setAggs(Map<String, Aggregation> aggs) {
        this.aggs = aggs;
    }

    public List<Map<String, Object>> getSort() {
        return sort;
    }

    public void setSort(List<Map<String, Object>> sort) {
        this.sort = sort;
    }

    static class Term {
        final Map<String, String> term;

        Term(String field, String value) {
            term = Collections.singletonMap(field, value);
        }

        public Map<String, String> getTerm() {
            return term;
        }
    }

    static class Terms {
        final Map<String, List<String>> terms;

        Terms(String field, List<String> values) {
            this.terms = Collections.singletonMap(field, values);
        }

        public Map<String, List<String>> getTerms() {
            return terms;
        }
    }
    static class FormatDateRange {
        final Map<String,DateBounds> range;
        FormatDateRange(String field, String format, String gte, String lte) {
            range = Collections.singletonMap(field, new FormatDateRange.DateBounds(gte, lte,format));
        }

        public Map<String, DateBounds> getRange() {
            return range;
        }

        static class DateBounds{
            final String gte;
            final String lte;
            final String format;
            public DateBounds(String gte, String lte,String format) {
                this.gte = gte;
                this.lte = lte;
                this.format=format;
            }

            public String getGte() {
                return gte;
            }

            public String getLte() {
                return lte;
            }
        }
    }
    static class Range {
        final Map<String, Bounds> range;

        Range(String field, long from, Long to) {
            range = Collections.singletonMap(field, new Bounds(from, to));
        }

        static class Bounds {
            final long from;
            final Long to;
            final boolean include_lower = true;
            final boolean include_upper = true;

            Bounds(long from, Long to) {
                this.from = from;
                this.to = to;
            }

            public long getFrom() {
                return from;
            }

            public Long getTo() {
                return to;
            }

            public boolean isInclude_lower() {
                return include_lower;
            }

            public boolean isInclude_upper() {
                return include_upper;
            }
        }

        public Map<String, Bounds> getRange() {
            return range;
        }
    }

    static class NestedBoolQuery {
        final Map<String, Object> nested;

        NestedBoolQuery(String path, String condition, List<Term> terms) {
            nested = new LinkedHashMap<>(2);
            nested.put("path", path);
            nested.put("query", new BoolQuery(condition, terms));
        }

        NestedBoolQuery(String path, String condition, Term term) {
            nested = new LinkedHashMap<>(2);
            nested.put("path", path);
            nested.put("query", new BoolQuery(condition, term));
        }

        public Map<String, Object> getNested() {
            return nested;
        }
    }

    static class BoolQuery {
        final Map<String, Object> bool;

        BoolQuery(String op, Object clause) {
            bool = Collections.singletonMap(op, clause);
        }

        public Map<String, Object> getBool() {
            return bool;
        }
    }
    public static class SortOrder {
        final String order;
        final String mode;
        public SortOrder(String order, String mode){
            this.order=order;
            this.mode=mode;
        }

        public String getOrder() {
            return order;
        }

        public String getMode() {
            return mode;
        }
    }
}