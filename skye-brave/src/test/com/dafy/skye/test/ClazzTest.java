package com.dafy.skye.test;

import brave.Tracer;
import com.dafy.skye.zipkin.TracingProxy;
import org.junit.Test;
import zipkin2.reporter.Reporter;

/**
 * Created by quanchengyun on 2018/10/17.
 */
public class ClazzTest {


    @Test
    public void   testClazz(){
        ClassLoader loader = ClazzTest.class.getClassLoader();
        try {
            Tracer tracer;
            //TracingProxy.generate("brave.Tracer",loader);
            TracerDepd tp = new TracerDepd(TracingProxy.getProxy("xxx", Reporter.NOOP));
            tp.trace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
