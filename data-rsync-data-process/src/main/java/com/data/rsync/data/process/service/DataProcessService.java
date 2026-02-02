package com.data.rsync.data.process.service;

import com.data.rsync.common.model.Task;

import java.util.Map;

/**
 * 数据处理服务接口
 */
public interface DataProcessService {

    /**
     * 处理数据变更
     * @param taskId 任务ID
     * @param dataChange 数据变更
     * @return 处理结果
     */
    boolean processDataChange(Long taskId, Map<String, Object> dataChange);

    /**
     * 执行数据转换
     * @param task 任务
     * @param data 原始数据
     * @return 转换后的数据
     */
    Map<String, Object> executeDataTransform(Task task, Map<String, Object> data);

    /**
     * 生成向量
     * @param task 任务
     * @param data 原始数据
     * @return 向量
     */
    float[] generateVector(Task task, Map<String, Object> data);

    /**
     * 执行数据清洗
     * @param task 任务
     * @param data 原始数据
     * @return 清洗后的数据
     */
    Map<String, Object> executeDataCleaning(Task task, Map<String, Object> data);

    /**
     * 批量处理数据
     * @param taskId 任务ID
     * @param dataList 数据列表
     * @return 处理结果
     */
    boolean batchProcessData(Long taskId, java.util.List<Map<String, Object>> dataList);

    /**
     * 获取处理状态
     * @param taskId 任务ID
     * @return 处理状态
     */
    String getProcessStatus(Long taskId);

    /**
     * 检查处理健康状态
     * @param taskId 任务ID
     * @return 健康状态
     */
    String checkProcessHealth(Long taskId);

}
