package com.dafy.skye.zipkin.extend;

import com.dafy.skye.zipkin.IDGenerator;
import com.dafy.skye.zipkin.IndexNameFormatter;
import com.dafy.skye.zipkin.zipkincopy.BodyConverters;
import com.dafy.skye.zipkin.zipkincopy.ElasticsearchSpanStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Call;
import zipkin2.Span;
import zipkin2.elasticsearch.ElasticsearchStorage;
import zipkin2.elasticsearch.internal.client.HttpCall;
import zipkin2.elasticsearch.internal.client.SearchRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Created by quanchengyun on 2018/10/16.
 */
public class CustomSpanStore extends ElasticsearchSpanStore {

    Logger log = LoggerFactory.getLogger(CustomSpanStore.class);

    IndexNameFormatter indexNameFormatter;
    public CustomSpanStore(ElasticsearchStorage es, boolean searchEnabled,IndexNameFormatter indexNameFormatter) {
        super(es, searchEnabled,indexNameFormatter);
        this.indexNameFormatter = indexNameFormatter;
        //super.indexNameFormatter = indexNameFormatter;
    }


    @Override
    public Call<List<Span>> getTrace(String traceId) {
        // make sure we have a 16 or 32 character trace ID
        traceId = Span.normalizeTraceId(traceId);

        // Unless we are strict, truncate the trace ID to 64bit (encoded as 16 characters)
        //if (!strictTraceId && traceId.length() == 32) traceId = traceId.substring(16);
        //long traceIdL = Long.decode( "0x"+traceId);
        long traceIdL = Long.parseUnsignedLong(traceId,16);
        long timestamp = IDGenerator.extractTimestamp(traceIdL);
        List<String> indices ;
        try{
             indices = indexNameFormatter.formatTypeAndRange(SPAN, timestamp, timestamp+600000L);
             //首先排除掉明显的traceId
        }catch (NumberFormatException e){
             indices = Arrays.asList(indexNameFormatter.formatType(SPAN));
        }
        SearchRequest request = SearchRequest.create(indices).term("traceId", traceId);
        HttpCall<List<Span>> call = search.newCall(request, BodyConverters.SPANS);
        try{
            List<Span> result = call.execute();
            if(result!=null && !result.isEmpty()){
                //return call;
                 request = SearchRequest.create(indices).term("traceId", traceId);
                 return search.newCall(request, BodyConverters.SPANS);
            }
        } catch (IOException e) {
            //
        }
        //at last try if the traceId is random number but get the time range
        indices = Arrays.asList(indexNameFormatter.formatType(SPAN));
        request = SearchRequest.create(indices).term("traceId", traceId);
        return search.newCall(request, BodyConverters.SPANS);
    }




}
