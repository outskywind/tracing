package com.dafy.skye.log.collector.storage.rolling;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.google.common.base.Strings;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
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
        private static final String DEFAULT_FILE_NAME_PATTERN="logs/%sn/%addr-%d{yyyy-MM-dd}.log";
        private static final String DEFAULT_LOG_PATTERN="[%sn-%pid@%addr] [%t] %-5level %d{yyyy-MM-dd HH:mm:ss.SSS} %logger{50} %L - %m%n";
        private static final String DEFAULT_LOG_DIR="logs";
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
