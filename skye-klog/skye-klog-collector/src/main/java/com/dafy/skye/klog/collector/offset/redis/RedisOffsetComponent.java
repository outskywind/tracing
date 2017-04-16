package com.dafy.skye.klog.collector.offset.redis;

import com.dafy.skye.klog.collector.AbstractCollectorComponent;
import com.dafy.skye.klog.collector.CollectorConfig;
import com.google.common.base.Strings;
import com.dafy.skye.klog.collector.offset.OffsetComponent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Caedmon on 2017/3/2.
 */
public class RedisOffsetComponent extends AbstractCollectorComponent implements OffsetComponent {
    private JedisPool jedisPool;
    private static final String KLOG_CONSUMER_KEY="klog.consumer.com.dafy.skye.klog.collector.offset";
    private RedisConfig redisConfig;
    public RedisOffsetComponent(RedisConfig redisConfig ){
        this.redisConfig=redisConfig;
    }

    @Override
    public void start() {
        JedisPoolConfig poolConfig=redisConfig.getJedisPoolConfig();
        String host=redisConfig.getHost();
        int port=redisConfig.getPort();
        this.jedisPool=new JedisPool(poolConfig,host,port);
    }

    @Override
    public void stop() {
        this.jedisPool.close();
    }

    @Override
    public void setOffset(long offset) {
        String groupId= getCollectorConfig().getGroupId();
        String topicName= getCollectorConfig().getTopic();
        Jedis jedis=jedisPool.getResource();
        String field=topicName+"."+groupId+"."+getCollectorConfig().getPartition();
        try{
            jedis.hset(KLOG_CONSUMER_KEY,field,String.valueOf(offset));
        }finally {
            jedis.close();
        }
    }

    @Override
    public long getOffset() {
        String groupId= getCollectorConfig().getGroupId();
        String topicName= getCollectorConfig().getTopic();
        Jedis jedis=jedisPool.getResource();
        String field=topicName+"."+groupId+"."+ getCollectorConfig().getPartition();
        long offset=0L;
        try{
            String offsetValue=jedis.hget(KLOG_CONSUMER_KEY,field);
            if(!Strings.isNullOrEmpty(offsetValue)){
                offset=Long.valueOf(offsetValue);
            }
        }finally {
            jedis.close();
        }
        return offset;
    }
}
