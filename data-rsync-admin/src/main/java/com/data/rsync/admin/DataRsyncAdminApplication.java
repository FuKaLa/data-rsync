package com.data.rsync.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 数据同步管理后台应用
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DataRsyncAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncAdminApplication.class, args);
    }

}
