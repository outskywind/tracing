package com.dafy.skye.klog.collector.autoconfigure;

import org.springframework.context.annotation.Import;

/**
 * Created by Caedmon on 2017/4/24.
 */
@Import(KLogCollectorAutoConfiguration.class)
public @interface EnableKLogCollector {
}
