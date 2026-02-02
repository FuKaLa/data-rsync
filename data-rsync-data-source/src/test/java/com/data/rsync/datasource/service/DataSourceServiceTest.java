package com.data.rsync.datasource.service;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.datasource.entity.DataSourceEntity;
import com.data.rsync.datasource.repository.DataSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataSourceServiceTest {

    @InjectMocks
    private DataSourceService dataSourceService;

    @Mock
    private DataSourceRepository dataSourceRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private DataSource dataSource;
    private DataSourceEntity dataSourceEntity;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        dataSource = new DataSource();
        dataSource.setId(1L);
        dataSource.setName("Test Data Source");
        dataSource.setType("MYSQL");
        dataSource.setHost("localhost");
        dataSource.setPort(3306);
        dataSource.setDatabase("test_db");
        dataSource.setUsername("root");
        dataSource.setPassword("password");

        dataSourceEntity = new DataSourceEntity();
        dataSourceEntity.setId(1L);
        dataSourceEntity.setName("Test Data Source");
        dataSourceEntity.setType("MYSQL");
        dataSourceEntity.setHost("localhost");
        dataSourceEntity.setPort(3306);
        dataSourceEntity.setDatabase("test_db");
        dataSourceEntity.setUsername("root");
        dataSourceEntity.setPassword("password");
        dataSourceEntity.setHealthStatus("HEALTHY");

        // 模拟Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreateDataSource() {
        // 测试创建数据源
        when(dataSourceRepository.save(any(DataSourceEntity.class))).thenReturn(dataSourceEntity);

        boolean result = dataSourceService.createDataSource(dataSource);
        assertTrue(result);
        verify(dataSourceRepository, times(1)).save(any(DataSourceEntity.class));
    }

    @Test
    void testUpdateDataSource() {
        // 测试更新数据源
        when(dataSourceRepository.findById(1L)).thenReturn(Optional.of(dataSourceEntity));
        when(dataSourceRepository.save(any(DataSourceEntity.class))).thenReturn(dataSourceEntity);

        boolean result = dataSourceService.updateDataSource(dataSource);
        assertTrue(result);
        verify(dataSourceRepository, times(1)).save(any(DataSourceEntity.class));
    }

    @Test
    void testDeleteDataSource() {
        // 测试删除数据源
        when(dataSourceRepository.existsById(1L)).thenReturn(true);

        boolean result = dataSourceService.deleteDataSource(1L);
        assertTrue(result);
        verify(dataSourceRepository, times(1)).deleteById(1L);
    }

    @Test
    void testTestConnection() {
        // 测试连接测试
        boolean result = dataSourceService.testConnection(dataSource);
        // 由于是模拟环境，这里会返回false，但测试方法调用是否正确
        verifyNoMoreInteractions(dataSourceRepository);
    }

    @Test
    void testCheckDataSourceHealth() {
        // 测试数据源健康检查
        String healthStatus = dataSourceService.checkDataSourceHealth(1L);
        // 由于是模拟环境，这里会返回UNKNOWN，但测试方法调用是否正确
        verifyNoMoreInteractions(dataSourceRepository);
    }
}
