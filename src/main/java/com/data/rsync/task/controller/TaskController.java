package com.data.rsync.task.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.task.entity.TaskEntity;
import com.data.rsync.task.entity.TaskNodeEntity;
import com.data.rsync.task.entity.TaskConnectionEntity;
import com.data.rsync.task.entity.TaskDependencyEntity;
import com.data.rsync.task.entity.TaskErrorDataEntity;
import com.data.rsync.task.entity.VectorizationConfigEntity;
import com.data.rsync.task.entity.MilvusIndexEntity;
import com.data.rsync.task.service.TaskService;
import com.data.rsync.task.vo.TaskTriggerResponse;
import com.data.rsync.task.vo.TaskFlowValidateResponse;
import com.data.rsync.task.vo.TaskVersionResponse;
import com.data.rsync.task.vo.BatchRetryResponse;
import com.data.rsync.task.vo.VectorizationPreviewRequest;
import com.data.rsync.task.vo.VectorizationPreviewResponse;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 任务管理控制器
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Resource
    private TaskService taskService;

    /**
     * 创建任务
     * @param taskEntity 任务实体
     * @return 创建结果
     */
    @PostMapping
    public Response<TaskEntity> createTask(@RequestBody TaskEntity taskEntity) {
        try {
            logger.info("收到任务创建请求，参数：{}", taskEntity.toString());
            TaskEntity createdTask = taskService.createTask(taskEntity);
            logger.info("任务创建成功，创建的任务：{}", createdTask.toString());
            return Response.success("任务创建成功", createdTask);
        } catch (Exception e) {
            logger.error("任务创建失败，错误信息：{}", e.getMessage(), e);
            return Response.failure(500, "创建任务失败");
        }
    }

    /**
     * 更新任务
     * @param taskEntity 任务实体
     * @return 更新结果
     */
    @PutMapping
    public Response<TaskEntity> updateTask(@RequestBody TaskEntity taskEntity) {
        try {
            TaskEntity updatedTask = taskService.updateTask(taskEntity);
            return Response.success("任务更新成功", updatedTask);
        } catch (Exception e) {
            return Response.failure(500, "更新任务失败");
        }
    }

    /**
     * 删除任务
     * @param id 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Response<String> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return Response.success("任务删除成功", null);
        } catch (Exception e) {
            return Response.failure(500, "删除任务失败");
        }
    }

    /**
     * 启停任务
     * @param id 任务ID
     * @param enabled 启用状态
     * @return 操作结果
     */
    @PatchMapping("/{id}/toggle")
    public Response<String> toggleTask(@PathVariable Long id, @RequestParam Boolean enabled) {
        try {
            taskService.toggleTask(id, enabled);
            return Response.success(enabled ? "任务已启用" : "任务已禁用", null);
        } catch (Exception e) {
            return Response.failure(500, "启停任务失败");
        }
    }

    /**
     * 根据ID查询任务
     * @param id 任务ID
     * @return 任务信息
     */
    @GetMapping("/{id}")
    public Response<TaskEntity> getTaskById(@PathVariable Long id) {
        try {
            TaskEntity taskEntity = taskService.getTaskById(id);
            if (taskEntity == null) {
                return Response.failure(404, "任务不存在");
            }
            return Response.success(taskEntity);
        } catch (Exception e) {
            return Response.failure(500, "获取任务详情失败");
        }
    }

    /**
     * 根据名称查询任务
     * @param name 任务名称
     * @return 任务信息
     */
    @GetMapping("/name/{name}")
    public Response<TaskEntity> getTaskByName(@PathVariable String name) {
        try {
            TaskEntity taskEntity = taskService.getTaskByName(name);
            if (taskEntity == null) {
                return Response.failure(404, "任务不存在");
            }
            return Response.success(taskEntity);
        } catch (Exception e) {
            return Response.failure(500, "获取任务详情失败");
        }
    }

    /**
     * 查询所有任务
     * @return 任务列表
     */
    @GetMapping
    public Response<List<TaskEntity>> getAllTasks() {
        try {
            List<TaskEntity> tasks = taskService.getAllTasks();
            return Response.success(tasks);
        } catch (Exception e) {
            return Response.failure(500, "获取任务列表失败");
        }
    }

    /**
     * 根据状态查询任务
     * @param status 任务状态
     * @return 任务列表
     */
    @GetMapping("/status/{status}")
    public Response<List<TaskEntity>> getTasksByStatus(@PathVariable String status) {
        try {
            List<TaskEntity> tasks = taskService.getTasksByStatus(status);
            return Response.success(tasks);
        } catch (Exception e) {
            return Response.failure(500, "获取任务列表失败");
        }
    }

    /**
     * 根据数据源ID查询任务
     * @param dataSourceId 数据源ID
     * @return 任务列表
     */
    @GetMapping("/data-source/{dataSourceId}")
    public Response<List<TaskEntity>> getTasksByDataSourceId(@PathVariable Long dataSourceId) {
        try {
            List<TaskEntity> tasks = taskService.getTasksByDataSourceId(dataSourceId);
            return Response.success(tasks);
        } catch (Exception e) {
            return Response.failure(500, "获取任务列表失败");
        }
    }

    /**
     * 触发任务执行
     * @param id 任务ID
     * @return 执行结果
     */
    @PostMapping("/{id}/trigger")
    public Response<TaskTriggerResponse> triggerTask(@PathVariable Long id) {
        try {
            TaskTriggerResponse response = taskService.triggerTask(id);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "触发任务执行失败");
        }
    }

    /**
     * 执行定时任务
     * @return 执行结果
     */
    @PostMapping("/execute-scheduled")
    public Response<String> executeScheduledTasks() {
        try {
            taskService.executeScheduledTasks();
            return Response.success("定时任务执行完成", null);
        } catch (Exception e) {
            return Response.failure(500, "执行定时任务失败");
        }
    }

    /**
     * 保存任务节点
     * @param taskId 任务ID
     * @param nodes 节点列表
     * @return 保存结果
     */
    @PostMapping("/{taskId}/nodes")
    public Response<String> saveTaskNodes(@PathVariable Long taskId, @RequestBody List<TaskNodeEntity> nodes) {
        try {
            taskService.saveTaskNodes(taskId, nodes);
            return Response.success("任务节点保存成功", null);
        } catch (Exception e) {
            return Response.failure(500, "保存任务节点失败");
        }
    }

    /**
     * 保存任务连接
     * @param taskId 任务ID
     * @param connections 连接列表
     * @return 保存结果
     */
    @PostMapping("/{taskId}/connections")
    public Response<String> saveTaskConnections(@PathVariable Long taskId, @RequestBody List<TaskConnectionEntity> connections) {
        try {
            taskService.saveTaskConnections(taskId, connections);
            return Response.success("任务连接保存成功", null);
        } catch (Exception e) {
            return Response.failure(500, "保存任务连接失败");
        }
    }

    /**
     * 保存任务依赖
     * @param taskId 任务ID
     * @param dependency 依赖信息
     * @return 保存结果
     */
    @PostMapping("/{taskId}/dependency")
    public Response<String> saveTaskDependency(@PathVariable Long taskId, @RequestBody TaskDependencyEntity dependency) {
        try {
            taskService.saveTaskDependency(taskId, dependency);
            return Response.success("任务依赖保存成功", null);
        } catch (Exception e) {
            return Response.failure(500, "保存任务依赖失败");
        }
    }

    /**
     * 获取任务节点
     * @param taskId 任务ID
     * @return 节点列表
     */
    @GetMapping("/{taskId}/nodes")
    public Response<List<TaskNodeEntity>> getTaskNodes(@PathVariable Long taskId) {
        try {
            List<TaskNodeEntity> nodes = taskService.getTaskNodes(taskId);
            return Response.success(nodes);
        } catch (Exception e) {
            return Response.failure(500, "获取任务节点失败");
        }
    }

    /**
     * 获取任务连接
     * @param taskId 任务ID
     * @return 连接列表
     */
    @GetMapping("/{taskId}/connections")
    public Response<List<TaskConnectionEntity>> getTaskConnections(@PathVariable Long taskId) {
        try {
            List<TaskConnectionEntity> connections = taskService.getTaskConnections(taskId);
            return Response.success(connections);
        } catch (Exception e) {
            return Response.failure(500, "获取任务连接失败");
        }
    }

    /**
     * 获取任务依赖
     * @param taskId 任务ID
     * @return 依赖信息
     */
    @GetMapping("/{taskId}/dependencies")
    public Response<List<TaskDependencyEntity>> getTaskDependencies(@PathVariable Long taskId) {
        try {
            List<TaskDependencyEntity> dependencies = taskService.getTaskDependencies(taskId);
            return Response.success(dependencies);
        } catch (Exception e) {
            return Response.failure(500, "获取任务依赖失败");
        }
    }

    /**
     * 验证任务流程
     * @param taskId 任务ID
     * @return 验证结果
     */
    @PostMapping("/{taskId}/validate-flow")
    public Response<TaskFlowValidateResponse> validateTaskFlow(@PathVariable Long taskId) {
        try {
            TaskFlowValidateResponse response = taskService.validateTaskFlow(taskId);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "验证任务流程失败");
        }
    }

    /**
     * 暂停任务
     * @param id 任务ID
     * @return 暂停结果
     */
    @PostMapping("/{id}/pause")
    public Response<Boolean> pauseTask(@PathVariable Long id) {
        try {
            boolean result = taskService.pauseTask(id);
            return Response.success("任务暂停成功", result);
        } catch (Exception e) {
            return Response.failure(500, "暂停任务失败");
        }
    }

    /**
     * 继续任务
     * @param id 任务ID
     * @return 继续结果
     */
    @PostMapping("/{id}/resume")
    public Response<Boolean> resumeTask(@PathVariable Long id) {
        try {
            boolean result = taskService.resumeTask(id);
            return Response.success("任务继续成功", result);
        } catch (Exception e) {
            return Response.failure(500, "继续任务失败");
        }
    }

    /**
     * 回滚任务
     * @param id 任务ID
     * @param rollbackPoint 回滚点
     * @return 回滚结果
     */
    @PostMapping("/{id}/rollback")
    public Response<Boolean> rollbackTask(@PathVariable Long id, @RequestParam String rollbackPoint) {
        try {
            boolean result = taskService.rollbackTask(id, rollbackPoint);
            return Response.success("任务回滚成功", result);
        } catch (Exception e) {
            return Response.failure(500, "回滚任务失败");
        }
    }

    /**
     * 获取任务历史版本
     * @param id 任务ID
     * @return 历史版本列表
     */
    @GetMapping("/{id}/versions")
    public Response<List<TaskVersionResponse>> getTaskVersions(@PathVariable Long id) {
        try {
            List<TaskVersionResponse> versions = taskService.getTaskVersions(id);
            return Response.success(versions);
        } catch (Exception e) {
            return Response.failure(500, "获取任务历史版本失败");
        }
    }

    /**
     * 获取任务错误数据
     * @param taskId 任务ID
     * @return 错误数据列表
     */
    @GetMapping("/{taskId}/error-data")
    public Response<List<TaskErrorDataEntity>> getTaskErrorData(@PathVariable Long taskId, 
                                                              @RequestParam(required = false) String errorType, 
                                                              @RequestParam(required = false) String syncStage) {
        try {
            List<TaskErrorDataEntity> errorDataList;
            if (errorType != null) {
                errorDataList = taskService.getTaskErrorDataByType(taskId, errorType);
            } else if (syncStage != null) {
                errorDataList = taskService.getTaskErrorDataByStage(taskId, syncStage);
            } else {
                errorDataList = taskService.getTaskErrorData(taskId);
            }
            return Response.success(errorDataList);
        } catch (Exception e) {
            return Response.failure(500, "获取任务错误数据失败");
        }
    }

    /**
     * 重试错误数据
     * @param errorDataId 错误数据ID
     * @return 重试结果
     */
    @PostMapping("/error-data/{errorDataId}/retry")
    public Response<Boolean> retryTaskErrorData(@PathVariable Long errorDataId) {
        try {
            boolean result = taskService.retryTaskErrorData(errorDataId);
            return Response.success("错误数据重试成功", result);
        } catch (Exception e) {
            return Response.failure(500, "重试错误数据失败");
        }
    }

    /**
     * 批量重试错误数据
     * @param errorDataIds 错误数据ID列表
     * @return 重试结果
     */
    @PostMapping("/error-data/batch-retry")
    public Response<BatchRetryResponse> batchRetryTaskErrorData(@RequestBody List<Long> errorDataIds) {
        try {
            BatchRetryResponse response = taskService.batchRetryTaskErrorData(errorDataIds);
            return Response.success("批量重试完成", response);
        } catch (Exception e) {
            return Response.failure(500, "批量重试错误数据失败");
        }
    }

    /**
     * 清理任务错误数据
     * @param taskId 任务ID
     * @return 清理结果
     */
    @DeleteMapping("/{taskId}/error-data")
    public Response<Boolean> cleanTaskErrorData(@PathVariable Long taskId) {
        try {
            boolean result = taskService.cleanTaskErrorData(taskId);
            return Response.success("错误数据清理成功", result);
        } catch (Exception e) {
            return Response.failure(500, "清理错误数据失败");
        }
    }

    /**
     * 保存向量化配置
     * @param config 向量化配置
     * @return 保存结果
     */
    @PostMapping("/vectorization-config")
    public Response<VectorizationConfigEntity> saveVectorizationConfig(@RequestBody VectorizationConfigEntity config) {
        try {
            VectorizationConfigEntity savedConfig = taskService.saveVectorizationConfig(config);
            return Response.success("向量化配置保存成功", savedConfig);
        } catch (Exception e) {
            return Response.failure(500, "保存向量化配置失败");
        }
    }

    /**
     * 根据任务ID查询向量化配置
     * @param taskId 任务ID
     * @return 向量化配置列表
     */
    @GetMapping("/{taskId}/vectorization-config")
    public Response<List<VectorizationConfigEntity>> getVectorizationConfigByTaskId(@PathVariable Long taskId) {
        try {
            List<VectorizationConfigEntity> configs = taskService.getVectorizationConfigByTaskId(taskId);
            return Response.success(configs);
        } catch (Exception e) {
            return Response.failure(500, "获取向量化配置失败");
        }
    }

    /**
     * 根据ID查询向量化配置
     * @param id 配置ID
     * @return 向量化配置
     */
    @GetMapping("/vectorization-config/{id}")
    public Response<VectorizationConfigEntity> getVectorizationConfigById(@PathVariable Long id) {
        try {
            VectorizationConfigEntity config = taskService.getVectorizationConfigById(id);
            if (config == null) {
                return Response.failure(404, "向量化配置不存在");
            }
            return Response.success(config);
        } catch (Exception e) {
            return Response.failure(500, "获取向量化配置失败");
        }
    }

    /**
     * 删除向量化配置
     * @param id 配置ID
     * @return 删除结果
     */
    @DeleteMapping("/vectorization-config/{id}")
    public Response<String> deleteVectorizationConfig(@PathVariable Long id) {
        try {
            taskService.deleteVectorizationConfig(id);
            return Response.success("向量化配置删除成功", null);
        } catch (Exception e) {
            return Response.failure(500, "删除向量化配置失败");
        }
    }

    /**
     * 生成向量化预览
     * @param previewRequest 预览请求参数
     * @return 预览结果
     */
    @PostMapping("/vectorization-config/preview")
    public Response<VectorizationPreviewResponse> generateVectorizationPreview(@RequestBody VectorizationPreviewRequest previewRequest) {
        try {
            // 构建向量化配置
            VectorizationConfigEntity config = new VectorizationConfigEntity();
            config.setAlgorithm(previewRequest.getAlgorithm());
            config.setDimension(previewRequest.getDimension());
            config.setModelName(previewRequest.getModelName());
            
            String sourceData = previewRequest.getSourceData();
            
            VectorizationPreviewResponse response = taskService.generateVectorizationPreview(config, sourceData);
            return Response.success("向量化预览生成成功", response);
        } catch (Exception e) {
            return Response.failure(500, "生成向量化预览失败");
        }
    }

    /**
     * 保存Milvus索引
     * @param index 索引信息
     * @return 保存结果
     */
    @PostMapping("/milvus-index")
    public Response<MilvusIndexEntity> saveMilvusIndex(@RequestBody MilvusIndexEntity index) {
        try {
            MilvusIndexEntity savedIndex = taskService.saveMilvusIndex(index);
            return Response.success("索引保存成功", savedIndex);
        } catch (Exception e) {
            return Response.failure(500, "保存Milvus索引失败");
        }
    }

    /**
     * 根据集合名称查询索引
     * @param collectionName 集合名称
     * @return 索引列表
     */
    @GetMapping("/milvus-index/collection/{collectionName}")
    public Response<List<MilvusIndexEntity>> getMilvusIndexesByCollection(@PathVariable String collectionName) {
        try {
            List<MilvusIndexEntity> indexes = taskService.getMilvusIndexesByCollection(collectionName);
            return Response.success(indexes);
        } catch (Exception e) {
            return Response.failure(500, "获取Milvus索引失败");
        }
    }

    /**
     * 根据ID查询索引
     * @param id 索引ID
     * @return 索引信息
     */
    @GetMapping("/milvus-index/{id}")
    public Response<MilvusIndexEntity> getMilvusIndexById(@PathVariable Long id) {
        try {
            MilvusIndexEntity index = taskService.getMilvusIndexById(id);
            if (index == null) {
                return Response.failure(404, "索引不存在");
            }
            return Response.success(index);
        } catch (Exception e) {
            return Response.failure(500, "获取Milvus索引失败");
        }
    }

    /**
     * 更新索引状态
     * @param id 索引ID
     * @param status 状态
     * @param progress 进度
     * @return 更新结果
     */
    @PatchMapping("/milvus-index/{id}/status")
    public Response<String> updateMilvusIndexStatus(@PathVariable Long id, 
                                                 @RequestParam String status, 
                                                 @RequestParam Integer progress) {
        try {
            taskService.updateMilvusIndexStatus(id, status, progress);
            return Response.success("索引状态更新成功", null);
        } catch (Exception e) {
            return Response.failure(500, "更新索引状态失败");
        }
    }

    /**
     * 删除索引
     * @param id 索引ID
     * @return 删除结果
     */
    @DeleteMapping("/milvus-index/{id}")
    public Response<String> deleteMilvusIndex(@PathVariable Long id) {
        try {
            taskService.deleteMilvusIndex(id);
            return Response.success("索引删除成功", null);
        } catch (Exception e) {
            return Response.failure(500, "删除Milvus索引失败");
        }
    }

    /**
     * 根据集合名称和索引名称删除索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 删除结果
     */
    @DeleteMapping("/milvus-index")
    public Response<String> deleteMilvusIndexByName(@RequestParam String collectionName, 
                                                 @RequestParam String indexName) {
        try {
            taskService.deleteMilvusIndexByName(collectionName, indexName);
            return Response.success("索引删除成功", null);
        } catch (Exception e) {
            return Response.failure(500, "删除Milvus索引失败");
        }
    }

    /**
     * 重建索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 重建结果
     */
    @PostMapping("/milvus-index/rebuild")
    public Response<Boolean> rebuildMilvusIndex(@RequestParam String collectionName, 
                                             @RequestParam String indexName) {
        try {
            boolean result = taskService.rebuildMilvusIndex(collectionName, indexName);
            return Response.success("索引重建成功", result);
        } catch (Exception e) {
            return Response.failure(500, "重建Milvus索引失败");
        }
    }

}
