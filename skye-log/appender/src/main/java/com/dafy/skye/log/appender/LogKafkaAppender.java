package com.dafy.skye.log.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import ch.qos.logback.core.util.Duration;
import com.ctrip.framework.apollo.Config;
import com.dafy.base.conf.DynamicConfConstants;
import com.dafy.base.conf.DynamicConfig;
import com.dafy.skye.log.core.logback.SkyeLogConverter;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by Caedmon on 2016/4/1.
 */
public class LogKafkaAppender extends UnsynchronizedAppenderBase<ILoggingEvent> implements Runnable{
    private Producer<String,String> kafkaProducer;
    private int queueSize=2048;
    private String kafkaAddress;
    private String kafkaTopic="skye-log";
    private BlockingQueue<SkyeLogEvent> queue= new LinkedBlockingDeque(this.queueSize);
    private Duration eventDelayLimit = new Duration(100L);
    private PreSerializationTransformer transformer=new LogSerializationTransformer();
    private String serviceName;
    private boolean includeCallerData=false;
    static {
        PatternLayout.defaultConverterMap.put("sn",SkyeLogConverter.ServiceNameConvert.class.getName());
        PatternLayout.defaultConverterMap.put("addr", SkyeLogConverter.AddressConvert.class.getName());
        PatternLayout.defaultConverterMap.put("pid", SkyeLogConverter.AddressConvert.class.getName());
    }
    private static Logger logger = LoggerFactory.getLogger(LogKafkaAppender.class);


    private boolean report=true;
    public void setReport(boolean report){
        this.report =report;
    }

    private Config dynamicConfig;

    public Config getDynamicConfig() {
        return dynamicConfig;
    }

    public void setDynamicConfig(Config dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
    }

    @Override
    public void start() {
        if(!this.isStarted()) {
            int errorCount = 0;
            if(kafkaAddress==null) {
                ++errorCount;
                this.addError("No kafka address specify");
            }

            if(this.queueSize < 0) {
                ++errorCount;
                this.addError("Queue size must be non-negative");
            }

            if(kafkaTopic==null||kafkaTopic.trim().equals("")){
                ++errorCount;
                this.addError("No kafka topic specify");
            }
            if(errorCount == 0) {
                this.queue=new LinkedBlockingDeque<>(this.queueSize);
                if(buildKafkaProducer()){
                    Thread t=new Thread(this);
                    t.start();
                    super.start();
                }
            }
        }
    }
    private boolean buildKafkaProducer(){
        if(kafkaProducer!=null){
            return true;
        }
        Properties props=new Properties();
        props.put("bootstrap.servers", kafkaAddress);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", LogEventSerializer.class.getName());
        kafkaProducer=new KafkaProducer(props);
        try{
            kafkaProducer.partitionsFor(this.kafkaTopic);
            logger.info("skye-log-appender init success. kafka.address={}",kafkaAddress);
        }catch (Throwable e){
            //spring-boot-logging 检查 status 是否有错误，这会导致失败,spring 启动时抛出异常，启动失败
            //但这不是我们想要的，如果kafka appender失败，那么append不记录
            logger.warn("skye-log-appender init failed. kafka.address={}",kafkaAddress);
            return false;
        }
        return true;
    }
    @Override
    public void run() {
        try{
            while(!Thread.currentThread().isInterrupted()) {
                this.dispatchEvent();
            }
        }catch (InterruptException e){

        }
    }

    public void dispatchEvent(){
        while(true) {
            try{
                SkyeLogEvent event = queue.take();
                ProducerRecord record = new ProducerRecord(this.kafkaTopic, event);
                kafkaProducer.send(record);
            }catch (Exception e){
                logger.warn("kafka Sending error,"+this.kafkaAddress+":"+e);
            }
        }
    }
    @Override
    public void stop() {
        super.stop();
    }
    @Override
    protected void append(ILoggingEvent event) {
        if(event.getLevel()== Level.DEBUG || event.getLevel()== Level.TRACE){
            return;
        }
        if(null!=dynamicConfig?!dynamicConfig.getBooleanProperty(DynamicConfConstants.skye_log_collect_key,report):!report){
            return;
        }
        //kafka 连接失败，就不会appender发送记录
        if(this.isStarted()) {
            try {
                if(includeCallerData){
                    event.getCallerData();
                }
                SkyeLogEvent logEvent=(SkyeLogEvent) this.getTransformer().transform(event);
                logEvent.setServiceName(this.serviceName);
                boolean e = this.queue.offer(logEvent, this.eventDelayLimit.getMilliseconds(), TimeUnit.MILLISECONDS);
                if(!e) {
                    logger.warn("Dropping event due to timeout limit of [" + this.eventDelayLimit + "] milliseconds being exceeded");
                }
            } catch (InterruptedException var3) {
                logger.warn("Interrupted while appending event to KakfaProducerAppender", var3);
            }
        }
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public String getKafkaAddress() {
        return kafkaAddress;
    }

    public void setKafkaAddress(String kafkaAddress) {
        this.kafkaAddress = kafkaAddress;
    }


    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public BlockingQueue<SkyeLogEvent> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<SkyeLogEvent> queue) {
        this.queue = queue;
    }

    public Duration getEventDelayLimit() {
        return eventDelayLimit;
    }

    public void setEventDelayLimit(Duration eventDelayLimit) {
        this.eventDelayLimit = eventDelayLimit;
    }

    public PreSerializationTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(PreSerializationTransformer transformer) {
        this.transformer = transformer;
    }

    public Producer<String, String> getKafkaProducer() {
        return kafkaProducer;
    }

    public void setKafkaProducer(Producer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }
}
