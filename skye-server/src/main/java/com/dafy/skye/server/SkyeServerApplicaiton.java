package com.dafy.skye.server;

import com.dafy.setcd.spring.boot.autoconfigure.SetcdPropertySource;
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
//配置此注解会覆盖默认的配置文件读取
@SetcdPropertySource(
        etcdKeys={"/server-config/skye/application-${env}.yml"}
)
public class SkyeServerApplicaiton {
    public static void main(String[] args) {
        SpringApplication application=new SpringApplication(SkyeServerApplicaiton.class);
        application.run(args);
    }
}
