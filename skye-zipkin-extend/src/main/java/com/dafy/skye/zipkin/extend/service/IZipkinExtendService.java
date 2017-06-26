package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.zipkin.extend.dto.*;

import java.util.List;
import java.util.Set;

/**
 * Created by Caedmon on 2017/6/20.
 */
public interface IZipkinExtendService {

    Set<String> getServices(ServiceNameQueryRequest request);

    Set<String> getSpans(SpanNameQueryRequest request);

    TraceMetricsResult getTracesMetrics(TraceQueryRequest request);

    TraceQueryResult getTraces(TraceQueryRequest request);

    SpanMetricsResult getSpansMetrics(TraceQueryRequest request);

    //Service被调用次数
    //接口成功失败

}
