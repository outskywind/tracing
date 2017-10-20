package com.dafy.skye.druid;

import com.dafy.skye.zk.ZkSerivceDiscovery;
import com.metamx.common.lifecycle.Lifecycle;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Created by quanchengyun on 2017/9/15.
 */
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(DruidConfigurationProperties.class)
public class DruidConfig {

    @Bean(destroyMethod="close")
    //@ConditionalOnBean(ZkSerivceDiscovery.class) 不同文件先后加载顺序会产生影响
    public DruidClient initDruidClient(ZkSerivceDiscovery zkSerivceDiscovery){
        Lifecycle lifecycle = new Lifecycle();
        return DruidClientBuilder.create(Arrays.asList(zkSerivceDiscovery.getBrokers()),lifecycle);
    }

}
