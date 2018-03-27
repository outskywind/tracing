package com.dafy.skye.conf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2018/3/16.
 */
public class ConfigCenterImportApplicationListener implements EnvironmentPostProcessor, Ordered {

    /**
     *必须在
     * @see org.springframework.context.annotation.ConfigurationClassPostProcessor 之前执行，才可以在Bean 定义的注解中使用配置中心
     * 否则只能在依赖注入的属性中使用
     */
    public  final static  int ORDER = ConfigFileApplicationListener.DEFAULT_ORDER-1;

    private final static String ENVIRONMENT_NAME = "configCenter";

    private final static Properties p = new Properties();

    private volatile AtomicBoolean initialed = new AtomicBoolean(false);


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // we get the @ConfigCenterPath configuration from the application class
        Class mainClazz = application.getMainApplicationClass();
        ConfigurationCenterPath path  = AnnotationUtils.findAnnotation(mainClazz,ConfigurationCenterPath.class);
        if(path!=null){
            String  pathUrl =  path.value();
            String host = path.host();
            String protocal = path.protocal();
            //generate the ResourceLoader
            ResourceLoader loader = new ConfigCenterResourceLoader();
            String resourceUrl = new StringBuffer().append(protocal).append("://").append(host).append("/").append(pathUrl).toString();
            if(initialed.compareAndSet(false,true)){
                //区分 yaml 与普通 properties 解析,因为yaml 格式兼容property 键值对
                //所以统一成yaml格式配置
                Resource resource = loader.getResource(resourceUrl);
                if(resource!=null){
                    try{
                        YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
                        String[] profiles = environment.getActiveProfiles();
                        for(String profile:profiles){
                            PropertySource propertySource = yamlPropertySourceLoader.load(ENVIRONMENT_NAME,loader.getResource(resourceUrl),profile);
                        }
                    }catch (IOException e){
                        throw new RuntimeException("load configCenter failed");
                    }
                }
                addPropertySources(environment,p);
            }
        }
    }

    protected void addPropertySources(ConfigurableEnvironment environment,
                                      Properties p) {
        environment.getPropertySources().addAfter(
                ENVIRONMENT_NAME,new PropertiesPropertySource(ENVIRONMENT_NAME,p));
    }


    @Override
    public int getOrder() {
        return ORDER;
    }
}
