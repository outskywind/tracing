package com.dafy.skye.autoconfigure;

import com.ctrip.framework.apollo.Config;
import com.dafy.skye.brave.ReporterDelegate;
import com.dafy.skye.conf.SkyeDynamicConf;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Sampler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import zipkin.Component;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Reporter;
import zipkin.reporter.kafka10.KafkaSender;

/**
 * Created by Caedmon on 2017/6/26.
 */
@Configuration
public class BraveAutoConfiguration {

    static Logger log  = LoggerFactory.getLogger(BraveAutoConfiguration.class);

    @Bean
    @ConfigurationProperties("skye")
    public BraveConfigProperties braveConfigProperties(){
        return new BraveConfigProperties();
    }

    @Autowired(required = false)
    Config dynamicConfig;

    @Value("${appName}")
    String appName;

    @Bean
    @ConditionalOnClass(Brave.class)
    @Lazy
    //因为 @ConfigurationPorperties 使用Registrar的机制，这里会无法生效
    //因为spring 先处理加载完 BeanMethod 的信息，再加载Registrar的Bean信息
    //@ConditionalOnBean(BraveConfigProperties.class)
    public Brave brave(BraveConfigProperties configProperties){
        Brave.Builder builder;
        //兼容1.7.1-
        if(StringUtils.isNotBlank(appName)){
            configProperties.setServiceName(appName);
        }
        if(StringUtils.isBlank(configProperties.getServiceName())){
            log.warn("appName is empty");
            return null;
        }

        if(StringUtils.isBlank(configProperties.getKafkaServers())){
            log.warn("Brave kafkaServers empty");
            return null;
        }
        //

        builder=new Brave.Builder(configProperties.getServiceName().trim());
        //
        if(configProperties.getSamplerRate()!=null){
            builder.traceSampler(Sampler.create(configProperties.getSamplerRate()));
        }else{
            //默认监控统计全部数据
            builder.traceSampler(Sampler.ALWAYS_SAMPLE);
        }
        Reporter<zipkin.Span> reporter;
        KafkaSender kafkaSender=KafkaSender.create(configProperties.getKafkaServers());
        Component.CheckResult checkResult=kafkaSender.check();
        if(!checkResult.ok){
            log.warn("Kafka Sender check error:   " + checkResult.exception);
            return null;
        }
        reporter= AsyncReporter.builder(kafkaSender).build();
        ReporterDelegate reporterDelegate = new ReporterDelegate(reporter,configProperties.isReport());
        if(dynamicConfig==null){
            dynamicConfig = SkyeDynamicConf.getInstance(configProperties.getServiceName());
        }
        reporterDelegate.setDynamicConfig(dynamicConfig);
        builder.reporter(reporterDelegate);
        return builder.build();
    }

}
