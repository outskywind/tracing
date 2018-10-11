package com.dafy.skye.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.*;


/**
 * 支持对String 类型的属性值进行自定义的替换
 * Created by quanchengyun on 2018/10/11.
 */
public class EnvironmentRewritePostProcessor implements BeanFactoryPostProcessor,EnvironmentAware {

    private WrappedEnvironment wrappedEnvironment;

    @Override
    public void setEnvironment(Environment environment) {
        this.wrappedEnvironment = new WrappedEnvironment(environment);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        List<EnvironmentInterceptor> interceptors = new ArrayList<>();
        Map<String,EnvironmentInterceptor> beansOfType = beanFactory.getBeansOfType(EnvironmentInterceptor.class,true,true);
        for(EnvironmentInterceptor interceptor:beansOfType.values()){
            interceptors.add(interceptor);
        }
        //
        Collections.sort(interceptors, Comparator.comparingInt(EnvironmentInterceptor::getOrder));

    }



}
