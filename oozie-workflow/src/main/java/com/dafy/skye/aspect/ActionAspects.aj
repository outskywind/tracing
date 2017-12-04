package com.dafy.skye.aspect;

/**
 * Created by quanchengyun on 2017/11/2.
 */
aspect ActionAspects {
    // 定义一个切入点，表示针对此方法进行aop 织入
    pointcut  backup(): execution(* _doAction(..)) ;

    before(): backup(){

    }

}
