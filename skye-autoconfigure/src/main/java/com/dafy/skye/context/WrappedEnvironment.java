package com.dafy.skye.context;

import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by quanchengyun on 2018/10/11.
 */
public class WrappedEnvironment implements Environment {
    //delegate target
    private Environment environment;

    private List<EnvironmentInterceptor> interceptors=new ArrayList<>();

    public WrappedEnvironment(Environment environment) {
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
}
