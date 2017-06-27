package com.dafy.skye.zipkin.extend;

import com.dafy.skye.log.server.autoconfig.EnableSkyeLogServer;
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
@EnableSkyeLogServer
public class ZipkinExtendApplication {
    public static void main(String[] args) {
        SpringApplication application=new SpringApplication(ZipkinExtendApplication.class);
        application.run(args);

    }
}
