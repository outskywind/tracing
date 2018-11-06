package com.dafy.skye.autoconfigure;

import com.dafy.skye.context.PropertySourceInterceptor;
import com.dafy.skye.context.EnvironmentRewritePostProcessor;
import com.dafy.skye.context.JDBCPropertySourceInterceptor;
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
    @ConditionalOnClass(name={"com.mysql.jdbc.StatementInterceptorV2","brave.mysql.TracingStatementInterceptor"})
    public PropertySourceInterceptor interceptor(){
        JDBCPropertySourceInterceptor interceptor = new JDBCPropertySourceInterceptor();
        //interceptor.setAppName(appName);
        return interceptor;
    }
}
