package com.dafy.skye.autoconfigure;

import ch.qos.logback.classic.LoggerContext;
import com.dafy.skye.log.appender.LogKafkaAppender;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.logging.LoggingApplicationListener;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Created by quanchengyun on 2018/5/28.
 */
public class LogConfigurationExtendListener implements GenericApplicationListener {

    //!important attention the order
    public static final int ORDER= LoggingApplicationListener.DEFAULT_ORDER+1;

    private static Class<?>[] EVENT_TYPES = {ApplicationPreparedEvent.class, ApplicationReadyEvent.class};

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
        else if(event instanceof  ApplicationReadyEvent){
            onApplicationReadyEvent((ApplicationReadyEvent) event);
        }

    }

    //此时applicationContext 已经启动完成.但是会错过在这之前已经启动运行的bean的日志。
    //还是应该要在bean初始化前完成日志的配置
    private void onApplicationReadyEvent(ApplicationReadyEvent event) {
        ConfigurableListableBeanFactory beanFactory = event.getApplicationContext()
                .getBeanFactory();
        LoggingSystem loggingSystem = (LoggingSystem)beanFactory.getSingleton(LoggingApplicationListener.LOGGING_SYSTEM_BEAN_NAME);
        if(loggingSystem instanceof LogbackLoggingSystem){
            LoggerContext loggerContext =getLoggerContext();
            //
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            if(rootLogger instanceof ch.qos.logback.classic.Logger){
                ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger)rootLogger;
                LogKafkaAppender appender = new LogKafkaAppender();
                logbackLogger.addAppender(appender);
                String kafkaServers = event.getApplicationContext().getEnvironment().getProperty("skye.kafkaServers");
                String serviceName = event.getApplicationContext().getEnvironment().getProperty("skye.serviceName");
                appender.setKafkaAddress(kafkaServers);
                appender.setServiceName(serviceName);
            }
        }
    }

    // 根据 配置中心的引入策略，此时无法获取到，需要修改引入的方案
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
