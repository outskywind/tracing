package com.dafy.skye.alertmanager;

import com.alibaba.druid.pool.DruidDataSource;
import com.dafy.setcd.spring.SetcdPropertySource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@Slf4j
@SpringBootApplication
@MapperScan("com.dafy.skye.alertmanager.mapper")
@SetcdPropertySource(etcdKeys ={
        "/server-config/sevend/share/datasource-${env}.properties"
}, locations = {
        "classpath:alertmanager.yml"
})
public class AlertManagerApplication {
    public static void main(String[] args) {
        log.info("skye alert manager init.....");
        //如果当前是本地windows,自动给定环境变量
        if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC) {
            //如果当前是本地windows环境,自动给一个本地的环境变量吧,懒得每个地方启动时去设置
            System.setProperty("env", "dev");//-Denv=product
        }

        SpringApplication.run(AlertManagerApplication.class, args);
    }

    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        return dataSource;
    }
}
