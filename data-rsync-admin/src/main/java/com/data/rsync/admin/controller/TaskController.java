package com.data.rsync.admin.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.task.manager.entity.TaskEntity;
import com.data.rsync.task.manager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 获取所有任务
     * @return 任务列表
     */
    @GetMapping("/list")
    public Response list() {
        List<TaskEntity> tasks = taskService.getAllTasks();
        return Response.success("获取成功", tasks);
    }

    /**
     * 获取任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    @GetMapping("/detail/{id}")
    public Response detail(@PathVariable Long id) {
        TaskEntity task = taskService.getTaskById(id);
        return Response.success("获取成功", task);
    }

    /**
     * 创建任务
     * @param task 任务信息
     * @return 创建结果
     */
    @PostMapping("/create")
    public Response create(@RequestBody @Valid TaskEntity task) {
        TaskEntity createdTask = taskService.createTask(task);
        return Response.success("创建成功", createdTask);
    }

    /**
     * 更新任务
     * @param task 任务信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public Response update(@RequestBody @Valid TaskEntity task) {
        TaskEntity updatedTask = taskService.updateTask(task);
        return Response.success("更新成功", updatedTask);
    }

    /**
     * 删除任务
     * @param id 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Response delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Response.success("删除成功");
    }

    /**
     * 启动任务
     * @param id 任务ID
     * @return 启动结果
     */
    @PostMapping("/start/{id}")
    public Response start(@PathVariable Long id) {
        Map<String, Object> result = taskService.triggerTask(id);
        return Response.success("启动成功", result);
    }

    /**
     * 暂停任务
     * @param id 任务ID
     * @return 暂停结果
     */
    @PostMapping("/pause/{id}")
    public Response pause(@PathVariable Long id) {
        boolean result = taskService.pauseTask(id);
        return Response.success("暂停成功", result);
    }

    /**
     * 继续任务
     * @param id 任务ID
     * @return 继续结果
     */
    @PostMapping("/resume/{id}")
    public Response resume(@PathVariable Long id) {
        boolean result = taskService.resumeTask(id);
        return Response.success("继续成功", result);
    }

    /**
     * 回滚任务
     * @param id 任务ID
     * @param rollbackPoint 回滚点
     * @return 回滚结果
     */
    @PostMapping("/rollback/{id}")
    public Response rollback(@PathVariable Long id, @RequestParam String rollbackPoint) {
        boolean result = taskService.rollbackTask(id, rollbackPoint);
        return Response.success("回滚成功", result);
    }

    /**
     * 获取任务版本历史
     * @param id 任务ID
     * @return 版本历史
     */
    @GetMapping("/versions/{id}")
    public Response versions(@PathVariable Long id) {
        List<Map<String, Object>> versions = taskService.getTaskVersions(id);
        return Response.success("获取成功", versions);
    }

}
