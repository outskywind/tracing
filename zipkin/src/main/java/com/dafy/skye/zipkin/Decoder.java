package com.dafy.skye.zipkin;

import zipkin.SpanDecoder;
import zipkin.internal.Util;
import zipkin.internal.V2JsonSpanDecoder;
import zipkin.internal.V2SpanConverter;
import zipkin2.Span;
import zipkin2.codec.SpanBytesDecoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by quanchengyun on 2018/5/8.
 */
public class Decoder {

    static final SpanDecoder JSON2_DECODER = new V2JsonSpanDecoder();
    static final byte[] LOCAL_ENDPOINT_TAG = "\"localEndpoint\"".getBytes(Util.UTF_8);


    private static SpanDecoder detectFormat(byte[] bytes) {
        if (bytes[0] <= 16 /* the first byte is the TType, in a range 0-16 */) {
            return SpanDecoder.THRIFT_DECODER;
        } else if (bytes[0] != '[' && bytes[0] != '{') {
            throw new IllegalArgumentException("Could not detect the span format");
        }
        bytes:
        for (int i = 0; i < bytes.length - LOCAL_ENDPOINT_TAG.length + 1; i++) {
            for (int j = 0; j < LOCAL_ENDPOINT_TAG.length; j++) {
                if (bytes[i + j] != LOCAL_ENDPOINT_TAG[j]) {
                    continue bytes;
                }
            }
            return JSON2_DECODER;
        }
        return SpanDecoder.JSON_DECODER;
    }

    private static List<zipkin2.Span> decodeLegacy(byte[] message){
        zipkin.Span span = SpanDecoder.THRIFT_DECODER.readSpan(message);
        List<Span> span2 = V2SpanConverter.fromSpan(span);
        return span2;
    }

    public static List<zipkin2.Span> decode(byte[] message){
        if (message[0] <= 16 && message[0] != 12 /* thrift, but not a list */) {
            List<zipkin2.Span> span2s = Decoder.decodeLegacy(message);
            return span2s;
        }
        SpanDecoder decoder = detectFormat(message);
        if(decoder instanceof V2JsonSpanDecoder){
            List<Span> out = new ArrayList<>();
            if (!SpanBytesDecoder.JSON_V2.decodeList(message, out)) return Collections.emptyList();
            return out;
        }
        List<zipkin.Span> legacySpans = decoder.readSpans(message);
        List<zipkin2.Span> span2s = new ArrayList<>();
        for(zipkin.Span span: legacySpans){
            span2s.addAll(V2SpanConverter.fromSpan(span));
        }
        return  span2s;
    }


}
