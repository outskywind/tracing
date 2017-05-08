package com.dafy.skye.log.core;

import com.dafy.skye.log.core.logback.SkyeLogEvent;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/1.
 */
public class SkyeLogEventDeserializer implements Deserializer<SkyeLogEvent>{
    public SkyeLogEventDeserializer() {
        super();
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public SkyeLogEvent deserialize(String s, byte[] bytes) {
        return SkyeLogEventCodec.DEFAULT.decode(bytes);
    }

    @Override
    public void close() {

    }
}
