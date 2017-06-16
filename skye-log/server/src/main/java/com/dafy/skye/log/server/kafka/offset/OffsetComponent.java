package com.dafy.skye.log.server.kafka.offset;

import com.dafy.skye.log.server.collector.CollectorComponent;

/**
 * Created by Caedmon on 2017/3/2.
 */
public interface OffsetComponent extends CollectorComponent {

    void setOffset(String partitionKey,long offset);

    long getOffset(String partitionKey);
}
