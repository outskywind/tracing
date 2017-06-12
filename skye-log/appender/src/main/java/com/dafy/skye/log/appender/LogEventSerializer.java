package com.dafy.skye.log.appender;

import com.dafy.skye.log.core.SkyeLogEventCodec;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Created by Caedmon on 2016/4/1.
 */
public class LogEventSerializer implements Serializer<SkyeLogEvent> {
    public LogEventSerializer() {
        super();
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public void close() {

    }

    @Override
    public byte[] serialize(String s, SkyeLogEvent serializable) {
        return SkyeLogEventCodec.DEFAULT.encode(serializable);
    }
}
