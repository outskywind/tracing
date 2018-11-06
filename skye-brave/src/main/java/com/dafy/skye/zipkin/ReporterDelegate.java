package com.dafy.skye.zipkin;

import com.ctrip.framework.apollo.Config;
import com.dafy.base.conf.DynamicConfConstants;
import zipkin2.reporter.Reporter;


import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2018/8/15.
 */
public class ReporterDelegate<T> implements Reporter<T> {

    private Reporter<T> reporter;

    private AtomicBoolean isReport;

    private Config dynamicConfig;

    public ReporterDelegate(Reporter<T> reporter, boolean isRePort) {
        this.reporter = reporter;
        this.isReport = new AtomicBoolean(isRePort);
    }

    @Override
    public void report(T span) {
        if (null != dynamicConfig ? dynamicConfig.getBooleanProperty(DynamicConfConstants.skye_report_key, isReport.get()) : isReport.get()) {
            reporter.report(span);
        }
    }

    public Config getDynamicConfig() {
        return dynamicConfig;
    }

    public void setDynamicConfig(Config dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
    }
}
