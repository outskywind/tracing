package com.dafy.skye.zipkin.extend.aop;


import java.lang.annotation.*;

/**
 * Created by quanchengyun on 2018/4/27.
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pointcut {

    /**
     * methodName
     * methodName(args...)
     * @return
     */
    String[] value() default{};

}
