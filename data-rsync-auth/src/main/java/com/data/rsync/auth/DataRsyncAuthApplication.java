package com.data.rsync.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 认证服务应用类
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan(basePackages = "com.data.rsync.auth.mapper")
@ComponentScan(basePackages = {
    "com.data.rsync.auth",
    "com.data.rsync.common"
})
public class DataRsyncAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncAuthApplication.class, args);
    }

}
