package com.dafy.skye.server;

import com.dafy.skye.log.server.autoconfig.EnableSkyeLogServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zipkin.server.EnableZipkinServer;

/**
 * Created by Caedmon on 2017/6/26.
 */
@SpringBootApplication
@EnableZipkinServer
@EnableSkyeLogServer
public class SkyeServerApplicaiton {
    public static void main(String[] args) {
        SpringApplication application=new SpringApplication(SkyeServerApplicaiton.class);
        application.run(args);
    }
}
