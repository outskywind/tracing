package com.dafy.skye.autoconfigure;

import com.dafy.skye.context.EnvironmentInterceptor;
import com.dafy.skye.context.EnvironmentRewritePostProcessor;
import com.dafy.skye.context.JDBCEnvironmentInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BeanFactoryPostProcessor Bean 的注册需要单独分开注册
 * Created by quanchengyun on 2018/10/18.
 */
@Configuration
public class BeanFactoryPostProcessorAutoConfiguration {

    @Bean
    public EnvironmentRewritePostProcessor initEnvironmentRewritePostProcessor(){
        return new EnvironmentRewritePostProcessor();
    }

    @Bean("jdbcRewrite")
    @ConditionalOnClass(name="com.mysql.jdbc.StatementInterceptorV2")
    public EnvironmentInterceptor interceptor(){
        JDBCEnvironmentInterceptor interceptor = new JDBCEnvironmentInterceptor();
        //interceptor.setAppName(appName);
        return interceptor;
    }
}
