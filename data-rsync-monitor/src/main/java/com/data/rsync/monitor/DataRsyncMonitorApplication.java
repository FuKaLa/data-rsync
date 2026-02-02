package com.data.rsync.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 监控服务应用主类
 */
@SpringBootApplication
@EnableFeignClients
public class DataRsyncMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncMonitorApplication.class, args);
    }

}
