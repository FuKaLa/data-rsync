package com.data.rsync.common.init;

import com.data.rsync.auth.model.User;
import com.data.rsync.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * 应用初始化类
 * 在应用启动时执行初始化操作
 */
@Component
public class DataRsyncInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataRsyncInitializer.class);

    @Autowired
    private AuthService authService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("开始执行应用初始化操作");

        // 初始化默认用户
        initDefaultUser();

        logger.info("应用初始化操作完成");
    }

    /**
     * 初始化默认用户
     */
    private void initDefaultUser() {
        try {
            // 检查是否已存在admin用户
            User adminUser = authService.getUserByUsername("admin");
            if (adminUser != null) {
                // 删除已存在的admin用户，然后重新创建
                logger.info("默认admin用户已存在，删除后重新创建");
                authService.deleteUser(adminUser.getId());
                logger.info("已删除默认admin用户");
            }

            // 创建默认admin用户
            User user = new User();
            user.setUsername("admin");
            user.setPassword("admin123"); // 密码会在创建时自动加密
            user.setName("系统管理员");
            user.setEmail("admin@example.com");
            user.setPhone("13800138000");
            user.setStatus("ENABLE");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setRoles(new ArrayList<>());

            authService.createUser(user);
            logger.info("默认admin用户创建成功，密码：admin123");
        } catch (Exception e) {
            logger.error("初始化默认用户失败", e);
        }
    }
}
