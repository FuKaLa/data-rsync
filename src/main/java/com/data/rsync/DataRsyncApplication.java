package com.data.rsync;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.data.rsync")
@MapperScan(basePackages = {
    "com.data.rsync.auth.mapper",
    "com.data.rsync.task.mapper",
    "com.data.rsync.data.mapper",
    "com.data.rsync.common.dict.mapper"
})
public class DataRsyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataRsyncApplication.class, args);
    }
}
