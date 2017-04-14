package com.dafy.skye.klog.collector;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import offset.redis.RedisOffsetComponent;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class CollectorController {
    private Properties properties;
    private LoggerContext context;
    private ExecutorService executorService;
    private volatile boolean started;
    public CollectorController(Properties properties) throws Exception{
        this.properties=properties;
        initContext();
    }
    public LoggerContext initContext() throws Exception{
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        context.reset();
        configurator.setContext(context);
        String configPath=properties.getProperty("logback.config.path");
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(configPath);
        configurator.doConfigure(in);
        this.context=context;
        return context;
    }
    public void start() {
        if(isStarted()){
            return;
        }
        String partitionValue=properties.getProperty("kafka.consumer.partition");
        int partition=1;
        if(partitionValue!=null){
            partition=Integer.parseInt(partitionValue);
        }
        executorService= Executors.newFixedThreadPool(partition);
        for(int i=0;i<partition;i++){
            KafkaConsumer<String,Object> kafkaConsumer=new KafkaConsumer<>(properties);
            KafkaCollector.Builder builder= KafkaCollector.Builder.create();
            KafkaCollector collector=builder
                    .kafkaConsumer(kafkaConsumer)
                    .offsetComponent(RedisOffsetComponent.create(properties,i))
                    .build();
            executorService.execute(collector);
        }
        this.started=true;

    }
    public boolean isStarted(){
        return started;
    }
}
