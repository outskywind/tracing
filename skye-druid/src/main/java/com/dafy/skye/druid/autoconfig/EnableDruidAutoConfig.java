package com.dafy.skye.druid.autoconfig;

import com.dafy.skye.druid.DruidConfig;
import com.dafy.skye.zk.ZkConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by quanchengyun on 2017/9/15.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DruidConfig.class,ZkConfig.class})
public @interface EnableDruidAutoConfig {

}
