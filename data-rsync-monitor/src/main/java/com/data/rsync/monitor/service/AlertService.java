package com.data.rsync.monitor.service;

/**
 * 告警服务接口
 * 负责发送告警信息，在任务执行异常时发送告警通知
 */
public interface AlertService {

    /**
     * 发送任务失败告警
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    void sendTaskFailureAlert(Long taskId, String errorMessage);

    /**
     * 发送任务警告告警
     * @param taskId 任务ID
     * @param warningMessage 警告信息
     */
    void sendTaskWarningAlert(Long taskId, String warningMessage);

    /**
     * 发送任务超时告警
     * @param taskId 任务ID
     * @param timeoutMessage 超时信息
     */
    void sendTaskTimeoutAlert(Long taskId, String timeoutMessage);

    /**
     * 发送任务状态告警
     * @param taskId 任务ID
     * @param statusMessage 状态信息
     */
    void sendTaskStatusAlert(Long taskId, String statusMessage);

    /**
     * 发送系统资源告警
     * @param resourceType 资源类型
     * @param usage 资源使用率
     */
    void sendSystemResourceAlert(String resourceType, double usage);

    /**
     * 发送自定义告警
     * @param type 告警类型
     * @param title 告警标题
     * @param message 告警消息
     * @param taskId 任务ID
     */
    void sendCustomAlert(String type, String title, String message, Long taskId);

    /**
     * 检查告警频率限制
     * @param type 告警类型
     * @param taskId 任务ID
     * @return 是否允许发送告警
     */
    boolean checkAlertRateLimit(String type, Long taskId);

    /**
     * 清理过期告警
     */
    void cleanExpiredAlerts();

}
