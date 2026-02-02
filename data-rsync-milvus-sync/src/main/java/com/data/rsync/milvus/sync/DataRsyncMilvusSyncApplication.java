package com.data.rsync.milvus.sync;

import com.data.rsync.milvus.sync.service.impl.MilvusSyncServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class DataRsyncMilvusSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncMilvusSyncApplication.class, args);
    }

    @Bean
    public CommandLineRunner shutdownHook(ConfigurableApplicationContext context, MilvusSyncServiceImpl milvusSyncService) {
        return args -> {
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    milvusSyncService.shutdown();
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }));
        };
    }

}
