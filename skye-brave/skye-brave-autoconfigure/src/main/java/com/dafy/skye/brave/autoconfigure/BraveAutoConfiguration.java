package com.dafy.skye.brave.autoconfigure;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Sampler;
import com.google.common.base.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.Component;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.kafka10.KafkaSender;

/**
 * Created by Caedmon on 2017/6/26.
 */
@Configuration
@EnableConfigurationProperties(BraveConfigProperties.class)
public class BraveAutoConfiguration {
    @Bean
    @ConditionalOnClass(Brave.class)
    @ConditionalOnBean(BraveConfigProperties.class)
    public Brave brave(BraveConfigProperties configProperties){
        if(Strings.isNullOrEmpty(configProperties.getServiceName())){
            throw new IllegalStateException("Brave service name is empty");
        }
        if(Strings.isNullOrEmpty(configProperties.getKafkaServers())){
            throw new IllegalStateException("Brave kafkaServers empty");
        }
        Brave.Builder builder=new Brave.Builder(configProperties.getServiceName());
        //
        if(configProperties.getSamplerRate()!=null){
            builder.traceSampler(Sampler.create(configProperties.getSamplerRate()));
        }
        //默认监控统计全部数据
        builder.traceSampler(Sampler.ALWAYS_SAMPLE);
        KafkaSender kafkaSender=KafkaSender.create(configProperties.getKafkaServers());
        Component.CheckResult checkResult=kafkaSender.check();
        if(!checkResult.ok){
            throw new IllegalStateException("Kafka Sender check error ",checkResult.exception);
        }
        AsyncReporter.Builder reporter= AsyncReporter.builder(kafkaSender);
        builder.reporter(reporter.build());
        return builder.build();
    }
}
