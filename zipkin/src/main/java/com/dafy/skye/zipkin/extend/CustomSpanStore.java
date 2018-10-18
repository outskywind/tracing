package com.dafy.skye.zipkin.extend;

import com.dafy.skye.zipkin.IDGenerator;
import com.dafy.skye.zipkin.zipkincopy.BodyConverters;
import com.dafy.skye.zipkin.zipkincopy.ElasticsearchSpanStore;
import zipkin2.Call;
import zipkin2.Span;
import zipkin2.elasticsearch.ElasticsearchStorage;
import zipkin2.elasticsearch.internal.IndexNameFormatter;
import zipkin2.elasticsearch.internal.client.SearchRequest;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by quanchengyun on 2018/10/16.
 */
public class CustomSpanStore extends ElasticsearchSpanStore {

    IndexNameFormatter indexNameFormatter;
    public CustomSpanStore(ElasticsearchStorage es, boolean searchEnabled) {
        super(es, searchEnabled);
        this.indexNameFormatter = es.indexNameFormatter();
    }



    @Override
    public Call<List<Span>> getTrace(String traceId) {
        // make sure we have a 16 or 32 character trace ID
        traceId = Span.normalizeTraceId(traceId);

        // Unless we are strict, truncate the trace ID to 64bit (encoded as 16 characters)
        //if (!strictTraceId && traceId.length() == 32) traceId = traceId.substring(16);
        long traceIdL = Long.decode(traceId);
        long timestamp = IDGenerator.extractTimestamp(traceIdL);
        List<String> indices = indexNameFormatter.formatTypeAndRange(SPAN, timestamp, timestamp+3600000L);

        SearchRequest request = SearchRequest.create(indices).term("traceId", traceId);
        return search.newCall(request, BodyConverters.SPANS);
    }
}
