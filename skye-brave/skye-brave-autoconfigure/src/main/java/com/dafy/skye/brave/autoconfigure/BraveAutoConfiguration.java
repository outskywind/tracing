package com.dafy.skye.brave.autoconfigure;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.Component;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.kafka10.KafkaSender;

/**
 * Created by Caedmon on 2017/6/26.
 */
@Configuration
public class BraveAutoConfiguration {
    @Bean
    public Brave brave(BraveConfigProperties configProperties){
        Brave.Builder builder=new Brave.Builder(configProperties.getServiceName());
        builder.traceSampler(Sampler.create(configProperties.getSamplerRate()));
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
