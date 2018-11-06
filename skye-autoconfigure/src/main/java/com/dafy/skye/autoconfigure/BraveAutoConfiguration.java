package com.dafy.skye.autoconfigure;

import brave.Tracing;
import com.ctrip.framework.apollo.Config;
import com.dafy.skye.zipkin.ReporterDelegate;
import com.dafy.skye.conf.SkyeDynamicConf;
import com.dafy.skye.zipkin.KafkaSender10;
import com.dafy.skye.zipkin.TracingProxy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import zipkin2.CheckResult;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;

/**
 * Created by Caedmon on 2017/6/26.
 */
@Configuration
public class BraveAutoConfiguration {

    static Logger log  = LoggerFactory.getLogger(BraveAutoConfiguration.class);

    @Bean
    @ConfigurationProperties("skye")
    public BraveConfigProperties braveConfigProperties(){
        return new BraveConfigProperties();
    }

    //@Autowired(required = false)
    //Config dynamicConfig;

    @Value("${appName}")
    String appName;

    @Value("${skye.dynamicConf.enable:false}")
    boolean enableDynamicConf =false;

    @Bean
    @ConditionalOnMissingBean
    public Config skyeDynamicConf(BraveConfigProperties configProperties){
        if(StringUtils.isNotBlank(appName)){
            configProperties.setServiceName(appName);
        }
        if(StringUtils.isBlank(configProperties.getServiceName())){
            log.warn("appName is empty");
            return null;
        }
        return enableDynamicConf?SkyeDynamicConf.getInstance(configProperties.getServiceName()):null;
    }

    @Bean("tracing")
    @Lazy
    //因为 @ConfigurationPorperties 使用Registrar的机制，这里会无法生效
    //因为spring 先处理加载完 BeanMethod 的信息，再加载Registrar的Bean信息
    //@ConditionalOnBean(BraveConfigProperties.class)
    public Tracing tracing(BraveConfigProperties configProperties,Config dynamicConfig){
        //兼容1.7.1-
        if(StringUtils.isNotBlank(appName)){
            configProperties.setServiceName(appName);
        }
        if(StringUtils.isBlank(configProperties.getServiceName())){
            log.warn("appName is empty");
            return null;
        }

        Reporter<zipkin2.Span> reporter;
        if(StringUtils.isBlank(configProperties.getKafkaServers())){
            log.warn("Brave kafkaServers empty");
            reporter= Reporter.NOOP;
        }else{
            reporter = getReporter(configProperties,dynamicConfig);
        }
        //
        //builder=new Brave.Builder(configProperties.getServiceName().trim());
        //
        //if(configProperties.getSamplerRate()!=null){
            //builder.traceSampler(Sampler.create(configProperties.getSamplerRate()));
        //}else{
            //默认监控统计全部数据
            //builder.traceSampler(Sampler.ALWAYS_SAMPLE);
        //}
        /*Tracing tracing = Tracing.newBuilder()
                .localServiceName(configProperties.getServiceName())
                .spanReporter(reporter).build();*/
        Tracing tracing =  TracingProxy.getProxy(configProperties.getServiceName(),reporter);
        return tracing;
    }

    protected Reporter<zipkin2.Span> getReporter(BraveConfigProperties configProperties,
                                              Config dynamicConfig){
        ReporterDelegate<zipkin2.Span> reporterDelegate=null;
        Sender kafkaSender= KafkaSender10.create(configProperties.getKafkaServers());
        CheckResult checkResult=kafkaSender.check();
        if(checkResult.ok()) {
            Reporter<zipkin2.Span> reporter= AsyncReporter.builder(kafkaSender).build();
            reporterDelegate = new ReporterDelegate(reporter,configProperties.isReport());
            //if(dynamicConfig==null){
            //    dynamicConfig = SkyeDynamicConf.getInstance(configProperties.getServiceName());
            //}
            reporterDelegate.setDynamicConfig(dynamicConfig);
        }else {
            if (!checkResult.ok()) {
                log.warn("Kafka Sender check error: " + checkResult.error());
            }
        }
        return reporterDelegate==null? Reporter.NOOP:reporterDelegate;
    }

    // attention ： 此BeanFactoryPostProcessor 定义会导致 @Configuration 注解的类 提前实例化
    // @Value 比较坑的在于 是由第三方的 AutowiredAnnotationBeanPostProcessor 处理的
    /*@Bean
    public EnvironmentRewritePostProcessor initEnvironmentRewritePostProcessor(){
        return new EnvironmentRewritePostProcessor();
    }*/

    /*@Bean("jdbcRewrite")
    @ConditionalOnClass(name="com.mysql.jdbc.StatementInterceptorV2")
    public PropertySourceInterceptor interceptor(@Value("${appName}")String  appName){
        JDBCPropertySourceInterceptor interceptor = new JDBCPropertySourceInterceptor();
        interceptor.setAppName(appName);
        return interceptor;
    }*/


}
