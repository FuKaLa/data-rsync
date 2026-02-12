package com.data.rsync.data.config;

import com.data.rsync.common.config.CommonMyBatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MyBatis-Plus 配置类
 * 导入公共模块的 MyBatis-Plus 配置
 */
@Configuration
@Import(CommonMyBatisPlusConfig.class)
public class DataSourceMyBatisPlusConfig {

}

