package com.dafy.skye.klog.collector.autoconfigure;

import com.dafy.skye.klog.collector.CollectorController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Caedmon on 2017/4/24.
 */
@Configuration
@EnableConfigurationProperties(KLogCollectorConfigProperties.class)
public class KLogCollectorAutoConfiguration {
    @Bean
    public CollectorController collectorController(KLogCollectorConfigProperties collectorProperties){
        return null;
    }
}
