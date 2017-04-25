package com.dafy.skye.autoconfigure.log.collector;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by Caedmon on 2017/4/24.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CollectorAutoConfiguration.class)
public @interface EnableSkyeLogCollector {
}
