package com.data.rsync.task.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * 任务调度器配置
 */
@Configuration
public class TaskSchedulerConfig {

    @Autowired
    private TaskJobFactory taskJobFactory;

    /**
     * 配置SchedulerFactoryBean
     * @return SchedulerFactoryBean
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        factoryBean.setJobFactory(taskJobFactory);
        factoryBean.setWaitForJobsToCompleteOnShutdown(true);
        factoryBean.setOverwriteExistingJobs(true);
        return factoryBean;
    }

    /**
     * 获取Scheduler实例
     * @return Scheduler
     * @throws SchedulerException 调度器异常
     */
    @Bean
    public Scheduler scheduler() throws SchedulerException {
        return schedulerFactoryBean().getScheduler();
    }
}
