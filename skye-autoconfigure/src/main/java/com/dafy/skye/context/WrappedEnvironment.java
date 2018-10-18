package com.dafy.skye.context;

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MissingRequiredPropertiesException;
import org.springframework.core.env.MutablePropertySources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by quanchengyun on 2018/10/11.
 */
public class WrappedEnvironment implements ConfigurableEnvironment {
    //delegate target
    private ConfigurableEnvironment environment;

    private List<EnvironmentInterceptor> interceptors=new ArrayList<>();

    public WrappedEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
        //
    }

    public void setInterceptors(List<EnvironmentInterceptor> interceptors){
        this.interceptors=interceptors;
    }


    @Override
    public String[] getActiveProfiles() {
        return this.environment.getActiveProfiles();
    }

    @Override
    public String[] getDefaultProfiles() {
        return this.environment.getDefaultProfiles();
    }

    @Override
    public boolean acceptsProfiles(String... profiles) {
        return this.environment.acceptsProfiles(profiles);
    }

    @Override
    public boolean containsProperty(String key) {
        return this.environment.containsProperty(key);
    }

    @Override
    public String getProperty(String key) {
        String v =  this.environment.getProperty(key);
        //
        for(EnvironmentInterceptor interceptor:interceptors){
            if(interceptor.matches(key,v)){
                v =interceptor.replace(v);
                break;
            }
        }
        return v;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String v =  this.environment.getProperty(key,defaultValue);
        for(EnvironmentInterceptor interceptor:interceptors){
            if(interceptor.matches(key,v)){
                v =interceptor.replace(v);
                break;
            }
        }
        return v;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        T v =  this.environment.getProperty(key,targetType);
        return v;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T v =  this.environment.getProperty(key,targetType,defaultValue);
        return v;
    }

    @Override
    @Deprecated
    public <T> Class<T> getPropertyAsClass(String key, Class<T> targetType) {
        return this.environment.getPropertyAsClass(key,targetType);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return this.environment.getRequiredProperty(key);
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return this.environment.getRequiredProperty(key,targetType);
    }

    @Override
    public String resolvePlaceholders(String text) {
        return this.environment.resolvePlaceholders(text);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return this.environment.resolveRequiredPlaceholders(text);
    }

    @Override
    public void setActiveProfiles(String... profiles) {
        this.environment.setActiveProfiles(profiles);
    }

    @Override
    public void addActiveProfile(String profile) {
        this.environment.addActiveProfile(profile);
    }

    @Override
    public void setDefaultProfiles(String... profiles) {
        this.environment.setDefaultProfiles(profiles);
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return this.environment.getPropertySources();
    }

    @Override
    public Map<String, Object> getSystemEnvironment() {
        return this.environment.getSystemEnvironment();
    }

    @Override
    public Map<String, Object> getSystemProperties() {
        return this.environment.getSystemProperties();
    }

    @Override
    public void merge(ConfigurableEnvironment parent) {
        this.environment.merge(parent);
    }

    @Override
    public ConfigurableConversionService getConversionService() {
        return this.environment.getConversionService();
    }

    @Override
    public void setConversionService(ConfigurableConversionService conversionService) {
        this.environment.setConversionService(conversionService);
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.environment.setPlaceholderPrefix(placeholderPrefix);
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.environment.setPlaceholderSuffix(placeholderSuffix);
    }

    @Override
    public void setValueSeparator(String valueSeparator) {
        this.environment.setValueSeparator(valueSeparator);
    }

    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        this.environment.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        this.environment.setRequiredProperties(requiredProperties);
    }

    @Override
    public void validateRequiredProperties() throws MissingRequiredPropertiesException {
        this.environment.validateRequiredProperties();
    }
}
