package com.dafy.skye.log.core.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Created by Caedmon on 2016/4/15.
 */
public class SkyeLogConverter {
    public static class ServiceNameConvert extends ClassicConverter {

        @Override
        public String convert(ILoggingEvent event) {
            if(event instanceof SkyeLogEvent){
                SkyeLogEvent eventVO=(SkyeLogEvent)event;
                return eventVO.getServiceName();
            }
            return "";
        }
    }
    public static class AddressConvert extends ClassicConverter{
        @Override
        public String convert(ILoggingEvent event) {
            if(event instanceof SkyeLogEvent){
                SkyeLogEvent eventVO=(SkyeLogEvent)event;
                return eventVO.getAddress();
            }
            return "";
        }
    }
    public static class PidConvert extends ClassicConverter{
        @Override
        public String convert(ILoggingEvent event) {
            if(event instanceof SkyeLogEvent){
                SkyeLogEvent eventVO=(SkyeLogEvent)event;
                return eventVO.getPid();
            }
            return "";
        }
    }
}
