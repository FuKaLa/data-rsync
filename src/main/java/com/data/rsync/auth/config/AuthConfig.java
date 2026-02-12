package com.data.rsync.auth.config;

import com.data.rsync.common.config.CommonMyBatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 权限管理配置类
 */
@Configuration
@Import(CommonMyBatisPlusConfig.class)
public class AuthConfig {

}


