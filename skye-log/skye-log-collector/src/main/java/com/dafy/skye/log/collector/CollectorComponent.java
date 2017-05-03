package com.dafy.skye.log.collector;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Caedmon on 2017/4/16.
 */
public interface CollectorComponent {
    void start();

    void stop();

}
