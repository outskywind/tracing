package offset.redis;

import com.google.common.base.Strings;
import offset.OffsetComponent;
import offset.OffsetConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * Created by Caedmon on 2017/3/2.
 */
public class RedisOffsetComponent implements OffsetComponent {
    private JedisPool jedisPool;
    private static final String KLOG_CONSUMER_KEY="klog.consumer.offset";
    private RedisConfig redisConfig;
    private OffsetConfig offsetConfig;
    public RedisOffsetComponent(RedisConfig redisConfig, OffsetConfig offsetConfig){
        this.offsetConfig=offsetConfig;
        initJedisPool(redisConfig);
    }

    @Override
    public void setOffsetConfig(OffsetConfig offsetConfig) {
        this.offsetConfig=offsetConfig;
    }

    /**
     * 初始化Redis 连接池
     * */
    private void initJedisPool(RedisConfig redisConfig){
        this.redisConfig=redisConfig;
        JedisPoolConfig poolConfig=redisConfig.jedisPoolConfig;
        String host=redisConfig.host;
        int port=redisConfig.port;
        int database=redisConfig.database;
        this.jedisPool=new JedisPool(poolConfig,host,port);
    }
    @Override
    public void setOffset(long offset) {
        String groupId=offsetConfig.groupId;
        String topicName=offsetConfig.topicName;
        Jedis jedis=jedisPool.getResource();
        String field=topicName+"."+groupId+"."+offsetConfig.partition;
        try{
            jedis.hset(KLOG_CONSUMER_KEY,field,String.valueOf(offset));
        }finally {
            jedis.close();
        }
    }

    @Override
    public long getOffset() {
        String groupId=offsetConfig.groupId;
        String topicName=offsetConfig.topicName;
        Jedis jedis=jedisPool.getResource();
        String field=topicName+"."+groupId+"."+offsetConfig.partition;
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
    public static RedisOffsetComponent create(Properties properties, int partition){
        RedisConfig redisConfig=new RedisConfig();
        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
        jedisPoolConfig.setMinIdle(2);
        jedisPoolConfig.setMaxIdle(5);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestOnCreate(true);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setMaxTotal(5);
        redisConfig.jedisPoolConfig=jedisPoolConfig;
        redisConfig.host=properties.getProperty("redis.host");
        redisConfig.port=Integer.parseInt(properties.getProperty("redis.port"));
        String topicName=properties.getProperty("kafka.topic.name");
        String groupId=properties.getProperty("group.id");
        OffsetConfig offsetConfig=new OffsetConfig();
        offsetConfig.groupId=groupId;
        offsetConfig.topicName=topicName;
        offsetConfig.partition=partition;
        RedisOffsetComponent recorder=new RedisOffsetComponent(redisConfig,offsetConfig);
        return recorder;
    }
}
