package com.dafy.skye.zipkin.extend.config;

import com.dafy.skye.zipkin.extend.web.filter.UserChangeFilter;
import com.dafy.skye.zipkin.extend.web.filter.UserFilter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by quanchengyun on 2018/4/12.
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    /**
     * @see org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration
     */
    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES,false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Bean
    @Order(1)
    public FilterRegistrationBean userFilterBean(UserFilter filter){
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("userFilter");
        return registration;
    }

    @Bean
    @Order(2)
    public FilterRegistrationBean userChangeFilterBean(UserChangeFilter filter){
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.addUrlPatterns("/service/follow","/service/unfollow");
        registration.setName("userChangeFilter");
        return registration;
    }


}
