package com.dafy.skye.log.collector.storage.cassandra;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/22.
 */
public class CassandraConfig {
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

    public static class Builder{
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
        public Builder keySpace(String keySpace){
            this.keySpace=keySpace;
            return this;
        }
        public Builder contactPoints(List<String> contactPoints){
            this.contactPoints=contactPoints;
            return this;
        }
        public Builder username(String username){
            this.username=username;
            return this;
        }
        public Builder password(String password){
            this.password=password;
            return this;
        }
        public Builder maxConnections(int maxConnections){
            this.maxConnections=maxConnections;
            return this;
        }
        public Builder ensureSchema(boolean ensureSchema){
            this.ensureSchema=ensureSchema;
            return this;
        }
        public Builder useSsl(boolean useSsl){
            this.useSsl=useSsl;
            return this;
        }
        public Builder port(int port){
            this.port=port;
            return this;
        }
        public Builder indexFetchMultiplier(int indexFetchMultiplier){
            this.indexFetchMultiplier=indexFetchMultiplier;
            return this;
        }
        public Builder schemaResource(String schemaResource){
            this.schemaResource=schemaResource;
            return this;
        }
    }
}