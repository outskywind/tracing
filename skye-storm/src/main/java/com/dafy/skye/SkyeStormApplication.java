package com.dafy.skye;

import com.dafy.setcd.spring.boot.autoconfigure.SetcdPropertySource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;

/**
 * Created by quanchengyun on 2017/8/25.
 */
@SpringBootApplication
//配置此注解会覆盖默认的配置文件读取
@SetcdPropertySource(
        etcdKeys={"/server-config/skye/storm-${env}.yml"}
)
public class SkyeStormApplication {


    public static void main(String[] args){
        SpringApplication application=new SpringApplication(SkyeStormApplication.class);
        //运行启动写pid
        application.addListeners(new ApplicationPidFileWriter("SkyeStormApplication.pid"));
        application.run(args);
        application.setRegisterShutdownHook(true);
        try {
            //Job定时任务是非daemon，但是他此时的线程还没启动的话，会导致直接退出，
            //因为第三方elastic ,kafka和 tomcat的线程都是daemon
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
