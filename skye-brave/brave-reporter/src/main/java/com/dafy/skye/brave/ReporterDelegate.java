package com.dafy.skye.brave;

import zipkin.reporter.Reporter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2018/8/15.
 */
public class ReporterDelegate<T> implements Reporter<T> {

    private Reporter<T> reporter;

    private AtomicBoolean isReport;

    public  ReporterDelegate(Reporter<T> reporter , boolean isRePort){
        this.reporter = reporter;
        this.isReport = new AtomicBoolean(isRePort);
    }
    @Override
    public void report(T span) {
        if(isReport.get()){
            reporter.report(span);
        }
    }
}
