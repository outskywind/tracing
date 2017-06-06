package com.dafy.skye.log.core;

import com.dafy.skye.log.core.logback.SkyeLogEvent;

/**
 * Created by Caedmon on 2017/5/8.
 */
public interface SkyeLogEventCodec {

    JavaSkyeLogEventCodec DEFAULT=new JavaSkyeLogEventCodec();

    SkyeLogEvent decode(byte[] value);

    byte[] encode(SkyeLogEvent event);
}
