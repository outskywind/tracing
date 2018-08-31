package com.dafy.skye.autoconfigure;

import com.dafy.skye.brave.spring.mvc.TraceFilter;
import com.github.kristofa.brave.Brave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * Created by quanchengyun on 2018/8/28.
 */
@Configuration
@AutoConfigureAfter(BraveAutoConfiguration.class)
@ConditionalOnBean(Brave.class)
public class SpringServletFilterAutoConfiguration {
    @Autowired(required = false)
    Brave brave;
    @Bean
    public Filter tracingFilter(){
        TraceFilter tracingFilter = new TraceFilter();
        tracingFilter.setBrave(this.brave);
        return tracingFilter;
    }
    /**
     * 配置过滤器
     * @return
     */
    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(tracingFilter());
        registration.addUrlPatterns("/*");
        registration.setName("tracingFilter");
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }

}
