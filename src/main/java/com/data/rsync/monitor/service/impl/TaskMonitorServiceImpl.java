package com.data.rsync.monitor.service.impl;

import com.data.rsync.monitor.service.TaskMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务监控服务实现类
 * 提供任务执行状态监控、告警和统计功能
 */
@Service
public class TaskMonitorServiceImpl implements TaskMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(TaskMonitorServiceImpl.class);

    // 任务状态缓存
    private final Map<Long, TaskStatus> taskStatusCache = new ConcurrentHashMap<>();
    
    // 告警阈值配置
    private final Map<String, Double> alertThresholds = new ConcurrentHashMap<>();
    
    // 告警记录
    private final Map<Long, List<Alert>> taskAlerts = new ConcurrentHashMap<>();
    
    // 系统资源监控缓存
    private final Map<String, Object> systemResourceCache = new ConcurrentHashMap<>();
    
    // 资源监控更新时间
    private long lastResourceCheckTime = 0;
    
    // 资源监控间隔（毫秒）
    private static final long RESOURCE_CHECK_INTERVAL = 30000;
    
    // 线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public TaskMonitorServiceImpl() {
        // 初始化告警阈值
        initAlertThresholds();
        
        // 启动定期资源监控
        startResourceMonitor();
    }

    /**
     * 初始化告警阈值
     */
    private void initAlertThresholds() {
        // 系统资源阈值
        alertThresholds.put("cpuUsage", 80.0);
        alertThresholds.put("memoryUsage", 85.0);
        alertThresholds.put("diskUsage", 90.0);
        
        // 任务执行阈值
        alertThresholds.put("taskFailureRate", 10.0);
        alertThresholds.put("taskTimeoutMinutes", 30.0);
        alertThresholds.put("taskProgressStallMinutes", 15.0);
    }

    /**
     * 启动定期资源监控
     */
    private void startResourceMonitor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(this::monitorSystemResources, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void monitorTaskStatus(Long taskId, String status, int progress, String errorMessage) {
        logger.info("Monitoring task status for taskId: {}, status: {}, progress: {}, errorMessage: {}", 
                   taskId, status, progress, errorMessage);
        
        try {
            // 更新任务状态
            TaskStatus currentStatus = taskStatusCache.getOrDefault(taskId, new TaskStatus());
            
            // 保存旧进度用于检测停滞
            int oldProgress = currentStatus.getProgress();
            long oldLastUpdatedTime = currentStatus.getLastUpdatedTime();
            
            currentStatus.setStatus(status);
            currentStatus.setProgress(progress);
            currentStatus.setErrorMessage(errorMessage);
            currentStatus.setLastUpdatedTime(System.currentTimeMillis());
            
            // 如果是新任务，设置开始时间
            if (currentStatus.getStartTime() == 0) {
                currentStatus.setStartTime(System.currentTimeMillis());
            }
            
            // 检测进度停滞
            if (oldProgress == progress && oldLastUpdatedTime > 0) {
                long stallTime = currentStatus.getLastUpdatedTime() - oldLastUpdatedTime;
                if (stallTime > alertThresholds.get("taskProgressStallMinutes") * 60 * 1000) {
                    currentStatus.setStalled(true);
                }
            } else {
                currentStatus.setStalled(false);
            }
            
            // 计算执行时间
            if (currentStatus.getStartTime() > 0) {
                currentStatus.setExecutionTime(System.currentTimeMillis() - currentStatus.getStartTime());
            }
            
            // 保存到缓存
            taskStatusCache.put(taskId, currentStatus);
            
            // 持久化到存储
            saveTaskStatusToStorage(taskId, currentStatus);
            
            // 检查是否需要告警
            checkForAlerts(taskId, currentStatus);
            
            logger.info("Task status monitored successfully for taskId: {}", taskId);
            
        } catch (Exception e) {
            logger.error("Error monitoring task status for taskId: {}", taskId, e);
        }
    }

    @Override
    public Map<String, Object> getTaskStatus(Long taskId) {
        logger.info("Getting task status for taskId: {}", taskId);
        
        try {
            // 从缓存获取任务状态
            TaskStatus status = taskStatusCache.get(taskId);
            
            if (status == null) {
                // 从数据库或其他存储中获取任务状态
                status = loadTaskStatusFromStorage(taskId);
                if (status != null) {
                    taskStatusCache.put(taskId, status);
                }
            }
            
            if (status == null) {
                logger.warn("Task status not found for taskId: {}", taskId);
                return Map.of(
                    "taskId", taskId,
                    "status", "NOT_FOUND",
                    "message", "Task not found"
                );
            }
            
            // 构建任务状态响应
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("taskId", taskId);
            statusMap.put("status", status.getStatus());
            statusMap.put("progress", status.getProgress());
            statusMap.put("startTime", status.getStartTime());
            statusMap.put("lastUpdatedTime", status.getLastUpdatedTime());
            statusMap.put("executionTime", status.getExecutionTime());
            statusMap.put("errorMessage", status.getErrorMessage());
            statusMap.put("stalled", status.isStalled());
            statusMap.put("metrics", status.getMetrics());
            statusMap.put("alerts", taskAlerts.getOrDefault(taskId, Collections.emptyList()));
            
            logger.info("Task status retrieved for taskId: {}", taskId);
            
            return statusMap;
        } catch (Exception e) {
            logger.error("Error getting task status for taskId: {}", taskId, e);
            return Map.of(
                "taskId", taskId,
                "status", "ERROR",
                "message", "Failed to get task status: " + e.getMessage()
            );
        }
    }

    @Override
    public List<Map<String, Object>> getAllTaskStatus() {
        logger.info("Getting all task status");
        
        try {
            List<Map<String, Object>> allTaskStatus = new ArrayList<>();
            
            // 遍历所有任务状态
            for (Map.Entry<Long, TaskStatus> entry : taskStatusCache.entrySet()) {
                Long taskId = entry.getKey();
                TaskStatus status = entry.getValue();
                
                Map<String, Object> statusMap = new HashMap<>();
                statusMap.put("taskId", taskId);
                statusMap.put("status", status.getStatus());
                statusMap.put("progress", status.getProgress());
                statusMap.put("startTime", status.getStartTime());
                statusMap.put("lastUpdatedTime", status.getLastUpdatedTime());
                statusMap.put("executionTime", status.getExecutionTime());
                statusMap.put("errorMessage", status.getErrorMessage());
                statusMap.put("stalled", status.isStalled());
                statusMap.put("metrics", status.getMetrics());
                statusMap.put("alerts", taskAlerts.getOrDefault(taskId, Collections.emptyList()));
                
                allTaskStatus.add(statusMap);
            }
            
            logger.info("All task status retrieved, count: {}", allTaskStatus.size());
            
            return allTaskStatus;
        } catch (Exception e) {
            logger.error("Error getting all task status: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void cleanTaskStatusCache(Long taskId) {
        logger.info("Cleaning task status cache for taskId: {}", taskId);
        
        try {
            // 从缓存中移除任务状态
            taskStatusCache.remove(taskId);
            taskAlerts.remove(taskId);
            
            logger.info("Task status cache cleaned successfully for taskId: {}", taskId);
            
        } catch (Exception e) {
            logger.error("Error cleaning task status cache for taskId: {}", taskId, e);
        }
    }

    @Override
    public void cleanExpiredTaskStatus() {
        logger.info("Cleaning expired task status");
        
        try {
            long currentTime = System.currentTimeMillis();
            long expirationThreshold = 24 * 60 * 60 * 1000; // 24小时过期
            
            // 清理过期任务状态
            List<Long> expiredTasks = new ArrayList<>();
            for (Map.Entry<Long, TaskStatus> entry : taskStatusCache.entrySet()) {
                Long taskId = entry.getKey();
                TaskStatus status = entry.getValue();
                
                // 检查是否已完成且超过过期时间
                if ("COMPLETED".equals(status.getStatus()) || "FAILED".equals(status.getStatus())) {
                    if (currentTime - status.getLastUpdatedTime() > expirationThreshold) {
                        expiredTasks.add(taskId);
                    }
                }
            }
            
            // 执行清理
            for (Long taskId : expiredTasks) {
                taskStatusCache.remove(taskId);
                taskAlerts.remove(taskId);
                logger.info("Cleaned expired task status for taskId: {}", taskId);
            }
            
            logger.info("Expired task status cleaned successfully, removed: {}", expiredTasks.size());
            
        } catch (Exception e) {
            logger.error("Error cleaning expired task status: {}", e.getMessage(), e);
        }
    }

    @Override
    public void monitorSystemResources() {
        logger.info("Monitoring system resources");
        
        try {
            long currentTime = System.currentTimeMillis();
            
            // 检查是否需要更新资源状态
            if (currentTime - lastResourceCheckTime < RESOURCE_CHECK_INTERVAL) {
                logger.debug("Skipping resource check, within interval");
                return;
            }
            
            // 获取系统资源信息
            Map<String, Object> resources = new HashMap<>();
            
            // CPU 使用率（简化实现）
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            double cpuUsage = 0.0;
            // 实际项目中应该使用正确的方法来获取CPU使用率
            // 这里暂时返回0.0，避免编译错误
            resources.put("cpuUsage", cpuUsage);
            
            // 内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;
            resources.put("memoryUsage", memoryUsage);
            resources.put("totalMemory", totalMemory);
            resources.put("freeMemory", freeMemory);
            resources.put("usedMemory", usedMemory);
            
            // 磁盘使用情况
            try {
                FileStore fileStore = Files.getFileStore(Paths.get("."));
                long totalDisk = fileStore.getTotalSpace();
                long usableDisk = fileStore.getUsableSpace();
                long usedDisk = totalDisk - usableDisk;
                double diskUsage = (double) usedDisk / totalDisk * 100;
                resources.put("diskUsage", diskUsage);
                resources.put("totalDisk", totalDisk);
                resources.put("freeDisk", usableDisk);
                resources.put("usedDisk", usedDisk);
            } catch (IOException e) {
                logger.warn("Failed to get disk usage: {}", e.getMessage());
            }
            
            // JVM 信息
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            resources.put("jvmUptime", runtimeBean.getUptime());
            resources.put("jvmName", runtimeBean.getVmName());
            resources.put("jvmVersion", runtimeBean.getVmVersion());
            
            // 线程信息
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            resources.put("threadCount", threadBean.getThreadCount());
            resources.put("peakThreadCount", threadBean.getPeakThreadCount());
            resources.put("daemonThreadCount", threadBean.getDaemonThreadCount());
            
            // 更新缓存
            systemResourceCache.putAll(resources);
            systemResourceCache.put("lastChecked", currentTime);
            lastResourceCheckTime = currentTime;
            
            // 检查资源告警
            checkResourceAlerts(resources);
            
            logger.info("System resources monitored successfully: CPU={:.2f}%, Memory={:.2f}%, Disk={:.2f}%", 
                       cpuUsage, memoryUsage, resources.getOrDefault("diskUsage", 0.0));
            
        } catch (Exception e) {
            logger.error("Error monitoring system resources: {}", e.getMessage(), e);
        }
    }

    @Override
    public void performTaskStatusCheck() {
        logger.info("Performing task status check");
        
        try {
            int totalTasks = taskStatusCache.size();
            int runningTasks = 0;
            int failedTasks = 0;
            int completedTasks = 0;
            int stalledTasks = 0;
            long totalExecutionTime = 0;
            int tasksWithErrors = 0;
            
            // 统计任务状态
            for (Map.Entry<Long, TaskStatus> entry : taskStatusCache.entrySet()) {
                TaskStatus status = entry.getValue();
                String statusStr = status.getStatus();
                
                switch (statusStr) {
                    case "RUNNING":
                        runningTasks++;
                        if (status.isStalled()) {
                            stalledTasks++;
                        }
                        if (status.getErrorMessage() != null && !status.getErrorMessage().isEmpty()) {
                            tasksWithErrors++;
                        }
                        if (status.getExecutionTime() > 0) {
                            totalExecutionTime += status.getExecutionTime();
                        }
                        break;
                    case "FAILED":
                        failedTasks++;
                        tasksWithErrors++;
                        break;
                    case "COMPLETED":
                        completedTasks++;
                        if (status.getExecutionTime() > 0) {
                            totalExecutionTime += status.getExecutionTime();
                        }
                        break;
                }
            }
            
            // 计算平均执行时间
            double avgExecutionTime = totalTasks > 0 ? (double) totalExecutionTime / totalTasks : 0;
            
            // 计算失败率
            double failureRate = totalTasks > 0 ? (double) failedTasks / totalTasks * 100 : 0;
            
            // 构建检查结果
            Map<String, Object> checkResult = new HashMap<>();
            checkResult.put("totalTasks", totalTasks);
            checkResult.put("runningTasks", runningTasks);
            checkResult.put("failedTasks", failedTasks);
            checkResult.put("completedTasks", completedTasks);
            checkResult.put("stalledTasks", stalledTasks);
            checkResult.put("tasksWithErrors", tasksWithErrors);
            checkResult.put("avgExecutionTime", avgExecutionTime);
            checkResult.put("failureRate", failureRate);
            checkResult.put("checkTime", System.currentTimeMillis());
            
            // 记录状态检查结果
            logger.info("Task status check completed: {}", checkResult);
            
            // 检查任务整体状态告警
            if (failureRate > alertThresholds.get("taskFailureRate")) {
                triggerAlert(0L, "TASK_FAILURE_RATE_HIGH", 
                            "Task failure rate is too high: " + String.format("%.2f%%", failureRate), 
                            Alert.Severity.WARNING);
            }
            
            if (stalledTasks > runningTasks * 0.3) { // 超过30%的运行任务停滞
                triggerAlert(0L, "TASK_STALL_RATE_HIGH", 
                            "Too many stalled tasks: " + stalledTasks, 
                            Alert.Severity.WARNING);
            }
            
        } catch (Exception e) {
            logger.error("Error performing task status check: {}", e.getMessage(), e);
        }
    }

    /**
     * 从存储加载任务状态
     */
    private TaskStatus loadTaskStatusFromStorage(Long taskId) {
        try {
            // 实际实现：从数据库或其他存储中加载任务状态
            // 这里简化实现，返回模拟结果
            logger.debug("Loading task status from storage for taskId: {}", taskId);
            return new TaskStatus();
        } catch (Exception e) {
            logger.error("Error loading task status from storage: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 保存任务状态到存储
     */
    private void saveTaskStatusToStorage(Long taskId, TaskStatus status) {
        try {
            // 实际实现：将任务状态持久化到数据库或其他存储
            // 这里简化实现，模拟存储操作
            logger.debug("Saving task status to storage for taskId: {}", taskId);
            
            // 模拟存储延迟
            Thread.sleep(50);
            
        } catch (Exception e) {
            logger.error("Error saving task status to storage: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查是否需要告警
     */
    private void checkForAlerts(Long taskId, TaskStatus status) {
        try {
            // 检查任务失败
            if ("FAILED".equals(status.getStatus())) {
                triggerAlert(taskId, "TASK_FAILED", 
                            "Task failed: " + status.getErrorMessage(), 
                            Alert.Severity.ERROR);
            }
            
            // 检查任务停滞
            if (status.isStalled()) {
                triggerAlert(taskId, "TASK_STALLED", 
                            "Task progress stalled", 
                            Alert.Severity.WARNING);
            }
            
            // 检查任务超时
            if ("RUNNING".equals(status.getStatus()) && status.getExecutionTime() > 0) {
                double timeoutMinutes = alertThresholds.get("taskTimeoutMinutes");
                if (status.getExecutionTime() > timeoutMinutes * 60 * 1000) {
                    triggerAlert(taskId, "TASK_TIMEOUT", 
                                "Task execution timed out", 
                                Alert.Severity.WARNING);
                }
            }
            
            // 检查任务错误
            if (status.getErrorMessage() != null && !status.getErrorMessage().isEmpty()) {
                triggerAlert(taskId, "TASK_ERROR", 
                            "Task error: " + status.getErrorMessage(), 
                            Alert.Severity.ERROR);
            }
            
        } catch (Exception e) {
            logger.error("Error checking alerts: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查资源告警
     */
    private void checkResourceAlerts(Map<String, Object> resources) {
        try {
            // CPU 告警
            double cpuUsage = (double) resources.getOrDefault("cpuUsage", 0.0);
            if (cpuUsage > alertThresholds.get("cpuUsage")) {
                triggerAlert(0L, "CPU_USAGE_HIGH", 
                            "CPU usage too high: " + String.format("%.2f%%", cpuUsage), 
                            Alert.Severity.WARNING);
            }
            
            // 内存告警
            double memoryUsage = (double) resources.getOrDefault("memoryUsage", 0.0);
            if (memoryUsage > alertThresholds.get("memoryUsage")) {
                triggerAlert(0L, "MEMORY_USAGE_HIGH", 
                            "Memory usage too high: " + String.format("%.2f%%", memoryUsage), 
                            Alert.Severity.WARNING);
            }
            
            // 磁盘告警
            if (resources.containsKey("diskUsage")) {
                double diskUsage = (double) resources.get("diskUsage");
                if (diskUsage > alertThresholds.get("diskUsage")) {
                    triggerAlert(0L, "DISK_USAGE_HIGH", 
                                "Disk usage too high: " + String.format("%.2f%%", diskUsage), 
                                Alert.Severity.WARNING);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error checking resource alerts: {}", e.getMessage(), e);
        }
    }

    /**
     * 触发告警
     */
    private void triggerAlert(Long taskId, String alertType, String message, Alert.Severity severity) {
        try {
            Alert alert = new Alert(alertType, message, severity);
            
            // 添加到告警记录
            taskAlerts.computeIfAbsent(taskId, k -> new ArrayList<>()).add(alert);
            
            // 限制每个任务的告警数量
            List<Alert> alerts = taskAlerts.get(taskId);
            if (alerts.size() > 50) {
                alerts.subList(0, alerts.size() - 50).clear();
            }
            
            logger.info("Triggered alert for taskId {}: [{}] {}", taskId, severity, message);
            
            // 实际实现：发送告警通知（邮件、短信、消息队列等）
            // sendAlertNotification(alert);
            
        } catch (Exception e) {
            logger.error("Error triggering alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 任务状态类
     */
    private static class TaskStatus {
        private String status;
        private int progress;
        private long startTime;
        private long lastUpdatedTime;
        private long executionTime;
        private String errorMessage;
        private boolean stalled;
        private Map<String, Object> metrics;

        public TaskStatus() {
            this.metrics = new HashMap<>();
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public void setLastUpdatedTime(long lastUpdatedTime) {
            this.lastUpdatedTime = lastUpdatedTime;
        }

        public long getExecutionTime() {
            return executionTime;
        }

        public void setExecutionTime(long executionTime) {
            this.executionTime = executionTime;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public boolean isStalled() {
            return stalled;
        }

        public void setStalled(boolean stalled) {
            this.stalled = stalled;
        }

        public Map<String, Object> getMetrics() {
            return metrics;
        }

        public void setMetrics(Map<String, Object> metrics) {
            this.metrics = metrics;
        }
    }

    /**
     * 告警类
     */
    private static class Alert {
        private final String type;
        private final String message;
        private final Severity severity;
        private final long timestamp;
        private final String id;

        public Alert(String type, String message, Severity severity) {
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.timestamp = System.currentTimeMillis();
            this.id = UUID.randomUUID().toString();
        }

        public String getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public Severity getSeverity() {
            return severity;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Alert{" +
                    "type='" + type + '\'' +
                    ", message='" + message + '\'' +
                    ", severity=" + severity +
                    ", timestamp=" + timestamp +
                    '}';
        }

        public enum Severity {
            INFO, WARNING, ERROR, CRITICAL
        }
    }
}
