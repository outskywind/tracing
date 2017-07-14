package com.dafy.skye.brave.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by Caedmon on 2017/6/27.
 */
@Target(ElementType.TYPE)
//重要不然运行解析后不会再metadata 信息里
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({BraveAutoConfiguration.class})
public @interface EnableBraveTracing {
}
