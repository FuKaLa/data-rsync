package com.data.rsync.task.service;

import com.data.rsync.task.entity.TaskEntity;
import com.data.rsync.task.scheduler.TaskExecuteJob;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 任务调度服务，用于管理定时任务
 */
@Service
public class TaskSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerService.class);
    private static final String JOB_GROUP = "DATA_RSYNC_TASKS";
    private static final String TRIGGER_GROUP = "DATA_RSYNC_TRIGGERS";

    @Autowired
    private Scheduler scheduler;

    /**
     * 创建或更新定时任务
     * @param task 任务实体
     * @throws SchedulerException 调度器异常
     */
    public void scheduleTask(TaskEntity task) throws SchedulerException {
        String jobName = "task_" + task.getId();
        String triggerName = "trigger_" + task.getId();

        // 检查任务是否已存在
        JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP);
        if (scheduler.checkExists(jobKey)) {
            // 更新任务
            updateTaskSchedule(task, jobKey, triggerName);
        } else {
            // 创建任务
            createTaskSchedule(task, jobName, triggerName);
        }
    }

    /**
     * 创建定时任务
     * @param task 任务实体
     * @param jobName 任务名称
     * @param triggerName 触发器名称
     * @throws SchedulerException 调度器异常
     */
    private void createTaskSchedule(TaskEntity task, String jobName, String triggerName) throws SchedulerException {
        // 创建JobDetail
        JobDetail jobDetail = JobBuilder.newJob(TaskExecuteJob.class)
                .withIdentity(jobName, JOB_GROUP)
                .build();

        // 设置任务参数
        jobDetail.getJobDataMap().put("taskId", task.getId());

        // 创建触发器
        Trigger trigger = createTrigger(task, triggerName);

        // 调度任务
        scheduler.scheduleJob(jobDetail, trigger);
        logger.info("定时任务创建成功，任务ID：{}", task.getId());
    }

    /**
     * 更新定时任务
     * @param task 任务实体
     * @param jobKey 任务键
     * @param triggerName 触发器名称
     * @throws SchedulerException 调度器异常
     */
    private void updateTaskSchedule(TaskEntity task, JobKey jobKey, String triggerName) throws SchedulerException {
        // 删除旧触发器
        TriggerKey oldTriggerKey = TriggerKey.triggerKey(triggerName, TRIGGER_GROUP);
        if (scheduler.checkExists(oldTriggerKey)) {
            scheduler.unscheduleJob(oldTriggerKey);
        }

        // 创建新触发器
        Trigger newTrigger = createTrigger(task, triggerName);

        // 重新调度任务
        scheduler.scheduleJob(newTrigger);
        logger.info("定时任务更新成功，任务ID：{}", task.getId());
    }

    /**
     * 创建触发器
     * @param task 任务实体
     * @param triggerName 触发器名称
     * @return 触发器
     */
    private Trigger createTrigger(TaskEntity task, String triggerName) {
        // 根据任务的调度类型创建不同的触发器
        String scheduleType = task.getScheduleType();
        String scheduleExpression = task.getScheduleExpression();

        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, TRIGGER_GROUP)
                .startNow();

        if ("CRON".equals(scheduleType) && scheduleExpression != null) {
            // 使用Cron表达式
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(scheduleExpression));
        } else if ("FIXED_RATE".equals(scheduleType) && task.getSyncInterval() != null) {
            // 使用固定速率
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(task.getSyncInterval())
                    .repeatForever());
        } else {
            // 默认使用5分钟间隔
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(5)
                    .repeatForever());
        }

        return triggerBuilder.build();
    }

    /**
     * 暂停任务
     * @param taskId 任务ID
     * @throws SchedulerException 调度器异常
     */
    public void pauseTask(Long taskId) throws SchedulerException {
        String jobName = "task_" + taskId;
        JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP);

        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
            logger.info("定时任务暂停成功，任务ID：{}", taskId);
        } else {
            logger.warn("定时任务不存在，任务ID：{}", taskId);
        }
    }

    /**
     * 恢复任务
     * @param taskId 任务ID
     * @throws SchedulerException 调度器异常
     */
    public void resumeTask(Long taskId) throws SchedulerException {
        String jobName = "task_" + taskId;
        JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP);

        if (scheduler.checkExists(jobKey)) {
            scheduler.resumeJob(jobKey);
            logger.info("定时任务恢复成功，任务ID：{}", taskId);
        } else {
            logger.warn("定时任务不存在，任务ID：{}", taskId);
        }
    }

    /**
     * 删除任务
     * @param taskId 任务ID
     * @throws SchedulerException 调度器异常
     */
    public void deleteTask(Long taskId) throws SchedulerException {
        String jobName = "task_" + taskId;
        JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP);

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            logger.info("定时任务删除成功，任务ID：{}", taskId);
        } else {
            logger.warn("定时任务不存在，任务ID：{}", taskId);
        }
    }

    /**
     * 立即执行任务
     * @param taskId 任务ID
     * @throws SchedulerException 调度器异常
     */
    public void executeTaskImmediately(Long taskId) throws SchedulerException {
        String jobName = "task_" + taskId;
        JobKey jobKey = JobKey.jobKey(jobName, JOB_GROUP);

        if (scheduler.checkExists(jobKey)) {
            scheduler.triggerJob(jobKey);
            logger.info("定时任务立即执行成功，任务ID：{}", taskId);
        } else {
            logger.warn("定时任务不存在，任务ID：{}", taskId);
        }
    }

    /**
     * 清理所有任务
     * @throws SchedulerException 调度器异常
     */
    public void clearAllTasks() throws SchedulerException {
        // 删除所有任务
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP))) {
            scheduler.deleteJob(jobKey);
            logger.info("定时任务删除成功：{}", jobKey.getName());
        }
    }
}
