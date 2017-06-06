package com.dafy.skye.server;

import com.dafy.skye.autoconfigure.log.collector.EnableSkyeLogCollector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zipkin.server.EnableZipkinServer;

/**
 * Created by Caedmon on 2017/4/24.
 */
@SpringBootApplication
@EnableSkyeLogCollector
@EnableZipkinServer
public class SkyeServer {

    public static void main(String[] args) {
        SpringApplication application=new SpringApplicationBuilder(SkyeServer.class)
                .properties("spring.config.name=skye-log-server,skye-zipkin-server").build();
        application.setWebEnvironment(true);
        application.run(args);

    }
}
