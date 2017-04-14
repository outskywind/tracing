package com.dafy.skye.klog.collector.storage.file;

import ch.qos.logback.classic.LoggerContext;

import java.util.Properties;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class RollingFileStorageConfig {
    public String fileNamePattern;
    public String logPattern;
    public String logDir;
    public LoggerContext loggerContext;
    public String getFileNamePattern() {
        return fileNamePattern;
    }

    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    public String getLogDir() {
        return logDir;
    }

    public LoggerContext getLoggerContext() {
        return loggerContext;
    }

    public void setLoggerContext(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }
    public static class Builder{
        private String fileNamePattern;
        private String logPattern;
        private String logDir;
        private LoggerContext loggerContext;
        public Builder load(Properties properties,LoggerContext loggerContext){
            String fileNamePattern=properties.getProperty("skye-collector.fileName.pattern");
            String logPattern=properties.getProperty("skye-collector.log.pattern");
            String logDir=properties.getProperty("skye-collector.log.dir");
            this.fileNamePattern(fileNamePattern);
            this.logPattern(logPattern);
            this.logDir(logDir);
            this.loggerContext(loggerContext);
            return this;
        }
        public static Builder create(Properties properties,LoggerContext loggerContext){
            Builder builder=create();
            builder.load(properties,loggerContext);
            return builder;
        }
        public  static Builder create(){
            return new Builder();
        }
        public Builder fileNamePattern(String fileNamePattern){
            this.fileNamePattern=fileNamePattern;
            return this;
        }
        public Builder logPattern(String logPattern){
            this.logPattern=logPattern;
            return this;
        }
        public Builder logDir(String logDir){
            this.logDir=logDir;
            return this;
        }
        public Builder loggerContext(LoggerContext loggerContext){
            this.loggerContext=loggerContext;
            return this;
        }
        public RollingFileStorageConfig build(){
            RollingFileStorageConfig config=new RollingFileStorageConfig();
            config.fileNamePattern=this.fileNamePattern;
            config.logPattern=this.logPattern;
            config.logDir=this.logDir;
            config.loggerContext=this.loggerContext;
            return config;
        }
    }
}
