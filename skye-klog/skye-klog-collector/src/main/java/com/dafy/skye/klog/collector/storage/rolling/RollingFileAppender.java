package com.dafy.skye.klog.collector.storage.rolling;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.KLogTimeBasedRollingPolicy;
import com.dafy.skye.klog.core.logback.KLogConverter;
import com.google.common.base.Strings;

import java.io.File;

/**
 * Created by Caedmon on 2016/4/18.
 */
public class RollingFileAppender extends ch.qos.logback.core.rolling.RollingFileAppender {
    static {
        PatternLayout.defaultConverterMap.put("sn",KLogConverter.ServiceNameConvert.class.getName());
        PatternLayout.defaultConverterMap.put("addr", KLogConverter.AddressConvert.class.getName());
        PatternLayout.defaultConverterMap.put("pid",KLogConverter.PidConvert.class.getName());
    }
    private String serviceName;
    private String address;
    private String appenderName;
    private RollingFileStorageConfig config;
    private static final String FILE_SP=File.separator;
    public RollingFileAppender(String appenderName, String serviceName,
                               String address, RollingFileStorageConfig config){
        this.appenderName=appenderName;
        this.serviceName=serviceName;
        this.address=address;
        this.config=config;
    }
    public void start(){
        if(isStarted()){
            return;
        }
        setContext(config.loggerContext);
        String logDir=config.logDir;
        setName(appenderName);
        if(Strings.isNullOrEmpty(logDir)){
            logDir="logs";
        }
        setFile(logDir+ FILE_SP+serviceName+FILE_SP+serviceName+"-"+address+".log");
        KLogTimeBasedRollingPolicy policy=new KLogTimeBasedRollingPolicy(
                this.serviceName,this.address
        );
        policy.setFileNamePattern(config.fileNamePattern);
        policy.setParent(this);
        policy.setContext(context);
        policy.start();
        this.setRollingPolicy(policy);
        PatternLayoutEncoder encoder=new PatternLayoutEncoder();
        encoder.setContext(context);
        setEncoder(encoder);
        encoder.setPattern(config.logPattern);
        encoder.start();
        super.start();
    }
}
