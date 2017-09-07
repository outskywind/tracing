package com.dafy.skye.server;

import com.dafy.setcd.spring.boot.autoconfigure.SetcdPropertySource;
import com.dafy.skye.log.server.autoconfig.EnableSkyeLogServer;
import com.dafy.skye.server.auto.config.EnableZipkinExtend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
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
@EnableZipkinExtend
public class SkyeServerApplicaiton {
    public static void main(String[] args) {
        SpringApplication application=new SpringApplication(SkyeServerApplicaiton.class);
        //运行启动写pid
        application.addListeners(new ApplicationPidFileWriter("SkyeServerApplicaiton.pid"));
        application.run(args);
        try {
            //Job定时任务是非daemon，但是他此时的线程还没启动的话，会导致直接退出，
            //因为第三方elastic ,kafka和 tomcat的线程都是daemon
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
