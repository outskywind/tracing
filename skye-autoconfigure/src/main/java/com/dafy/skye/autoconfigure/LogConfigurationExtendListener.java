package com.dafy.skye.autoconfigure;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.dafy.skye.log.appender.LogKafkaAppender;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.logging.LoggingApplicationListener;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Iterator;

/**
 * Created by quanchengyun on 2018/5/28.
 */
public class LogConfigurationExtendListener implements GenericApplicationListener {

    //!important attention the order
    public static final int ORDER= LoggingApplicationListener.DEFAULT_ORDER+1;

    private static Class<?>[] EVENT_TYPES = {ApplicationPreparedEvent.class, ContextRefreshedEvent.class};

    private static Class<?>[] SOURCE_TYPES = { SpringApplication.class,
            ApplicationContext.class };


    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        return isAssignableFrom(resolvableType.getRawClass(), EVENT_TYPES);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        if (type != null) {
            for (Class<?> supportedType : supportedTypes) {
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        //just do it
        if(event instanceof ApplicationPreparedEvent){
            onApplicationPreparedEvent((ApplicationPreparedEvent) event);
        }
        else if(event instanceof  ContextRefreshedEvent){
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        }

    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {

        String report = event.getApplicationContext().getEnvironment().getProperty("skye.report");

        ApplicationContext context = event.getApplicationContext();
        LoggingSystem loggingSystem = (LoggingSystem)context.getBean(LoggingApplicationListener.LOGGING_SYSTEM_BEAN_NAME);
        if(loggingSystem instanceof LogbackLoggingSystem){
            LoggerContext loggerContext =getLoggerContext();
            //
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            if(rootLogger instanceof ch.qos.logback.classic.Logger){
                ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger)rootLogger;
                Iterator<Appender<ILoggingEvent>> appenders = logbackLogger.iteratorForAppenders();
                boolean isAttached = false;
                while(appenders.hasNext()){
                    if(appenders.next() instanceof LogKafkaAppender){
                        isAttached = true;
                        break;
                    }
                }
                if(!isAttached){
                    LogKafkaAppender appender = new LogKafkaAppender();
                    logbackLogger.addAppender(appender);
                    String kafkaServers = event.getApplicationContext().getEnvironment().getProperty("skye.kafkaServers");
                    String serviceName = event.getApplicationContext().getEnvironment().getProperty("skye.serviceName");
                    if(StringUtils.isEmpty(kafkaServers)){
                        kafkaServers = event.getApplicationContext().getEnvironment().getProperty("skye.kafka-servers");
                    }
                    if(StringUtils.isEmpty(serviceName)){
                        serviceName = event.getApplicationContext().getEnvironment().getProperty("skye.service-name");
                    }
                    if("false".equalsIgnoreCase(report)){
                        appender.setReport(false);
                    }
                    if(StringUtils.isEmpty(serviceName)){
                        return ;
                    }
                    appender.setKafkaAddress(kafkaServers);
                    appender.setServiceName(serviceName.trim());
                    appender.setContext(loggerContext);
                    appender.setName("skye");
                    appender.start();
                }
            }
        }
    }

    // 根据 配置中心客户端的引入策略，此时无法获取到，需要修改引入的方案
    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        //...
    }


    private LoggerContext getLoggerContext() {
        ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        Assert.isInstanceOf(LoggerContext.class, factory,
                String.format(
                        "LoggerFactory is not a Logback LoggerContext but Logback is on "
                                + "the classpath. Either remove Logback or the competing "
                                + "implementation (%s loaded from %s). If you are using "
                                + "WebLogic you will need to add 'org.slf4j' to "
                                + "prefer-application-packages in WEB-INF/weblogic.xml",
                        factory.getClass(), getLocation(factory)));
        return (LoggerContext) factory;
    }

    private Object getLocation(ILoggerFactory factory) {
        try {
            ProtectionDomain protectionDomain = factory.getClass().getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource != null) {
                return codeSource.getLocation();
            }
        }
        catch (SecurityException ex) {
            // Unable to determine location
        }
        return "unknown location";
    }


    @Override
    public int getOrder() {
        return ORDER;
    }
}
