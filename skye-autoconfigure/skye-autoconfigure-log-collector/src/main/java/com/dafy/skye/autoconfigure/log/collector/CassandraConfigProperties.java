package com.dafy.skye.autoconfigure.log.collector;

import com.dafy.skye.log.collector.storage.cassandra.CassandraConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/25.
 */
@ConfigurationProperties("skye.log.collector.storage.casandra")
public class CassandraConfigProperties {
    private String keySpace="skye";
    private List<String> contactPoints= Arrays.asList("10.8.15.79");
    private String username="cassandra";
    private String password="cassandra";
    private int maxConnections=8;
    private boolean ensureSchema = true;
    private boolean useSsl=false;
    private int port=9042;
    private int indexFetchMultiplier=3;
    private String schemaResource="cassandra-schema.sql";
    public List<String> getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(List<String> contactPoints) {
        this.contactPoints = contactPoints;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKeySpace() {
        return keySpace;
    }

    public void setKeySpace(String keySpace) {
        this.keySpace = keySpace;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public boolean isEnsureSchema() {
        return ensureSchema;
    }

    public void setEnsureSchema(boolean ensureSchema) {
        this.ensureSchema = ensureSchema;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public int getIndexFetchMultiplier() {
        return indexFetchMultiplier;
    }

    public void setIndexFetchMultiplier(int indexFetchMultiplier) {
        this.indexFetchMultiplier = indexFetchMultiplier;
    }

    public String getSchemaResource() {
        return schemaResource;
    }

    public void setSchemaResource(String schemaResource) {
        this.schemaResource = schemaResource;
    }
    public CassandraConfig.Builder builder(){
        CassandraConfig.Builder builder=new CassandraConfig.Builder();
        builder.contactPoints(this.contactPoints)
                .ensureSchema(this.ensureSchema)
                .indexFetchMultiplier(this.indexFetchMultiplier)
                .keySpace(this.keySpace)
                .maxConnections(this.maxConnections)
                .port(this.port)
                .username(this.username)
                .password(this.password)
                .schemaResource(this.schemaResource)
                .useSsl(this.useSsl);
        return builder;
    }
    public CassandraConfig buildCassandraConfig(){
        CassandraConfig cassandraConfig=new CassandraConfig();
        cassandraConfig.setContactPoints(this.contactPoints);
        cassandraConfig.setEnsureSchema(this.ensureSchema);
        cassandraConfig.setIndexFetchMultiplier(this.indexFetchMultiplier);
        cassandraConfig.setKeySpace(this.keySpace);
        cassandraConfig.setMaxConnections(this.maxConnections);
        cassandraConfig.setUsername(this.username);
        cassandraConfig.setPassword(this.password);
        cassandraConfig.setPort(this.port);
        cassandraConfig.setSchemaResource(this.schemaResource);
        cassandraConfig.setUseSsl(this.useSsl);
        return cassandraConfig;
    }
}
