package com.data.rsync.admin.controller;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Response;
import com.data.rsync.datasource.service.DataSourceService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据源控制器
 */
@RestController
@RequestMapping("/data-source")
public class DataSourceController {

    @Resource
    private DataSourceService dataSourceService;

    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    @GetMapping("/list")
    public Response list() {
        List<DataSource> dataSources = dataSourceService.getAllDataSources();
        return Response.success("获取成功", dataSources);
    }

    /**
     * 获取数据源详情
     * @param id 数据源ID
     * @return 数据源详情
     */
    @GetMapping("/detail/{id}")
    public Response detail(@PathVariable Long id) {
        DataSource dataSource = dataSourceService.getDataSource(id);
        return Response.success("获取成功", dataSource);
    }

    /**
     * 创建数据源
     * @param dataSource 数据源信息
     * @return 创建结果
     */
    @PostMapping("/create")
    public Response create(@RequestBody DataSource dataSource) {
        DataSource createdDataSource = dataSourceService.createDataSource(dataSource);
        return Response.success("创建成功", createdDataSource);
    }

    /**
     * 更新数据源
     * @param id 数据源ID
     * @param dataSource 数据源信息
     * @return 更新结果
     */
    @PutMapping("/update/{id}")
    public Response update(@PathVariable Long id, @RequestBody DataSource dataSource) {
        DataSource updatedDataSource = dataSourceService.updateDataSource(id, dataSource);
        return Response.success("更新成功", updatedDataSource);
    }

    /**
     * 删除数据源
     * @param id 数据源ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Response delete(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return Response.success("删除成功");
    }

    /**
     * 测试数据源连接
     * @param id 数据源ID
     * @return 测试结果
     */
    @PostMapping("/test-connection/{id}")
    public Response testConnection(@PathVariable Long id) {
        boolean connected = dataSourceService.testDataSourceConnection(id);
        if (connected) {
            return Response.success("连接成功");
        } else {
            return Response.failure("连接失败");
        }
    }

}
