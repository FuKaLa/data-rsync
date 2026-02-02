package com.data.rsync.log.listener.service;

import com.data.rsync.common.model.Task;

/**
 * 日志监听服务接口
 */
public interface LogListenerService {

    /**
     * 启动日志监听
     * @param task 任务
     * @return 监听结果
     */
    boolean startLogListener(Task task);

    /**
     * 停止日志监听
     * @param taskId 任务ID
     * @return 停止结果
     */
    boolean stopLogListener(Long taskId);

    /**
     * 获取监听状态
     * @param taskId 任务ID
     * @return 监听状态
     */
    String getListenerStatus(Long taskId);

    /**
     * 执行全量扫描
     * @param task 任务
     * @return 扫描结果
     */
    boolean executeFullScan(Task task);

    /**
     * 获取断点续传位点
     * @param taskId 任务ID
     * @return 断点续传位点
     */
    String getBreakpoint(Long taskId);

    /**
     * 设置断点续传位点
     * @param taskId 任务ID
     * @param breakpoint 断点续传位点
     */
    void setBreakpoint(Long taskId, String breakpoint);

    /**
     * 清理断点续传位点
     * @param taskId 任务ID
     */
    void clearBreakpoint(Long taskId);

    /**
     * 检查监听健康状态
     * @param taskId 任务ID
     * @return 健康状态
     */
    String checkListenerHealth(Long taskId);

}
