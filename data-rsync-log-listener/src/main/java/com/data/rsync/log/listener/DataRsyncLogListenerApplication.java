package com.data.rsync.log.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DataRsyncLogListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncLogListenerApplication.class, args);
    }

}
