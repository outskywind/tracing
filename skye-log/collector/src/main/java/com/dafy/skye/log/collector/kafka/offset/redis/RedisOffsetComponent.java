package com.dafy.skye.log.collector.kafka.offset.redis;

import com.google.common.base.Strings;
import com.dafy.skye.log.collector.kafka.offset.OffsetComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by Caedmon on 2017/3/2.
 */
public class RedisOffsetComponent  implements OffsetComponent {
    private JedisPool jedisPool;
    private static String SKYE_LOG_OFFSET_KEY ="skye.log.collector.offset";
    private static final Logger log= LoggerFactory.getLogger(RedisOffsetComponent.class);
    public RedisOffsetComponent(JedisPool jedisPool){
        this.jedisPool=jedisPool;
    }

    @Override
    public void start() {
        log.info("RedisOffsetComponent started:offsetKey={}", SKYE_LOG_OFFSET_KEY);
    }

    @Override
    public void stop() {
        this.jedisPool.close();
    }

    @Override
    public void setOffset(String partitionKey, long offset) {
        Jedis jedis=jedisPool.getResource();
        try{
            jedis.hset(SKYE_LOG_OFFSET_KEY,partitionKey,String.valueOf(offset));
        }finally {
            jedis.close();
        }
    }

    @Override
    public long getOffset(String partitionKey) {
        long offset=0L;
        Jedis jedis=jedisPool.getResource();
        try{
            String offsetValue=jedis.hget(SKYE_LOG_OFFSET_KEY,partitionKey);
            if(!Strings.isNullOrEmpty(offsetValue)){
                offset=Long.valueOf(offsetValue);
            }
        }finally {
            jedis.close();
        }
        return offset;
    }
}
