package com.data.rsync.common.feign;

import com.data.rsync.common.model.DataSource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 数据源服务Feign客户端
 */
@FeignClient(value = "data-rsync-data-source", fallback = DataSourceFeignFallback.class)
public interface DataSourceFeignClient {

    /**
     * 获取数据源列表
     * @return 数据源列表
     */
    @GetMapping("/api/data-source/list")
    List<DataSource> getDataSourceList();

    /**
     * 根据ID获取数据源
     * @param id 数据源ID
     * @return 数据源
     */
    @GetMapping("/api/data-source/{id}")
    DataSource getDataSourceById(@PathVariable("id") Long id);

    /**
     * 创建数据源
     * @param dataSource 数据源
     * @return 创建结果
     */
    @PostMapping("/api/data-source/create")
    boolean createDataSource(@RequestBody DataSource dataSource);

    /**
     * 测试数据源连接
     * @param dataSource 数据源
     * @return 连接结果
     */
    @PostMapping("/api/data-source/test-connection")
    boolean testDataSourceConnection(@RequestBody DataSource dataSource);
}
