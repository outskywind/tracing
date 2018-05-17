package com.dafy.skye.log.server.kafka;

import com.dafy.kafka.MessageConsumer;
import com.dafy.skye.log.core.SkyeLogEventCodec;
import com.dafy.skye.log.server.collector.CollectorDelegate;

import java.util.List;

/**
 * Created by quanchengyun on 2018/5/7.
 */
public class SkyeLogMessageConsumer implements MessageConsumer {

    private CollectorDelegate delegate;

    public void setDelegate(CollectorDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean accept(List<byte[]> message) {
        return delegate.acceptEvents(message, SkyeLogEventCodec.DEFAULT);
    }
}
