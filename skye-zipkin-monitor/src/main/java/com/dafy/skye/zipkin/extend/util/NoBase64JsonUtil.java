package com.dafy.skye.zipkin.extend.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * Created by Caedmon on 2017/6/21.
 */
public class NoBase64JsonUtil {
    private static ObjectMapper noBase64Mapper=new ObjectMapper();
    public static String writeStringNoBase64(Object value){
        if(noBase64Mapper==null){
            Jackson2ObjectMapperBuilder builder=Jackson2ObjectMapperBuilder.json();
            builder.serializerByType(byte[].class, new JsonSerializer<byte[]>() {
                @Override
                public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException
                {
                    gen.writeString(new String(value));
                }
            });
            noBase64Mapper=builder.build();
        }
        try {
            return noBase64Mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
