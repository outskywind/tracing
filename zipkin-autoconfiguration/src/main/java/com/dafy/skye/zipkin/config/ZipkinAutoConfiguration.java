package com.dafy.skye.zipkin.config;

import com.dafy.skye.zipkin.config.druid.ZipkinDruidStorageAutoConfiguration;
import com.dafy.skye.zipkin.config.elasticsearch.ElasticSearchStorageAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by quanchengyun on 2018/5/15.
 */
@Configuration
@Import({ElasticSearchStorageAutoConfiguration.class,ZipkinDruidStorageAutoConfiguration.class})
public class ZipkinAutoConfiguration {


}
