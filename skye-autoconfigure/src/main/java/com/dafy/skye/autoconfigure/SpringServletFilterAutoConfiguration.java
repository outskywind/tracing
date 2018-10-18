package com.dafy.skye.autoconfigure;


import brave.Tracing;
import com.dafy.skye.brave.servlet.ServletTraceFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;


import javax.servlet.*;
import java.io.IOException;

/**
 * Created by quanchengyun on 2018/8/28.
 */
@Configuration
@AutoConfigureAfter(BraveAutoConfiguration.class)
@ConditionalOnBean(Servlet.class)
public class SpringServletFilterAutoConfiguration {
    @Autowired(required = false)
    Tracing tracing;

    static Filter Noop_Filter = new NoopFilter();

    static class NoopFilter implements Filter{
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {}
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {}
        @Override
        public void destroy() {}
    }


    @Bean("customTracingFilter")
    public Filter tracingFilter(){
        if(tracing==null){
            return Noop_Filter;
        }
        ServletTraceFilter tracingFilter = new ServletTraceFilter();
        tracingFilter.setTracing(tracing);
        return tracingFilter;
    }

    /**
     * 配置过滤器,所以使用适配器模式，因为原始类不可扩展继承实多态
     * @return
     */
    @Bean
    public FilterRegistrationBean tracingFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(tracingFilter());
        registration.addUrlPatterns("/*");
        registration.setName("tracingFilter");
        //spring 内置的几个servlet filter 例如 charactorEncodingFilter 都是Ordered.HIGHEST_PRECEDENCE
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }


}
