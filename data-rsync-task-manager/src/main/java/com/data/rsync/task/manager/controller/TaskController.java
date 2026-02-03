package com.data.rsync.task.manager.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.task.manager.entity.TaskEntity;
import com.data.rsync.task.manager.entity.TaskNodeEntity;
import com.data.rsync.task.manager.entity.TaskConnectionEntity;
import com.data.rsync.task.manager.entity.TaskDependencyEntity;
import com.data.rsync.task.manager.entity.TaskErrorDataEntity;
import com.data.rsync.task.manager.entity.VectorizationConfigEntity;
import com.data.rsync.task.manager.entity.MilvusIndexEntity;
import com.data.rsync.task.manager.service.TaskService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务管理控制器
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

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
            TaskEntity createdTask = taskService.createTask(taskEntity);
            return Response.success("任务创建成功", createdTask);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 触发任务执行
     * @param id 任务ID
     * @return 执行结果
     */
    @PostMapping("/{id}/trigger")
    public Response<Map<String, Object>> triggerTask(@PathVariable Long id) {
        try {
            Map<String, Object> result = taskService.triggerTask(id);
            return Response.success(result);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 验证任务流程
     * @param taskId 任务ID
     * @return 验证结果
     */
    @PostMapping("/{taskId}/validate-flow")
    public Response<Map<String, Object>> validateTaskFlow(@PathVariable Long taskId) {
        try {
            Map<String, Object> result = taskService.validateTaskFlow(taskId);
            return Response.success(result);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 获取任务历史版本
     * @param id 任务ID
     * @return 历史版本列表
     */
    @GetMapping("/{id}/versions")
    public Response<List<Map<String, Object>>> getTaskVersions(@PathVariable Long id) {
        try {
            List<Map<String, Object>> versions = taskService.getTaskVersions(id);
            return Response.success(versions);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 批量重试错误数据
     * @param errorDataIds 错误数据ID列表
     * @return 重试结果
     */
    @PostMapping("/error-data/batch-retry")
    public Response<Map<String, Object>> batchRetryTaskErrorData(@RequestBody List<Long> errorDataIds) {
        try {
            Map<String, Object> result = taskService.batchRetryTaskErrorData(errorDataIds);
            return Response.success("批量重试完成", result);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 生成向量化预览
     * @param request 预览请求参数
     * @return 预览结果
     */
    @PostMapping("/vectorization-config/preview")
    public Response<Map<String, Object>> generateVectorizationPreview(@RequestBody Map<String, Object> request) {
        try {
            // 构建向量化配置
            VectorizationConfigEntity config = new VectorizationConfigEntity();
            config.setAlgorithm((String) request.get("algorithm"));
            config.setDimension((Integer) request.get("dimension"));
            config.setModelName((String) request.get("modelName"));
            
            String sourceData = (String) request.get("sourceData");
            
            Map<String, Object> previewResult = taskService.generateVectorizationPreview(config, sourceData);
            return Response.success(previewResult);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
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
            return Response.failure(500, e.getMessage());
        }
    }

}
