package com.dafy.skye.autoconfigure;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Sampler;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import zipkin.Component;
import zipkin.reporter.AsyncReporter;
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

    @Bean
    @ConditionalOnClass(Brave.class)
    @Lazy
    //因为 @ConfigurationPorperties 使用Registrar的机制，这里会无法生效
    //因为spring 先处理加载完 BeanMethod 的信息，再加载Registrar的Bean信息
    //@ConditionalOnBean(BraveConfigProperties.class)
    public Brave brave(BraveConfigProperties configProperties){
        if(Strings.isNullOrEmpty(configProperties.getServiceName())){
            log.warn("Brave service name is empty");
            return null;
        }
        if(Strings.isNullOrEmpty(configProperties.getKafkaServers())){
            log.warn("Brave kafkaServers empty");
            return null;
        }
        Brave.Builder builder=new Brave.Builder(configProperties.getServiceName());
        //
        if(configProperties.getSamplerRate()!=null){
            builder.traceSampler(Sampler.create(configProperties.getSamplerRate()));
        }else{
            //默认监控统计全部数据
            builder.traceSampler(Sampler.ALWAYS_SAMPLE);
        }
        KafkaSender kafkaSender=KafkaSender.create(configProperties.getKafkaServers());
        Component.CheckResult checkResult=kafkaSender.check();
        if(!checkResult.ok){
            System.out.println("Kafka Sender check error:   " + checkResult.exception);
            return null;
        }
        AsyncReporter.Builder reporter= AsyncReporter.builder(kafkaSender);
        builder.reporter(reporter.build());
        return builder.build();
    }

}
