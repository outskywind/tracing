package com.dafy.skye.brave.spring.mvc;

import com.github.kristofa.brave.Brave;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by quanchengyun on 2017/8/3.
 */
@Configuration
public class SpringMVCAutoConfiguration extends WebMvcConfigurerAdapter {

    /**
     * 因为是继承的，加载这个类必须加载父类，父类不存在就会无法加载
     * @param brave
     * @return
     */
    @Bean
    @ConditionalOnBean(Brave.class)
    @ConditionalOnClass(name={"org.springframework.web.servlet.handler.HandlerInterceptorAdapter"})
    public SimpleBraveTracingInterceptor simpleBraveTracingInterceptor(Brave brave){
        SimpleBraveTracingInterceptor interceptor = new SimpleBraveTracingInterceptor(brave);
        return interceptor;
    }



}
