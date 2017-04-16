package com.dafy.skye.klog.collector.offset.redis;

import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * Created by Caedmon on 2017/3/16.
 */
public class RedisConfig {
    private String host;
    private int port;
    private JedisPoolConfig jedisPoolConfig;

    public RedisConfig(String host, int port, JedisPoolConfig jedisPoolConfig) {
        this.host = host;
        this.port = port;
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static class Builder{
        private String host;
        private int port;
        private JedisPoolConfig jedisPoolConfig;
        public static final String PROPERTIES_PREFIX="skye-klog-collector.redis";
        public Builder host(String host){
            this.host=host;
            return this;
        }
        public Builder port(int port){
            this.port=port;
            return this;
        }
        public Builder jedisPoolConfig(JedisPoolConfig jedisPoolConfig){
            this.jedisPoolConfig=jedisPoolConfig;
            return this;
        }
        public static Builder create(){
            return new Builder();
        }
        public RedisConfig build(){
            return new RedisConfig(this.host,this.port,this.jedisPoolConfig);
        }
        public RedisConfig build(Properties properties){
            this.host=properties.getProperty(PROPERTIES_PREFIX+".host","master.redis.sevend.com");
            this.port=Integer.parseInt(properties.getProperty(PROPERTIES_PREFIX+".port","16389"));
            JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
            jedisPoolConfig.setMinIdle(2);
            jedisPoolConfig.setMaxIdle(5);
            jedisPoolConfig.setTestOnReturn(true);
            jedisPoolConfig.setTestOnCreate(true);
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPoolConfig.setMaxTotal(5);
            this.jedisPoolConfig=jedisPoolConfig;
            return this.build();
        }
    }
}
