package com.dafy.skye.alertmanager;

import com.dafy.setcd.spring.SetcdPropertySource;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("com.dafy.base.alertmanager.mapper")
@SetcdPropertySource(locations = {
        "classpath:alertmanager.yml"
})
public class AlertManagerApplication {
    public static void main(String[] args) {
        log.info("alert manager init.....");
        SpringApplication.run(AlertManagerApplication.class, args);
    }
}
