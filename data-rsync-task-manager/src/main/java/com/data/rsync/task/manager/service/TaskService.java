package com.data.rsync.task.manager.service;

import com.data.rsync.task.manager.entity.TaskEntity;
import com.data.rsync.task.manager.entity.TaskNodeEntity;
import com.data.rsync.task.manager.entity.TaskConnectionEntity;
import com.data.rsync.task.manager.entity.TaskDependencyEntity;
import java.util.List;
import java.util.Map;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 创建任务
     * @param taskEntity 任务实体
     * @return 创建的任务
     */
    TaskEntity createTask(TaskEntity taskEntity);

    /**
     * 更新任务
     * @param taskEntity 任务实体
     * @return 更新后的任务
     */
    TaskEntity updateTask(TaskEntity taskEntity);

    /**
     * 删除任务
     * @param id 任务ID
     */
    void deleteTask(Long id);

    /**
     * 启停任务
     * @param id 任务ID
     * @param enabled 启用状态
     */
    void toggleTask(Long id, Boolean enabled);

    /**
     * 根据ID查询任务
     * @param id 任务ID
     * @return 任务实体
     */
    TaskEntity getTaskById(Long id);

    /**
     * 根据名称查询任务
     * @param name 任务名称
     * @return 任务实体
     */
    TaskEntity getTaskByName(String name);

    /**
     * 查询所有任务
     * @return 任务列表
     */
    List<TaskEntity> getAllTasks();

    /**
     * 根据状态查询任务
     * @param status 任务状态
     * @return 任务列表
     */
    List<TaskEntity> getTasksByStatus(String status);

    /**
     * 根据数据源ID查询任务
     * @param dataSourceId 数据源ID
     * @return 任务列表
     */
    List<TaskEntity> getTasksByDataSourceId(Long dataSourceId);

    /**
     * 触发任务执行
     * @param id 任务ID
     * @return 任务执行结果
     */
    Map<String, Object> triggerTask(Long id);

    /**
     * 计算下次执行时间
     * @param taskEntity 任务实体
     * @return 下次执行时间
     */
    TaskEntity calculateNextExecTime(TaskEntity taskEntity);

    /**
     * 执行定时任务
     */
    void executeScheduledTasks();

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 任务状态
     * @param errorMessage 错误信息
     */
    void updateTaskStatus(Long id, String status, String errorMessage);

    /**
     * 更新任务进度
     * @param id 任务ID
     * @param progress 任务进度
     */
    void updateTaskProgress(Long id, Integer progress);

    /**
     * 暂停任务
     * @param id 任务ID
     * @return 暂停结果
     */
    boolean pauseTask(Long id);

    /**
     * 继续任务
     * @param id 任务ID
     * @return 继续结果
     */
    boolean resumeTask(Long id);

    /**
     * 回滚任务
     * @param id 任务ID
     * @param rollbackPoint 回滚点
     * @return 回滚结果
     */
    boolean rollbackTask(Long id, String rollbackPoint);

    /**
     * 获取任务历史版本
     * @param id 任务ID
     * @return 历史版本列表
     */
    List<Map<String, Object>> getTaskVersions(Long id);

    /**
     * 保存任务节点
     * @param taskId 任务ID
     * @param nodes 节点列表
     */
    void saveTaskNodes(Long taskId, List<TaskNodeEntity> nodes);

    /**
     * 保存任务连接
     * @param taskId 任务ID
     * @param connections 连接列表
     */
    void saveTaskConnections(Long taskId, List<TaskConnectionEntity> connections);

    /**
     * 保存任务依赖
     * @param taskId 任务ID
     * @param dependency 依赖信息
     */
    void saveTaskDependency(Long taskId, TaskDependencyEntity dependency);

    /**
     * 获取任务节点
     * @param taskId 任务ID
     * @return 节点列表
     */
    List<TaskNodeEntity> getTaskNodes(Long taskId);

    /**
     * 获取任务连接
     * @param taskId 任务ID
     * @return 连接列表
     */
    List<TaskConnectionEntity> getTaskConnections(Long taskId);

    /**
     * 获取任务依赖
     * @param taskId 任务ID
     * @return 依赖信息
     */
    List<TaskDependencyEntity> getTaskDependencies(Long taskId);

    /**
     * 验证任务流程
     * @param taskId 任务ID
     * @return 验证结果
     */
    Map<String, Object> validateTaskFlow(Long taskId);

    /**
     * 保存任务错误数据
     * @param errorData 错误数据实体
     * @return 保存的错误数据
     */
    TaskErrorDataEntity saveTaskErrorData(TaskErrorDataEntity errorData);

    /**
     * 根据任务ID查询错误数据
     * @param taskId 任务ID
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> getTaskErrorData(Long taskId);

    /**
     * 根据任务ID和错误类型查询错误数据
     * @param taskId 任务ID
     * @param errorType 错误类型
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> getTaskErrorDataByType(Long taskId, String errorType);

    /**
     * 根据任务ID和同步环节查询错误数据
     * @param taskId 任务ID
     * @param syncStage 同步环节
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> getTaskErrorDataByStage(Long taskId, String syncStage);

    /**
     * 重试错误数据
     * @param errorDataId 错误数据ID
     * @return 重试结果
     */
    boolean retryTaskErrorData(Long errorDataId);

    /**
     * 批量重试错误数据
     * @param errorDataIds 错误数据ID列表
     * @return 重试结果
     */
    Map<String, Object> batchRetryTaskErrorData(List<Long> errorDataIds);

    /**
     * 清理任务错误数据
     * @param taskId 任务ID
     * @return 清理结果
     */
    boolean cleanTaskErrorData(Long taskId);

    /**
     * 保存向量化配置
     * @param config 向量化配置实体
     * @return 保存的向量化配置
     */
    VectorizationConfigEntity saveVectorizationConfig(VectorizationConfigEntity config);

    /**
     * 根据任务ID查询向量化配置
     * @param taskId 任务ID
     * @return 向量化配置列表
     */
    List<VectorizationConfigEntity> getVectorizationConfigByTaskId(Long taskId);

    /**
     * 根据ID查询向量化配置
     * @param id 配置ID
     * @return 向量化配置
     */
    VectorizationConfigEntity getVectorizationConfigById(Long id);

    /**
     * 删除向量化配置
     * @param id 配置ID
     */
    void deleteVectorizationConfig(Long id);

    /**
     * 生成向量化预览
     * @param config 向量化配置
     * @param sourceData 源数据
     * @return 向量化预览结果
     */
    Map<String, Object> generateVectorizationPreview(VectorizationConfigEntity config, String sourceData);

    /**
     * 保存Milvus索引信息
     * @param index Milvus索引实体
     * @return 保存的索引信息
     */
    MilvusIndexEntity saveMilvusIndex(MilvusIndexEntity index);

    /**
     * 根据集合名称查询索引
     * @param collectionName 集合名称
     * @return 索引列表
     */
    List<MilvusIndexEntity> getMilvusIndexesByCollection(String collectionName);

    /**
     * 根据ID查询索引
     * @param id 索引ID
     * @return 索引信息
     */
    MilvusIndexEntity getMilvusIndexById(Long id);

    /**
     * 根据集合名称和索引名称查询索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 索引信息
     */
    MilvusIndexEntity getMilvusIndexByName(String collectionName, String indexName);

    /**
     * 更新索引状态
     * @param id 索引ID
     * @param status 状态
     * @param progress 进度
     */
    void updateMilvusIndexStatus(Long id, String status, Integer progress);

    /**
     * 删除索引
     * @param id 索引ID
     */
    void deleteMilvusIndex(Long id);

    /**
     * 根据集合名称和索引名称删除索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     */
    void deleteMilvusIndexByName(String collectionName, String indexName);

    /**
     * 重建索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 重建结果
     */
    boolean rebuildMilvusIndex(String collectionName, String indexName);

}
