package com.data.rsync.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 数据同步管理后台应用
 */
@SpringBootApplication(
    scanBasePackages = {"com.data.rsync.admin", "com.data.rsync.auth"},
    exclude = {SqlInitializationAutoConfiguration.class}
)
@MapperScan(basePackages = {"com.data.rsync.admin.mapper", "com.data.rsync.auth.mapper"})
@EnableDiscoveryClient
@EnableFeignClients
public class DataRsyncAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncAdminApplication.class, args);
    }

}
