package com.dafy.skye.component;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2017/10/24.
 */
public class LogBuilder {
    //在 hadoop yarn 容器上运行程序时的文件系统接口无法直接访问本地文件及目录
    //默认配置的log就无法输出到文件上，需要重新自定义appender 调用 hadoop的文件系统接口输出到文件
    private static final AtomicBoolean initialed= new AtomicBoolean(true);

    public static <T> Logger buildLogger(Class<T> logClass, String logfile ,String fileNamePattern, String pattern){
        //Logger log = LoggerFactory.getLogger(logClass);
        Logger rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        if(rootLogger instanceof ch.qos.logback.classic.Logger){
            //logger 设置对应的appender
            ch.qos.logback.classic.Logger rootLogback = (ch.qos.logback.classic.Logger)rootLogger;
            //ch.qos.logback.classic.Logger logbackLog = (ch.qos.logback.classic.Logger)log;
            LoggerContext rootContext = rootLogback.getLoggerContext();
            if(!initialed.get()){
                //appender
                RollingFileAppender fileAppender = new RollingFileAppender<>();
                //set name
                fileAppender.setName("file");
                //set context first
                fileAppender.setContext(rootContext);
                fileAppender.setFile(logfile);
                //encoder
                PatternLayoutEncoder encoder = new PatternLayoutEncoder();
                encoder.setPattern(pattern);
                encoder.setContext(rootContext);
                encoder.start();
                fileAppender.setEncoder(encoder);
                //rollingpolicy
                ch.qos.logback.core.rolling.TimeBasedRollingPolicy rollingPolicy = new ch.qos.logback.core.rolling.TimeBasedRollingPolicy();
                rollingPolicy.setFileNamePattern(fileNamePattern);

                // parent and context are required
                rollingPolicy.setParent(fileAppender);
                rollingPolicy.setContext(rootContext);

                rollingPolicy.start();
                fileAppender.setRollingPolicy(rollingPolicy);
                //add to root logger
                rootLogback.addAppender(fileAppender);
                initialed.compareAndSet(false,true);
                //start the appender
                fileAppender.start();
            }
            //get the child logger
            return rootContext.getLogger(logClass);
        }
        //should we support other log frameworks?
        return rootLogger;
    }

    public static <T> Logger buildLogger(Class<T> logClass){
        //Logger log = LoggerFactory.getLogger(logClass);
        Logger rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        if(rootLogger instanceof ch.qos.logback.classic.Logger && initialed.get()){
            //logger 设置对应的appender
            ch.qos.logback.classic.Logger rootLogback = (ch.qos.logback.classic.Logger)rootLogger;
            LoggerContext rootContext = rootLogback.getLoggerContext();
            //get the child logger
            return rootContext.getLogger(logClass);
        }
        //should we support other log frameworks?
        return rootLogger;
    }

}
