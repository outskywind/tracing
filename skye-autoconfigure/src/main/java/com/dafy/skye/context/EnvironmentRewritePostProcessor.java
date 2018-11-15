package com.dafy.skye.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.*;

import java.util.*;


/**
 * 支持对String 类型的属性值进行自定义的替换
 * 因为 PropertySourcesPlaceholderConfigurer 是 内置且 PriorityOrdered 最低优先级
 * 须定义在它优先级之前
 * Created by quanchengyun on 2018/10/11.
 */
public class EnvironmentRewritePostProcessor implements BeanFactoryPostProcessor,EnvironmentAware,ApplicationContextAware,PriorityOrdered {

    private ConfigurableEnvironment environment;
    private ConfigurableApplicationContext context;

    private List<PropertySourceInterceptor> interceptors=new ArrayList<>();

    private int order = Ordered.HIGHEST_PRECEDENCE;

    @Override
    public void setEnvironment(Environment environment) {
        if(environment instanceof ConfigurableEnvironment){
            this.environment = (ConfigurableEnvironment)environment;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if(this.environment==null){
            return;
        }
        //提前初始化会导致其他的Bean全部提前初始化，在对应的BeanPostProcessor注册之前
        Map<String,PropertySourceInterceptor>  interceptorBeans = beanFactory.getBeansOfType(PropertySourceInterceptor.class,true,false);
        for (PropertySourceInterceptor interceptor:interceptorBeans.values()){
            if(interceptor.isOk()){
                interceptors.add(interceptor);
            }
        }
        interceptors.sort(Comparator.comparingInt(PropertySourceInterceptor::getOrder));

        //String appName = environment.getProperty(JDBCPropertySourceInterceptor.APPNAME_KEY);
        //
        if(!interceptors.isEmpty()){
            //JDBCPropertySourceInterceptor interceptor = new JDBCPropertySourceInterceptor(appName);
            List<PropertySource<?>> newSource = new ArrayList<>();
            for(PropertySource<?> source: environment.getPropertySources()) {
                InterceptedPropertySource proxy = new InterceptedPropertySource(source.getName(),Collections.singleton(source));
                proxy.setIntercepters(interceptors);
                newSource.add(proxy);
            }
            //
            for(PropertySource<?> source : newSource){
                environment.getPropertySources().replace(source.getName(),source);
            }
        }
        //and then replace the  PropertySourcesPlaceholderConfigurer
        //fixed: modify environment before PropertySourcesPlaceholderConfigurer applied
        /*PropertySourcesPlaceholderConfigurer bean ;
        Map<String, PropertySourcesPlaceholderConfigurer> beans = beanFactory
                .getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false,
                        false);
        //
        if (beans.size() == 1) {
            bean =  beans.values().iterator().next();

            MutablePropertySources propertySources= (MutablePropertySources)bean.getAppliedPropertySources();
            propertySources.replace(PropertySourcesPlaceholderConfigurer.ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME,
                    new PropertySource<Environment>(
                            PropertySourcesPlaceholderConfigurer.ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME,
                            this.environment) {
                @Override
                public String getProperty(String key) {
                    return this.source.getProperty(key);
                }
            });
        }*/
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(this.context instanceof ConfigurableApplicationContext){
            this.context = (ConfigurableApplicationContext)applicationContext;
        }
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Holds the configuration {@link PropertySource}s as they are loaded can relocate
     * them once configuration classes have been processed.
     */
    static class InterceptedPropertySource
            extends EnumerablePropertySource<Collection<PropertySource<?>>> {

        private final Collection<PropertySource<?>> sources;

        private final String[] names;
        private List<PropertySourceInterceptor> intercepters;

        InterceptedPropertySource(String name , Collection<PropertySource<?>> sources) {
            super(name, sources);
            this.sources = sources;
            List<String> names = new ArrayList<String>();
            for (PropertySource<?> source : sources) {
                if (source instanceof EnumerablePropertySource) {
                    names.addAll(Arrays.asList(
                            ((EnumerablePropertySource<?>) source).getPropertyNames()));
                }
            }
            this.names = names.toArray(new String[names.size()]);
        }

        @Override
        public Object getProperty(String name) {
            for (PropertySource<?> propertySource : this.sources) {
                Object value = propertySource.getProperty(name);
                if (value != null) {
                    //intercept value
                    for(PropertySourceInterceptor intercepter:this.intercepters){
                        if(intercepter.matches(name,value)){
                            return intercepter.replace(value);
                        }
                    }
                    return value;
                }
            }
            return null;
        }

        @Override
        public String[] getPropertyNames() {
            return this.names;
        }

        public void setIntercepters(List<PropertySourceInterceptor> interceptors) {
            this.intercepters = interceptors;
        }
    }

}
