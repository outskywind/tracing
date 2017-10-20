package com.dafy.skye.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.storm.kafka.spout.SerializableDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zipkin.Codec;
import zipkin.Span;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static zipkin.storage.Callback.NOOP;

/**
 * Created by quanchengyun on 2017/8/25.
 */
public class SpanCodecComponent<T> implements SerializableDeserializer<T> {

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }
    /**
     * 这个只能用来反序列化Span zipkin使用
     * @param s
     * @param bytes
     * @return
     */
    @Override
    public T deserialize(String s, byte[] bytes) {

        if (bytes[0] == '[') {
            return (T)Codec.JSON.readSpans(bytes);
        } else {
            if (bytes[0] == 12 /* TType.STRUCT */) {
                return (T)Codec.THRIFT.readSpans(bytes);
            } else {
                List<Span> spans = new ArrayList<>(Collections.singletonList(bytes).size());
                try {
                    int bytesRead = 0;
                    for (byte[] serializedSpan : Collections.singletonList(bytes)) {
                        bytesRead += serializedSpan.length;
                        spans.add(Codec.THRIFT.readSpan(serializedSpan));
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                return (T)spans;
            }
        }
    }

    @Override
    public void close() {

    }
}
