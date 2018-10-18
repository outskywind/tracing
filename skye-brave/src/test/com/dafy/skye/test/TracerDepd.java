package com.dafy.skye.test;

import brave.Tracer;
import brave.Tracing;

/**
 * Created by quanchengyun on 2018/10/17.
 */
public class TracerDepd {


    Tracer tracer;


    public  TracerDepd(Tracing tracing) {
        this.tracer=tracing.tracer();
    }

    public void trace(){
        tracer.newTrace();
    }
}
