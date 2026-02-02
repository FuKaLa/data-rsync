package com.data.rsync.milvus.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DataRsyncMilvusSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncMilvusSyncApplication.class, args);
    }

}
