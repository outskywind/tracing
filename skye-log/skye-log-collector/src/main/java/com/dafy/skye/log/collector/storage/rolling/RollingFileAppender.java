package com.dafy.skye.log.collector.storage.rolling;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.SkyeLogTimeBasedRollingPolicy;
import com.dafy.skye.log.core.logback.SkyeLogConverter;
import com.google.common.base.Strings;

import java.io.File;

/**
 * Created by Caedmon on 2016/4/18.
 */
public class RollingFileAppender extends ch.qos.logback.core.rolling.RollingFileAppender {
    static {
        PatternLayout.defaultConverterMap.put("sn",SkyeLogConverter.ServiceNameConvert.class.getName());
        PatternLayout.defaultConverterMap.put("addr", SkyeLogConverter.AddressConvert.class.getName());
        PatternLayout.defaultConverterMap.put("pid",SkyeLogConverter.PidConvert.class.getName());
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
        SkyeLogTimeBasedRollingPolicy policy=new SkyeLogTimeBasedRollingPolicy(
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
