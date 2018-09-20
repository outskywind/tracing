package com.dafy.skye.brave;

import com.dafy.base.conf.DynamicConfig;
import zipkin.reporter.Reporter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2018/8/15.
 */
public class ReporterDelegate<T> implements Reporter<T> {

    private Reporter<T> reporter;

    private AtomicBoolean isReport;

    public DynamicConfig getDynamicConfig() {
        return dynamicConfig;
    }

    public void setDynamicConfig(DynamicConfig dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
    }

    private DynamicConfig dynamicConfig;

    public  ReporterDelegate(Reporter<T> reporter , boolean isRePort){
        this.reporter = reporter;
        this.isReport = new AtomicBoolean(isRePort);
    }



    @Override
    public void report(T span) {
        //
        if(null != dynamicConfig){
            if(dynamicConfig.getBooleanProperty(Constants.skye_report_key,true)){
                reporter.report(span);
            }
        }
        if(isReport.get()){
            reporter.report(span);
        }
    }
}
