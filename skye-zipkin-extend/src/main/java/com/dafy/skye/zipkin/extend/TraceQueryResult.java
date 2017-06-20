package com.dafy.skye.zipkin.extend;

import com.dafy.skye.common.query.QueryResult;
import zipkin.Span;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/20.
 */
public class TraceQueryResult extends QueryResult{
    private List<TraceIntervalStats> stats;
    private List<List<Span>> traces;
    public List<TraceIntervalStats> getStats() {
        return stats;
    }

    public void setStats(List<TraceIntervalStats> stats) {
        this.stats = stats;
    }

    public List<List<Span>> getTraces() {
        return traces;
    }

    public void setTraces(List<List<Span>> traces) {
        this.traces = traces;
    }

    @Override
    public String toString() {
        return "TraceQueryResult{" +
                "stats=" + stats +
                ", traces=" + traces +
                ", took=" + took +
                ", success=" + success +
                ", error='" + error + '\'' +
                ", extra=" + extra +
                '}';
    }
}
