package com.dafy.skye.autoconfigure;

import com.dafy.skye.brave.spring.mvc.SimpleBraveTracingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by quanchengyun on 2018/7/12.
 */
@Configuration
@AutoConfigureAfter(SpringWebMvcTracingAutoConfiguration.class)
public class SpringWebMvcAutoConfiguration extends WebMvcConfigurerAdapter {

    @Autowired(required = false)
    SimpleBraveTracingInterceptor tracingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(tracingInterceptor!=null){
            registry.addInterceptor(tracingInterceptor);
        }
    }
}
