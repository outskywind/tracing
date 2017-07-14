package com.dafy.skye.zipkin.extend;

import com.dafy.setcd.spring.boot.autoconfigure.SetcdPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import zipkin.server.EnableZipkinServer;

/**
 * Created by Caedmon on 2017/6/15.
 */
@SpringBootApplication
@EnableAutoConfiguration
@EnableZipkinServer
//配置此注解会覆盖默认的配置文件读取
@SetcdPropertySource(
        etcdKeys={"/server-config/skye/application-${env}.yml"}
)
public class ZipkinExtendApplication {
    public static void main(String[] args) {
        SpringApplication application=new SpringApplication(ZipkinExtendApplication.class);
        application.run(args);

    }
}
