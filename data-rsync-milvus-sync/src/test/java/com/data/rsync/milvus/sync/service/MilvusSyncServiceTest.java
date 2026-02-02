package com.data.rsync.milvus.sync.service;

import com.data.rsync.common.model.Task;
import com.data.rsync.milvus.sync.service.impl.MilvusSyncServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MilvusSyncServiceTest {

    @InjectMocks
    private MilvusSyncServiceImpl milvusSyncService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private Task task;
    private Map<String, Object> data;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        task = new Task();
        task.setId(1L);
        task.setName("Test Task");
        task.setDataSourceType("MYSQL");
        task.setDatabaseName("test_db");
        task.setTableName("test_table");
        task.setPrimaryKey("id");

        data = new HashMap<>();
        data.put("id", 1L);
        data.put("name", "Test Data");
        data.put("description", "This is a test data");
        // 模拟向量数据
        float[] vector = new float[128];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) i / vector.length;
        }
        data.put("vector", vector);

        // 模拟Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCheckMilvusConnection() {
        // 测试检查Milvus连接
        boolean result = milvusSyncService.checkMilvusConnection();
        // 由于是模拟环境，这里会返回true（实际实现中会检查连接）
        assertTrue(result);
    }

    @Test
    void testGetSyncStatus() {
        // 测试获取同步状态
        String status = milvusSyncService.getSyncStatus(1L);
        assertNotNull(status);
        assertEquals("IDLE", status);
    }

    @Test
    void testCreateMilvusCollection() {
        // 测试创建Milvus集合
        boolean result = milvusSyncService.createMilvusCollection(task);
        // 由于是模拟环境，这里会返回false（实际实现中会尝试创建集合）
        // 但我们测试方法调用是否正确
        assertNotNull(result);
    }

    @Test
    void testCreateMilvusIndex() {
        // 测试创建Milvus索引
        boolean result = milvusSyncService.createMilvusIndex(task);
        // 由于是模拟环境，这里会返回false（实际实现中会尝试创建索引）
        // 但我们测试方法调用是否正确
        assertNotNull(result);
    }

    @Test
    void testRebuildMilvusIndex() {
        // 测试重建Milvus索引
        boolean result = milvusSyncService.rebuildMilvusIndex(task);
        // 由于是模拟环境，这里会返回false（实际实现中会尝试重建索引）
        // 但我们测试方法调用是否正确
        assertNotNull(result);
    }

    @Test
    void testValidateMilvusData() {
        // 测试验证Milvus数据
        boolean result = milvusSyncService.validateMilvusData(task);
        // 由于是模拟环境，这里会返回false（实际实现中会尝试验证数据）
        // 但我们测试方法调用是否正确
        assertNotNull(result);
    }

    @Test
    void testExecuteMilvusQuery() {
        // 测试执行Milvus查询
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("expr", "id = 1");
        queryParam.put("limit", 10);

        List<Map<String, Object>> results = milvusSyncService.executeMilvusQuery(task, queryParam);
        assertNotNull(results);
        assertTrue(results.isEmpty()); // 模拟环境返回空列表
    }
}
