package com.data.rsync.task.scheduler;

import com.data.rsync.task.entity.TaskEntity;
import com.data.rsync.task.service.TaskService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 任务执行Job，用于实际执行定时任务
 */
public class TaskExecuteJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteJob.class);

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Long taskId = jobDataMap.getLongValue("taskId");

        logger.info("开始执行定时任务，任务ID：{}", taskId);

        try {
            // 获取任务信息
            TaskEntity taskEntity = taskService.getTaskById(taskId);
            if (taskEntity == null) {
                logger.warn("任务不存在，任务ID：{}", taskId);
                return;
            }

            // 检查任务状态
            if (taskEntity.getEnabled() == null || !taskEntity.getEnabled()) {
                logger.warn("任务已禁用，任务ID：{}", taskId);
                return;
            }

            // 触发任务执行
            taskService.triggerTask(taskId);

            logger.info("定时任务执行成功，任务ID：{}", taskId);
        } catch (Exception e) {
            logger.error("定时任务执行失败，任务ID：{}", taskId, e);
            // 更新任务状态为失败
            try {
                taskService.updateTaskStatus(taskId, "FAILED", "定时任务执行失败：" + e.getMessage());
            } catch (Exception ex) {
                logger.error("更新任务状态失败", ex);
            }
        }
    }
}
