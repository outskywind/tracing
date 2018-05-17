package com.dafy.skye.zipkin.extend;

import com.dafy.setcd.spring.SetcdPropertySource;
import com.dafy.skye.zipkin.config.ZipkinAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;
import zipkin.server.EnableZipkinServer;


/**
 * Created by Caedmon on 2017/6/15.
 */
@SpringBootApplication
@Import(ZipkinAutoConfiguration.class)
@EnableZipkinServer
//配置此注解会覆盖默认的配置文件读取
@SetcdPropertySource(
        etcdKeys={"/server-config/skye/application-${env}.yml"},localOverride = true
)
public class SkyeMonitorApplication {
    public static void main(String[] args) {
        if(StringUtils.isEmpty(System.getProperty("env"))){
            System.setProperty("env","dev");
        }
        SpringApplication application=new SpringApplication(SkyeMonitorApplication.class);
        application.run(args);
        try {
            //Job定时任务是非deamon，但是他此时的线程还没启动的话，会导致直接退出，
            //因为第三方elastic 和 tomcat的线程都是deamon
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
