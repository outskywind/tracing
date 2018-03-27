package com.dafy.skye.conf;


import java.lang.annotation.*;

/**
 * Created by quanchengyun on 2018/3/19.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationCenterPath {

    String value();

    String host();

    String protocal() default "http";

    //String format() default "properties";

    boolean overrideLocal() default true;
}
