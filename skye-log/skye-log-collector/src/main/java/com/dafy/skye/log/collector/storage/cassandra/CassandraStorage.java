package com.dafy.skye.log.collector.storage.cassandra;

import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.collector.storage.cassandra.domain.CassandraSkyeLogEntity;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
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

/**
 * Created by Caedmon on 2017/4/22.
 */
public class CassandraStorage implements StorageComponent {
    private Cluster cluster;
    private CassandraConfig configProperties;
    private static final Logger log= LoggerFactory.getLogger(CassandraStorage.class);
    public static final String UTF_8="UTF-8";
    private static final String INSERT_SQL="INSERT INTO skye.traces_log" +
            " (trace_id,ts_uuid,service_name,address,pid,thread,logger_name,level,mdc,message)" +
            " VALUES (?,?,?,?,?,?,?,?,?,?)";
    public CassandraStorage(CassandraConfig configProperties){
        this.configProperties=configProperties;
    }
    private Session session;
    private static PreparedStatement INSERT_ST=null;
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
        ensureExists();
    }
    private Session getSession(){
        if(session==null){
            synchronized (this){
                if(this.session==null){
                    this.session=this.cluster.connect();
                }
            }
        }
        return this.session;
    }
    private synchronized KeyspaceMetadata ensureExists(){
        KeyspaceMetadata result=this.cluster.getMetadata().getKeyspace(configProperties.getKeySpace());
        if(configProperties.isEnsureSchema()){
            String keySpace=configProperties.getKeySpace();
            String schemaResource=configProperties.getSchemaResource();
            if (result == null || result.getTable("traces_log") == null) {
                log.info("Installing schema {}", schemaResource);
                applyCqlFile(keySpace, getSession(), schemaResource);
                // refresh metadata since we've installed the schema
                result = this.cluster.getMetadata().getKeyspace(keySpace);
            }
        }
        return result;
    }
    private void applyCqlFile(String keySpace, Session session, String resource) {
        InputStream inputStream=CassandraSkyeLogEntity.class.getClassLoader().getResourceAsStream(resource);
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
    private BoundStatement buildInsertBST(CassandraSkyeLogEntity entity){
        if(INSERT_ST==null){
            INSERT_ST=getSession().prepare(INSERT_SQL);
        }
        BoundStatement statement=new BoundStatement(INSERT_ST);
        statement.bind(new Object[]{
                entity.getTraceId(),entity.getTsUuid(),entity.getServiceName(),
                entity.getAddress(),entity.getPid(),entity.getThread(),entity.getLoggerName(),
                entity.getLevel(),entity.getMdc(),entity.getMessage()
        });
        return statement;
    }
    @Override
    public void stop() {
        this.cluster.close();
    }

    @Override
    public void save(SkyeLogEvent event) {
        if(event==null){
            log.error("SkyeLogEvent is null");
            return;
        }
        Map<String,String> mdc=event.getMdc();
        if(mdc==null||mdc.isEmpty()||Strings.isNullOrEmpty(mdc.get("skyeTraceId"))){
            log.error("SkyeLogEvent traceId is empty:serviceName={},address={}",event.getServiceName(),event.getAddress());
            return;
        }
        Session session=getSession();
        CassandraSkyeLogEntity entity= CassandraSkyeLogEntity.build(event);
        BoundStatement statement= buildInsertBST(entity);
        session.executeAsync(statement);
    }
    @Override
    public void batchSave(Collection<SkyeLogEvent> events) {
        BatchStatement batch = new BatchStatement();
        Session session=getSession();
        for (SkyeLogEvent event:events) {
            CassandraSkyeLogEntity entity= CassandraSkyeLogEntity.build(event);
            Statement bs = buildInsertBST(entity);
            batch.add(bs);
        }
        session.executeAsync(batch);
        batch.clear();
    }
}
