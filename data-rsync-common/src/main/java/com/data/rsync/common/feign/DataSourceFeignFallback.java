package com.data.rsync.common.feign;

import com.data.rsync.common.model.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 数据源服务Feign客户端降级实现
 */
@Component
@Slf4j
public class DataSourceFeignFallback implements DataSourceFeignClient {

    @Override
    public List<DataSource> getDataSourceList() {
        log.warn("DataSource service fallback: getDataSourceList");
        return Collections.emptyList();
    }

    @Override
    public DataSource getDataSourceById(Long id) {
        log.warn("DataSource service fallback: getDataSourceById, id={}", id);
        return null;
    }

    @Override
    public boolean createDataSource(DataSource dataSource) {
        log.warn("DataSource service fallback: createDataSource");
        return false;
    }

    @Override
    public boolean testDataSourceConnection(DataSource dataSource) {
        log.warn("DataSource service fallback: testDataSourceConnection");
        return false;
    }
}
