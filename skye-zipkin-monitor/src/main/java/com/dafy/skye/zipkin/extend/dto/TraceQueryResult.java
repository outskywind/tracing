package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.common.query.QueryResult;
import zipkin.Span;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/21.
 */
public class TraceQueryResult extends QueryResult {
    private List<List<Span>> traces;

    public List<List<Span>> getTraces() {
        return traces;
    }

    public void setTraces(List<List<Span>> traces) {
        this.traces = traces;
    }
}
