package com.data.rsync.task.manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 任务管理服务应用类
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan(basePackages = "com.data.rsync.task.manager.repository")
public class DataRsyncTaskManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncTaskManagerApplication.class, args);
    }

}
