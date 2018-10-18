package com.dafy.skye.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.*;


/**
 * 支持对String 类型的属性值进行自定义的替换
 * Created by quanchengyun on 2018/10/11.
 */
public class EnvironmentRewritePostProcessor implements BeanFactoryPostProcessor,EnvironmentAware,ApplicationContextAware {

    private WrappedEnvironment wrappedEnvironment;
    private ConfigurableApplicationContext context;

    @Override
    public void setEnvironment(Environment environment) {
        if(environment instanceof ConfigurableEnvironment){
            this.wrappedEnvironment = new WrappedEnvironment((ConfigurableEnvironment)environment);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if(this.wrappedEnvironment==null){
            return;
        }
        List<EnvironmentInterceptor> interceptors = new ArrayList<>();
        Map<String,EnvironmentInterceptor> beansOfType = beanFactory.getBeansOfType(EnvironmentInterceptor.class,true,true);
        for(EnvironmentInterceptor interceptor:beansOfType.values()){
            interceptor.setAppName(this.wrappedEnvironment.getProperty("appName"));
            interceptors.add(interceptor);
        }
        //
        Collections.sort(interceptors, Comparator.comparingInt(EnvironmentInterceptor::getOrder));
        this.wrappedEnvironment.setInterceptors(interceptors);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(this.context instanceof ConfigurableApplicationContext){
            this.context = (ConfigurableApplicationContext)applicationContext;
            this.context.setEnvironment(wrappedEnvironment);
        }
    }
}
