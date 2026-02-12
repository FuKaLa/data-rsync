package com.data.rsync.task.service.impl;

import com.data.rsync.task.service.TaskSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务调度服务实现类
 * 提供任务调度、优先级管理、并发控制等功能
 */
@Service
public class TaskSchedulerServiceImpl extends TaskSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(TaskSchedulerServiceImpl.class);

    // 任务调度线程池
    private final ExecutorService taskExecutor;
    
    // 任务优先级队列
    private final PriorityBlockingQueue<ScheduledTask> taskQueue;
    
    // 任务调度器
    private final ScheduledExecutorService scheduler;
    
    // 活跃任务计数
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    
    // 最大并发任务数
    private static final int MAX_CONCURRENT_TASKS = 5;

    public TaskSchedulerServiceImpl() {
        // 初始化任务执行线程池
        this.taskExecutor = new ThreadPoolExecutor(
            2, // 核心线程数
            MAX_CONCURRENT_TASKS, // 最大线程数
            60L, TimeUnit.SECONDS, // 线程存活时间
            new LinkedBlockingQueue<>(100), // 任务队列
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "task-executor-" + threadNumber.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
        
        // 初始化任务优先级队列
        this.taskQueue = new PriorityBlockingQueue<>();
        
        // 初始化调度器
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // 启动任务调度线程
        startTaskScheduler();
    }

    public boolean scheduleTask(String taskId, String taskType, Map<String, Object> taskParams, int priority) {
        logger.info("Scheduling task: taskId={}, taskType={}, priority={}", taskId, taskType, priority);
        
        try {
            // 创建调度任务
            ScheduledTask scheduledTask = new ScheduledTask(
                taskId, taskType, taskParams, priority, System.currentTimeMillis()
            );
            
            // 将任务加入优先级队列
            taskQueue.offer(scheduledTask);
            
            logger.info("Task scheduled successfully: taskId={}", taskId);
            
            return true;
        } catch (Exception e) {
            logger.error("Error scheduling task: taskId={}", taskId, e);
            return false;
        }
    }

    public boolean cancelTask(String taskId) {
        logger.info("Cancelling task: taskId={}", taskId);
        
        try {
            // 从队列中移除任务
            boolean removed = taskQueue.removeIf(task -> task.getTaskId().equals(taskId));
            
            if (removed) {
                logger.info("Task cancelled successfully: taskId={}", taskId);
            } else {
                logger.warn("Task not found in queue: taskId={}", taskId);
            }
            
            return removed;
        } catch (Exception e) {
            logger.error("Error cancelling task: taskId={}", taskId, e);
            return false;
        }
    }

    public int getQueueSize() {
        return taskQueue.size();
    }

    public int getActiveTaskCount() {
        return activeTaskCount.get();
    }

    public Map<String, Object> getSchedulerStatus() {
        return Map.of(
            "queueSize", getQueueSize(),
            "activeTaskCount", getActiveTaskCount(),
            "maxConcurrentTasks", MAX_CONCURRENT_TASKS,
            "threadPoolStatus", getThreadPoolStatus()
        );
    }

    /**
     * 启动任务调度线程
     */
    private void startTaskScheduler() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                // 检查是否可以执行新任务
                while (activeTaskCount.get() < MAX_CONCURRENT_TASKS && !taskQueue.isEmpty()) {
                    // 从队列中获取优先级最高的任务
                    ScheduledTask task = taskQueue.poll();
                    if (task != null) {
                        // 提交任务到执行线程池
                        submitTaskForExecution(task);
                    }
                }
            } catch (Exception e) {
                logger.error("Error in task scheduler loop", e);
            }
        }, 0, 100, TimeUnit.MILLISECONDS); // 每100毫秒检查一次
    }

    /**
     * 提交任务到执行线程池
     */
    private void submitTaskForExecution(ScheduledTask task) {
        activeTaskCount.incrementAndGet();
        
        taskExecutor.submit(() -> {
            try {
                // 执行任务
                executeTask(task);
            } catch (Exception e) {
                logger.error("Error executing task: taskId={}", task.getTaskId(), e);
            } finally {
                // 任务执行完成，减少活跃任务计数
                activeTaskCount.decrementAndGet();
            }
        });
    }

    /**
     * 执行具体任务
     */
    private void executeTask(ScheduledTask task) {
        logger.info("Executing task: taskId={}, taskType={}", task.getTaskId(), task.getTaskType());
        
        try {
            // 根据任务类型执行不同的任务逻辑
            switch (task.getTaskType()) {
                case "VECTOR_SYNC":
                    executeVectorSyncTask(task);
                    break;
                case "DATA_CONSISTENCY_CHECK":
                    executeDataConsistencyCheckTask(task);
                    break;
                case "INDEX_OPTIMIZATION":
                    executeIndexOptimizationTask(task);
                    break;
                default:
                    logger.warn("Unknown task type: {}", task.getTaskType());
            }
            
            logger.info("Task executed successfully: taskId={}", task.getTaskId());
        } catch (Exception e) {
            logger.error("Error executing task: taskId={}", task.getTaskId(), e);
        }
    }

    /**
     * 执行向量同步任务
     */
    private void executeVectorSyncTask(ScheduledTask task) {
        // 实际实现：调用VectorSyncService执行向量同步
        logger.info("Executing vector sync task: taskId={}", task.getTaskId());
        // 模拟执行
        try {
            Thread.sleep(1000); // 模拟任务执行时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 执行数据一致性检查任务
     */
    private void executeDataConsistencyCheckTask(ScheduledTask task) {
        // 实际实现：调用DataConsistencyService执行一致性检查
        logger.info("Executing data consistency check task: taskId={}", task.getTaskId());
        // 模拟执行
        try {
            Thread.sleep(500); // 模拟任务执行时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 执行索引优化任务
     */
    private void executeIndexOptimizationTask(ScheduledTask task) {
        // 实际实现：执行向量库索引优化
        logger.info("Executing index optimization task: taskId={}", task.getTaskId());
        // 模拟执行
        try {
            Thread.sleep(800); // 模拟任务执行时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取线程池状态
     */
    private Map<String, Object> getThreadPoolStatus() {
        if (taskExecutor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) taskExecutor;
            return Map.of(
                "corePoolSize", executor.getCorePoolSize(),
                "maximumPoolSize", executor.getMaximumPoolSize(),
                "poolSize", executor.getPoolSize(),
                "activeCount", executor.getActiveCount(),
                "queueSize", executor.getQueue().size(),
                "completedTaskCount", executor.getCompletedTaskCount()
            );
        }
        return Map.of("status", "UNKNOWN");
    }

    /**
     * 调度任务类
     */
    private static class ScheduledTask implements Comparable<ScheduledTask> {
        private final String taskId;
        private final String taskType;
        private final Map<String, Object> taskParams;
        private final int priority;
        private final long scheduledTime;

        public ScheduledTask(String taskId, String taskType, Map<String, Object> taskParams, int priority, long scheduledTime) {
            this.taskId = taskId;
            this.taskType = taskType;
            this.taskParams = taskParams;
            this.priority = priority;
            this.scheduledTime = scheduledTime;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getTaskType() {
            return taskType;
        }

        public Map<String, Object> getTaskParams() {
            return taskParams;
        }

        public int getPriority() {
            return priority;
        }

        public long getScheduledTime() {
            return scheduledTime;
        }

        @Override
        public int compareTo(ScheduledTask other) {
            // 优先级高的任务先执行
            if (this.priority != other.priority) {
                return Integer.compare(other.priority, this.priority);
            }
            // 优先级相同时，先调度的任务先执行
            return Long.compare(this.scheduledTime, other.scheduledTime);
        }
    }
}
