package com.data.rsync.datasource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DataRsyncDataSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncDataSourceApplication.class, args);
    }

}
