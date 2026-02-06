package com.data.rsync.monitor.service.impl;

import com.data.rsync.common.constants.DataRsyncConstants;
import com.data.rsync.common.model.Task;
import com.data.rsync.monitor.service.TaskMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务监控服务实现类
 * 监控同步任务的执行状态，并在出现异常时发送告警
 */
@Service
@Slf4j
public class TaskMonitorServiceImpl implements TaskMonitorService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private AlertServiceImpl alertService;

    /**
     * 任务状态缓存
     */
    private final Map<Long, TaskStatusInfo> taskStatusCache = new ConcurrentHashMap<>();

    /**
     * 任务状态信息
     */
    private static class TaskStatusInfo {
        private String status;
        private LocalDateTime lastUpdated;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String errorMessage;
        private int progress;

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
    }

    /**
     * 监控任务状态
     * @param taskId 任务ID
     * @param status 任务状态
     * @param progress 任务进度
     * @param errorMessage 错误信息
     */
    @Override
    public void monitorTaskStatus(Long taskId, String status, int progress, String errorMessage) {
        try {
            log.info("Monitoring task status: taskId={}, status={}, progress={}, errorMessage={}", 
                    taskId, status, progress, errorMessage);

            // 更新任务状态缓存
            TaskStatusInfo statusInfo = taskStatusCache.computeIfAbsent(taskId, k -> new TaskStatusInfo());
            statusInfo.setStatus(status);
            statusInfo.setProgress(progress);
            statusInfo.setErrorMessage(errorMessage);
            statusInfo.setLastUpdated(LocalDateTime.now());

            // 记录任务开始时间
            if ("RUNNING".equals(status) && statusInfo.getStartTime() == null) {
                statusInfo.setStartTime(LocalDateTime.now());
                log.info("Task started: taskId={}, startTime={}", taskId, statusInfo.getStartTime());
            }

            // 记录任务结束时间
            if (Arrays.asList("SUCCESS", "FAILED", "WARNING").contains(status) && statusInfo.getEndTime() == null) {
                statusInfo.setEndTime(LocalDateTime.now());
                long durationMs = Duration.between(statusInfo.getStartTime(), statusInfo.getEndTime()).toMillis();
                log.info("Task completed: taskId={}, status={}, duration={}ms", 
                        taskId, status, durationMs);
            }

            // 存储任务状态到Redis
            storeTaskStatusToRedis(taskId, status, progress, errorMessage);

            // 检查是否需要发送告警
            checkAndSendAlert(taskId, status, errorMessage);

        } catch (Exception e) {
            log.error("Failed to monitor task status: {}", e.getMessage(), e);
        }
    }

    /**
     * 存储任务状态到Redis
     * @param taskId 任务ID
     * @param status 任务状态
     * @param progress 任务进度
     * @param errorMessage 错误信息
     */
    private void storeTaskStatusToRedis(Long taskId, String status, int progress, String errorMessage) {
        try {
            String taskKey = DataRsyncConstants.RedisKey.TASK_PREFIX + taskId;
            redisTemplate.opsForHash().put(taskKey, "status", status);
            redisTemplate.opsForHash().put(taskKey, "progress", String.valueOf(progress));
            if (errorMessage != null) {
                redisTemplate.opsForHash().put(taskKey, "errorMessage", errorMessage);
            }
            redisTemplate.opsForHash().put(taskKey, "lastUpdated", LocalDateTime.now().toString());
            
            // 设置过期时间为7天
            redisTemplate.expire(taskKey, 7, java.util.concurrent.TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Failed to store task status to Redis: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查并发送告警
     * @param taskId 任务ID
     * @param status 任务状态
     * @param errorMessage 错误信息
     */
    private void checkAndSendAlert(Long taskId, String status, String errorMessage) {
        try {
            // 检查任务状态是否需要告警
            if ("FAILED".equals(status)) {
                // 发送任务失败告警
                alertService.sendTaskFailureAlert(taskId, errorMessage);
            } else if ("WARNING".equals(status)) {
                // 发送任务警告告警
                alertService.sendTaskWarningAlert(taskId, errorMessage);
            } else if ("RUNNING".equals(status)) {
                // 检查任务是否运行超时
                checkTaskTimeout(taskId);
            }
        } catch (Exception e) {
            log.error("Failed to check and send alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查任务是否运行超时
     * @param taskId 任务ID
     */
    private void checkTaskTimeout(Long taskId) {
        try {
            TaskStatusInfo statusInfo = taskStatusCache.get(taskId);
            if (statusInfo != null && statusInfo.getStartTime() != null) {
                long durationMs = Duration.between(statusInfo.getStartTime(), LocalDateTime.now()).toMillis();
                // 检查任务是否运行超过1小时
                if (durationMs > 3600000) { // 1 hour
                    String errorMessage = "Task running timeout: " + durationMs + "ms";
                    log.warn("Task running timeout: taskId={}, duration={}ms", taskId, durationMs);
                    alertService.sendTaskTimeoutAlert(taskId, errorMessage);
                }
            }
        } catch (Exception e) {
            log.error("Failed to check task timeout: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    @Override
    public Map<String, Object> getTaskStatus(Long taskId) {
        try {
            Map<String, Object> statusMap = new HashMap<>();
            
            // 从缓存获取任务状态
            TaskStatusInfo statusInfo = taskStatusCache.get(taskId);
            if (statusInfo != null) {
                statusMap.put("status", statusInfo.getStatus());
                statusMap.put("progress", statusInfo.getProgress());
                statusMap.put("lastUpdated", statusInfo.getLastUpdated());
                statusMap.put("startTime", statusInfo.getStartTime());
                statusMap.put("endTime", statusInfo.getEndTime());
                statusMap.put("errorMessage", statusInfo.getErrorMessage());
            } else {
                // 从Redis获取任务状态
                String taskKey = DataRsyncConstants.RedisKey.TASK_PREFIX + taskId;
                if (redisTemplate.hasKey(taskKey)) {
                    statusMap.put("status", redisTemplate.opsForHash().get(taskKey, "status"));
                    statusMap.put("progress", redisTemplate.opsForHash().get(taskKey, "progress"));
                    statusMap.put("lastUpdated", redisTemplate.opsForHash().get(taskKey, "lastUpdated"));
                    statusMap.put("errorMessage", redisTemplate.opsForHash().get(taskKey, "errorMessage"));
                } else {
                    statusMap.put("status", "UNKNOWN");
                    statusMap.put("progress", 0);
                }
            }
            
            return statusMap;
        } catch (Exception e) {
            log.error("Failed to get task status: {}", e.getMessage(), e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "ERROR");
            errorMap.put("errorMessage", e.getMessage());
            return errorMap;
        }
    }

    /**
     * 获取所有任务状态
     * @return 所有任务状态
     */
    @Override
    public List<Map<String, Object>> getAllTaskStatus() {
        try {
            List<Map<String, Object>> taskStatusList = new ArrayList<>();
            
            // 从缓存获取所有任务状态
            for (Map.Entry<Long, TaskStatusInfo> entry : taskStatusCache.entrySet()) {
                Long taskId = entry.getKey();
                TaskStatusInfo statusInfo = entry.getValue();
                
                Map<String, Object> statusMap = new HashMap<>();
                statusMap.put("taskId", taskId);
                statusMap.put("status", statusInfo.getStatus());
                statusMap.put("progress", statusInfo.getProgress());
                statusMap.put("lastUpdated", statusInfo.getLastUpdated());
                statusMap.put("startTime", statusInfo.getStartTime());
                statusMap.put("endTime", statusInfo.getEndTime());
                statusMap.put("errorMessage", statusInfo.getErrorMessage());
                
                taskStatusList.add(statusMap);
            }
            
            return taskStatusList;
        } catch (Exception e) {
            log.error("Failed to get all task status: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 清理任务状态缓存
     * @param taskId 任务ID
     */
    @Override
    public void cleanTaskStatusCache(Long taskId) {
        try {
            taskStatusCache.remove(taskId);
            log.info("Cleaned task status cache: taskId={}", taskId);
        } catch (Exception e) {
            log.error("Failed to clean task status cache: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理过期任务状态
     */
    @Override
    public void cleanExpiredTaskStatus() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7);
            List<Long> expiredTaskIds = new ArrayList<>();
            
            // 检查缓存中的任务状态是否过期
            for (Map.Entry<Long, TaskStatusInfo> entry : taskStatusCache.entrySet()) {
                Long taskId = entry.getKey();
                TaskStatusInfo statusInfo = entry.getValue();
                
                if (statusInfo.getLastUpdated().isBefore(cutoffTime)) {
                    expiredTaskIds.add(taskId);
                }
            }
            
            // 清理过期任务状态
            for (Long taskId : expiredTaskIds) {
                taskStatusCache.remove(taskId);
                log.info("Cleaned expired task status: taskId={}", taskId);
            }
            
        } catch (Exception e) {
            log.error("Failed to clean expired task status: {}", e.getMessage(), e);
        }
    }

    /**
     * 监控系统资源使用情况
     */
    @Override
    public void monitorSystemResources() {
        try {
            // 获取系统资源使用情况
            double cpuUsage = getCpuUsage();
            double memoryUsage = getMemoryUsage();
            double diskUsage = getDiskUsage();
            
            log.info("System resources: CPU={}%, Memory={}%, Disk={}%", 
                    cpuUsage, memoryUsage, diskUsage);

            // 检查系统资源使用是否过高
            if (cpuUsage > 80) {
                alertService.sendSystemResourceAlert("CPU", cpuUsage);
            }
            if (memoryUsage > 80) {
                alertService.sendSystemResourceAlert("Memory", memoryUsage);
            }
            if (diskUsage > 80) {
                alertService.sendSystemResourceAlert("Disk", diskUsage);
            }

            // 存储系统资源使用情况到Redis
            storeSystemResourceUsage(cpuUsage, memoryUsage, diskUsage);

        } catch (Exception e) {
            log.error("Failed to monitor system resources: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取CPU使用率
     * @return CPU使用率
     */
    private double getCpuUsage() {
        // 简化实现，实际项目中需要使用操作系统API获取
        return Math.random() * 50 + 20; // 模拟20-70%的CPU使用率
    }

    /**
     * 获取内存使用率
     * @return 内存使用率
     */
    private double getMemoryUsage() {
        // 简化实现，实际项目中需要使用操作系统API获取
        return Math.random() * 40 + 30; // 模拟30-70%的内存使用率
    }

    /**
     * 获取磁盘使用率
     * @return 磁盘使用率
     */
    private double getDiskUsage() {
        // 简化实现，实际项目中需要使用操作系统API获取
        return Math.random() * 30 + 40; // 模拟40-70%的磁盘使用率
    }

    /**
     * 存储系统资源使用情况到Redis
     * @param cpuUsage CPU使用率
     * @param memoryUsage 内存使用率
     * @param diskUsage 磁盘使用率
     */
    private void storeSystemResourceUsage(double cpuUsage, double memoryUsage, double diskUsage) {
        try {
            String resourceKey = DataRsyncConstants.RedisKey.MONITOR_PREFIX + "system_resources";
            redisTemplate.opsForHash().put(resourceKey, "cpuUsage", String.valueOf(cpuUsage));
            redisTemplate.opsForHash().put(resourceKey, "memoryUsage", String.valueOf(memoryUsage));
            redisTemplate.opsForHash().put(resourceKey, "diskUsage", String.valueOf(diskUsage));
            redisTemplate.opsForHash().put(resourceKey, "timestamp", LocalDateTime.now().toString());
            
            // 设置过期时间为1小时
            redisTemplate.expire(resourceKey, 1, java.util.concurrent.TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Failed to store system resource usage: {}", e.getMessage(), e);
        }
    }

    /**
     * 执行任务状态巡检
     */
    @Override
    public void performTaskStatusCheck() {
        try {
            log.info("Performing task status check");
            
            // 检查所有任务状态
            for (Map.Entry<Long, TaskStatusInfo> entry : taskStatusCache.entrySet()) {
                Long taskId = entry.getKey();
                TaskStatusInfo statusInfo = entry.getValue();
                
                // 检查任务是否长时间未更新
                if (Duration.between(statusInfo.getLastUpdated(), LocalDateTime.now()).toMinutes() > 30) {
                    log.warn("Task status not updated for a long time: taskId={}, lastUpdated={}", 
                            taskId, statusInfo.getLastUpdated());
                    alertService.sendTaskStatusAlert(taskId, "任务状态长时间未更新");
                }
                
                // 检查任务是否运行超时
                if ("RUNNING".equals(statusInfo.getStatus())) {
                    checkTaskTimeout(taskId);
                }
            }
            
            // 清理过期任务状态
            cleanExpiredTaskStatus();
            
            // 监控系统资源
            monitorSystemResources();
            
        } catch (Exception e) {
            log.error("Failed to perform task status check: {}", e.getMessage(), e);
        }
    }
}
