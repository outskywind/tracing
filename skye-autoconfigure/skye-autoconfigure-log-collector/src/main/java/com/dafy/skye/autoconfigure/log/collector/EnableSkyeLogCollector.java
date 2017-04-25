package com.dafy.skye.autoconfigure.log.collector;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by Caedmon on 2017/4/24.
 */
@Import(CollectorAutoConfiguration.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableSkyeLogCollector {
}
