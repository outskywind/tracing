package com.dafy.skye.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.io.Serializable;

/**
 * Created by Caedmon on 2016/4/1.
 */
public class LogSerializationTransformer implements PreSerializationTransformer<ILoggingEvent> {
    @Override
    public Serializable transform(ILoggingEvent event) {
        if(event==null) return null;
        if(event instanceof LoggingEvent) {
            return SkyeLogEvent.build(event);
        }
        else if(event instanceof SkyeLogEvent) return (SkyeLogEvent)event;
        else throw new IllegalArgumentException("Unsupported type "+event.getClass().getName());
    }
}
