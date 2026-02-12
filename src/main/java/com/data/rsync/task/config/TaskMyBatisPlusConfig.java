package com.data.rsync.task.config;

import com.data.rsync.common.config.CommonMyBatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 任务模块 MyBatis-Plus 配置类
 */
@Configuration
@Import(CommonMyBatisPlusConfig.class)
public class TaskMyBatisPlusConfig {

}
