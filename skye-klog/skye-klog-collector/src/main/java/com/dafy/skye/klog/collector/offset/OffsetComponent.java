package com.dafy.skye.klog.collector.offset;

import com.dafy.skye.klog.collector.CollectorComponent;

/**
 * Created by Caedmon on 2017/3/2.
 */
public interface OffsetComponent extends CollectorComponent{

    void setOffset(long offset);

    long getOffset();
}
