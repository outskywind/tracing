package com.dafy.skye.server;

import com.dafy.skye.autoconfigure.log.collector.EnableSkyeLogCollector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by Caedmon on 2017/4/24.
 */
@SpringBootApplication
@EnableSkyeLogCollector
public class SkyeServer {
    public static void main(String[] args) {
        SpringApplication application=new SpringApplication(SkyeServer.class);
        application.run(args);
    }
}
