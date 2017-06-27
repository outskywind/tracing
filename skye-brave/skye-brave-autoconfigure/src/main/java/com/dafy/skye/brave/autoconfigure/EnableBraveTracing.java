package com.dafy.skye.brave.autoconfigure;

import org.springframework.context.annotation.Import;

/**
 * Created by Caedmon on 2017/6/27.
 */
@Import({BraveConfigProperties.class,BraveAutoConfiguration.class})
public @interface EnableBraveTracing {
}
