package com.dafy.skye.zipkin.extend.config;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by quanchengyun on 2018/11/2.
 */
@Configuration
public class UndertowConfig {


    @Bean
    public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
        factory.addBuilderCustomizers((UndertowBuilderCustomizer) builder ->
                builder.setServerOption(UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE,true));
        return factory;
    }
}
