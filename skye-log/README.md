skye-klog
=============
skye-log解决分布式环境日志分散问题,目的是将日志统一收集到一台服务器,方便开发人员查看log。  
通过扩展logback appender,将log写入kafka,然后由kafka consumer统一消费收集
## 依赖
- kafka
- logback

## 集成步骤
### 1.安装kafka
[kafka github](https://github.com/apache/kafka)
### 2.创建kafka topic,相关参数含义请参考kafka相关资料
```
.${kafka.path}/bin/kafka-topics.bat --create --zookeeper zookeeper1.dafy.com --partitions 4 --replication-factor 1  --topic klog
```
### 3.应用集成klog,引入maven依赖
```xml
<dependency>
	<groupId>com.dafy.skye</groupId>
	<artifactId>skye-klog-appender</artifactId>
	<version>${skye-klog.version}</version>
</dependency>
```
### 4.在需要收集日志的应用logback配置中添加logback appender
```xml
<appender name="klog" class="com.dafy.skye.klog.appender.KLogKafkaAppender">
	<!--kafka地址-->
	<kafkaAddress>kafka1.dafy.com:9092</kafkaAddress>
	<!--对应上面创建的topic-->
	<kafkaTopic>klog</kafkaTopic>
	<!-- 服务名称-->
	<serviceName>serviceA</serviceName>
	<!--是否包含堆栈信息-->
	<includeCallerData>true</includeCallerData>
</appender>
```
### 5.启动消费端,收集日志
```
.${klog.consumer.path}/bin/start.sh
```
### 6.klog-consumer.properties配置说明
```
#kafka 地址
bootstrap.servers=kafka1.dafy.com:9092
#zookeeper 地址
zookeeper.connect=zookeeper1.dafy.com:2181
#zookeeper 链接超时时间
zookeeper.connection.timeout.ms=6000
#kafka 分组ID
group.id=klog-group
#是否自动ack
enable.auto.commit=false
#key序列化类
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
#value序列化类
value.deserializer=com.dafy.klog.JavaDeserializer
#消费端拉取消息间隔
kafka.pull.interval=3000
#topic分区数
kafka.consumer.partition=4
#收集各个服务的日志保存路径
#根据上面示例的logback appender配置,如果应用部署在IP(129.168.1.2)的服务器上,
#2017年3月21日的日志最后的文件路径为:logs/serviceA/serviceA-192.168.1.2-2017-03-21.log
logback.fileName.pattern=logs/%sn/%sn-%addr-%d{yyyy-MM-dd}.log
#Log format encoder
#%sn对应klog-appender配置的serviceName,pid进程ID,其他参考logback配置
logback.log.pattern=[%sn-%pid@%addr] [%t] %-5level %d{yyyy-MM-dd HH:mm:ss.SSS} %logger{50} %L - %m%n
#kafka topic 对应klog-appender配置的kafkaTopic
kafka.topic.name=klog
#logback 配置文件地址
logback.config.path=logback-consumer.xml
#Redis配置 用来记录各个分区offset
redis.host=10.8.15.15
redis.port=16389
```
