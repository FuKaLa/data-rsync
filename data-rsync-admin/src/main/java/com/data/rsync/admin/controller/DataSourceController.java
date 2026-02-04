package com.data.rsync.admin.controller;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Response;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源控制器
 */
@RestController
@RequestMapping("/data-source")
public class DataSourceController {

    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    @GetMapping("/list")
    public Response list() {
        // 这里应该通过Feign客户端调用data-rsync-data-source模块的服务
        return Response.success("获取成功", null);
    }

    /**
     * 获取数据源详情
     * @param id 数据源ID
     * @return 数据源详情
     */
    @GetMapping("/detail/{id}")
    public Response detail(@PathVariable Long id) {
        // 这里应该通过Feign客户端调用data-rsync-data-source模块的服务
        return Response.success("获取成功", null);
    }

    /**
     * 创建数据源
     * @param dataSource 数据源信息
     * @return 创建结果
     */
    @PostMapping("/create")
    public Response create(@RequestBody DataSource dataSource) {
        // 这里应该通过Feign客户端调用data-rsync-data-source模块的服务
        return Response.success("创建成功", null);
    }

    /**
     * 更新数据源
     * @param id 数据源ID
     * @param dataSource 数据源信息
     * @return 更新结果
     */
    @PutMapping("/update/{id}")
    public Response update(@PathVariable Long id, @RequestBody DataSource dataSource) {
        // 这里应该通过Feign客户端调用data-rsync-data-source模块的服务
        return Response.success("更新成功", null);
    }

    /**
     * 删除数据源
     * @param id 数据源ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Response delete(@PathVariable Long id) {
        // 这里应该通过Feign客户端调用data-rsync-data-source模块的服务
        return Response.success("删除成功");
    }

    /**
     * 测试数据源连接
     * @param id 数据源ID
     * @return 测试结果
     */
    @PostMapping("/test-connection/{id}")
    public Response testConnection(@PathVariable Long id) {
        // 这里应该通过Feign客户端调用data-rsync-data-source模块的服务
        return Response.success("连接成功");
    }

}
