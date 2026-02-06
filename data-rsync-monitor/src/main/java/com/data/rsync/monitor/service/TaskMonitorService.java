package com.data.rsync.monitor.service;

import java.util.List;
import java.util.Map;

/**
 * 任务监控服务接口
 * 监控同步任务的执行状态，并在出现异常时发送告警
 */
public interface TaskMonitorService {

    /**
     * 监控任务状态
     * @param taskId 任务ID
     * @param status 任务状态
     * @param progress 任务进度
     * @param errorMessage 错误信息
     */
    void monitorTaskStatus(Long taskId, String status, int progress, String errorMessage);

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    Map<String, Object> getTaskStatus(Long taskId);

    /**
     * 获取所有任务状态
     * @return 所有任务状态
     */
    List<Map<String, Object>> getAllTaskStatus();

    /**
     * 清理任务状态缓存
     * @param taskId 任务ID
     */
    void cleanTaskStatusCache(Long taskId);

    /**
     * 清理过期任务状态
     */
    void cleanExpiredTaskStatus();

    /**
     * 监控系统资源使用情况
     */
    void monitorSystemResources();

    /**
     * 执行任务状态巡检
     */
    void performTaskStatusCheck();

}
