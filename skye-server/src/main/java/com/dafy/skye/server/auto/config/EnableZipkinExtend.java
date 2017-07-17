package com.dafy.skye.server.auto.config;

import com.dafy.skye.zipkin.extend.config.ESClientConfig;
import com.dafy.skye.zipkin.extend.config.JobConfig;
import com.dafy.skye.zipkin.extend.config.ZipkinExtendESConfig;
import com.dafy.skye.zipkin.extend.controller.TraceController;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import zipkin.autoconfigure.storage.elasticsearch.http.ZipkinElasticsearchHttpStorageAutoConfiguration;

import java.lang.annotation.*;

/**
 * Created by quanchengyun on 2017/7/14.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({ZipkinExtendAutoConfig.class,TraceController.class,ESClientConfig.class,JobConfig.class})
public @interface EnableZipkinExtend {
}
