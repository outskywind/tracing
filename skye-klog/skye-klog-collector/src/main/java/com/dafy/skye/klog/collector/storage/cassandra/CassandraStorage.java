package com.dafy.skye.klog.collector.storage.cassandra;

import com.alibaba.fastjson.JSON;
import com.dafy.skye.klog.collector.AbstractCollectorComponent;
import com.dafy.skye.klog.collector.storage.StorageComponent;
import com.dafy.skye.klog.core.logback.KLogEvent;
import com.datastax.driver.core.*;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Caedmon on 2017/4/22.
 */
public class CassandraStorage extends AbstractCollectorComponent implements StorageComponent {
    private Cluster cluster;
    private CassandraConfigProperties configProperties;
    private static final Logger log= LoggerFactory.getLogger(CassandraStorage.class);
    public static final String UTF_8="UTF-8";
    public CassandraStorage(CassandraConfigProperties configProperties){
        this.configProperties=configProperties;
    }

    @Override
    public void start() {
        String[] contactPoints=new String[configProperties.getContactPoints().size()];
        PoolingOptions poolingOptions = new PoolingOptions();
           // 每个连接的最大请求数
        poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, 32);
        // 表示和集群里的机器至少有2个连接 最多有4个连接
        poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, 2)
                .setMaxConnectionsPerHost(HostDistance.LOCAL, configProperties.getMaxConnections())
                .setCoreConnectionsPerHost(HostDistance.REMOTE, 2)
                .setMaxConnectionsPerHost(HostDistance.REMOTE, configProperties.getMaxConnections());
        configProperties.getContactPoints().toArray(contactPoints);
        this.cluster=Cluster.builder().addContactPoints(contactPoints)
                .withPoolingOptions(poolingOptions)
                .withCredentials(configProperties.getUsername(),configProperties.getPassword())
                .withCodecRegistry(new CodecRegistry().register()).build();
        if(configProperties.isEnsureSchema()){
            ensureExists();
        }
    }
    private Session getSession(){
        return this.cluster.connect();
    }
    private KeyspaceMetadata ensureExists(){
        String keySpace=configProperties.getKeySpace();
        String schemaResource=configProperties.getSchemaResource();
        KeyspaceMetadata result=this.cluster.getMetadata().getKeyspace(configProperties.getKeySpace());
        if (result == null || result.getTable("traces_log") == null) {
            log.info("Installing schema {}", schemaResource);
            applyCqlFile(keySpace, getSession(), schemaResource);
            // refresh metadata since we've installed the schema
            result = this.cluster.getMetadata().getKeyspace(keySpace);
        }
        return result;
    }
    void applyCqlFile(String keySpace, Session session, String resource) {
        InputStream inputStream=TraceLogSchema.class.getClassLoader().getResourceAsStream(resource);
        try (Reader reader = new InputStreamReader(inputStream, UTF_8)) {
            for (String cmd : CharStreams.toString(reader).split(";")) {
                cmd = cmd.trim().replace(" " + this.configProperties.getKeySpace(), " " + keySpace);
                if (!cmd.isEmpty()) {
                    session.execute(cmd);
                }
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    @Override
    public void stop() {
        this.cluster.close();
    }

    @Override
    public void save(KLogEvent event) {
        if(event==null){
            log.error("KLogEvent is null");
            return;
        }
        Map<String,String> mdc=event.getMdc();
        if(mdc==null||mdc.isEmpty()||Strings.isNullOrEmpty(mdc.get("skyeTraceId"))){
            log.warn("KLogEvent traceId is empty:serviceName={}",event.getServiceName());
            return;
        }
        Session session=getSession();
        TraceLogSchema schema=TraceLogSchema.build(event);

        String appender= JSON.toJSONString(schema);
        ResultSet resultSet=session.execute("INSERT INTO skye.traces_log JSON '"+appender+"'");
        System.out.println(resultSet);
    }

    @Override
    public void batchSave(Collection<KLogEvent> events) {

    }
}
