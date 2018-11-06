package com.dafy.skye.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.*;

import java.util.*;


/**
 * 支持对String 类型的属性值进行自定义的替换
 * Created by quanchengyun on 2018/10/11.
 */
public class EnvironmentRewritePostProcessor implements BeanFactoryPostProcessor,EnvironmentAware,ApplicationContextAware {

    private ConfigurableEnvironment environment;
    private ConfigurableApplicationContext context;

    private List<PropertySourceInterceptor> interceptors=new ArrayList<>();

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
        Map<String,PropertySourceInterceptor>  interceptorBeans = beanFactory.getBeansOfType(PropertySourceInterceptor.class,true,true);
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
        PropertySourcesPlaceholderConfigurer bean ;
        Map<String, PropertySourcesPlaceholderConfigurer> beans = beanFactory
                .getBeansOfType(PropertySourcesPlaceholderConfigurer.class, false,
                        false);
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
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(this.context instanceof ConfigurableApplicationContext){
            this.context = (ConfigurableApplicationContext)applicationContext;
        }
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
