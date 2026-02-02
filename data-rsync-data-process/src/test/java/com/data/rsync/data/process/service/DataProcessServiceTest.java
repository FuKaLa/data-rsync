package com.data.rsync.data.process.service;

import com.data.rsync.common.model.Task;
import com.data.rsync.data.process.service.impl.DataProcessServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataProcessServiceTest {

    @InjectMocks
    private DataProcessServiceImpl dataProcessService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

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

        // 模拟Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testProcessDataChange() {
        // 测试处理数据变更
        boolean result = dataProcessService.processDataChange(1L, data);
        assertTrue(result);
        // 验证Kafka发送操作
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), anyString(), anyString());
    }

    @Test
    void testExecuteDataTransform() {
        // 测试数据转换
        Map<String, Object> transformedData = dataProcessService.executeDataTransform(task, data);
        assertNotNull(transformedData);
        assertEquals(data.get("id"), transformedData.get("id"));
        assertEquals(data.get("name"), transformedData.get("name"));
        assertEquals(data.get("description"), transformedData.get("description"));
    }

    @Test
    void testGenerateVector() {
        // 测试生成向量
        float[] vector = dataProcessService.generateVector(task, data);
        assertNotNull(vector);
        assertTrue(vector.length > 0);
    }

    @Test
    void testExecuteDataCleaning() {
        // 测试数据清洗
        Map<String, Object> cleanedData = dataProcessService.executeDataCleaning(task, data);
        assertNotNull(cleanedData);
        assertEquals(data.get("id"), cleanedData.get("id"));
        assertEquals(data.get("name"), cleanedData.get("name"));
        assertEquals(data.get("description"), cleanedData.get("description"));
    }

    @Test
    void testBatchProcessData() {
        // 测试批量处理数据
        List<Map<String, Object>> dataList = List.of(data);
        boolean result = dataProcessService.batchProcessData(1L, dataList);
        assertTrue(result);
        // 验证Kafka发送操作
        verify(kafkaTemplate, atLeastOnce()).send(anyString(), anyString(), anyString());
    }

    @Test
    void testGetProcessStatus() {
        // 测试获取处理状态
        String status = dataProcessService.getProcessStatus(1L);
        assertNotNull(status);
        assertEquals("IDLE", status);
    }

    @Test
    void testCheckProcessHealth() {
        // 测试检查处理健康状态
        String healthStatus = dataProcessService.checkProcessHealth(1L);
        assertNotNull(healthStatus);
        assertEquals("IDLE", healthStatus);
    }
}
