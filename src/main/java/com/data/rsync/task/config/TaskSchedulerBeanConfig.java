package com.data.rsync.task.config;

import com.data.rsync.task.scheduler.TaskJobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 任务调度器Bean配置
 */
@Configuration
public class TaskSchedulerBeanConfig {

    /**
     * 配置TaskJobFactory
     * @return TaskJobFactory
     */
    @Bean
    public TaskJobFactory taskJobFactory() {
        return new TaskJobFactory();
    }
}
