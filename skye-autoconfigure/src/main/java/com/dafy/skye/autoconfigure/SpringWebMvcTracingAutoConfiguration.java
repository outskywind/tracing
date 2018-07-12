package com.dafy.skye.autoconfigure;

import com.dafy.skye.brave.spring.mvc.SimpleBraveTracingInterceptor;
import com.github.kristofa.brave.Brave;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by quanchengyun on 2018/7/12.
 */
@Configuration
@AutoConfigureAfter(BraveAutoConfiguration.class)
public class SpringWebMvcTracingAutoConfiguration {
    @Bean
    @ConditionalOnBean(Brave.class)
    @ConditionalOnClass(name={"org.springframework.web.servlet.handler.HandlerInterceptorAdapter"})
    public SimpleBraveTracingInterceptor simpleBraveTracingInterceptor(Brave brave){
        SimpleBraveTracingInterceptor interceptor = new SimpleBraveTracingInterceptor(brave);
        return interceptor;
    }
}
