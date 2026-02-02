package com.data.rsync.task.manager.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.task.manager.entity.TaskEntity;
import com.data.rsync.task.manager.service.TaskService;
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

    @Autowired
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

}
