package com.data.rsync.common.feign;

import com.data.rsync.common.model.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Milvus同步服务Feign客户端
 */
@FeignClient(value = "data-rsync-milvus-sync", fallback = MilvusSyncFeignFallback.class)
public interface MilvusSyncFeignClient {

    /**
     * 写入数据到Milvus
     * @param taskId 任务ID
     * @param data 数据
     * @return 写入结果
     */
    @PostMapping("/api/milvus-sync/write")
    boolean writeDataToMilvus(@RequestParam("taskId") Long taskId, @RequestBody Map<String, Object> data);

    /**
     * 批量写入数据到Milvus
     * @param taskId 任务ID
     * @param dataList 数据列表
     * @return 写入结果
     */
    @PostMapping("/api/milvus-sync/batch-write")
    boolean batchWriteDataToMilvus(@RequestParam("taskId") Long taskId, @RequestBody List<Map<String, Object>> dataList);

    /**
     * 从Milvus删除数据
     * @param taskId 任务ID
     * @param primaryKey 主键
     * @return 删除结果
     */
    @PostMapping("/api/milvus-sync/delete")
    boolean deleteDataFromMilvus(@RequestParam("taskId") Long taskId, @RequestParam("primaryKey") Object primaryKey);

    /**
     * 创建Milvus集合
     * @param task 任务
     * @return 创建结果
     */
    @PostMapping("/api/milvus-sync/create-collection")
    boolean createMilvusCollection(@RequestBody Task task);

    /**
     * 创建Milvus索引
     * @param task 任务
     * @return 创建结果
     */
    @PostMapping("/api/milvus-sync/create-index")
    boolean createMilvusIndex(@RequestBody Task task);

    /**
     * 执行数据一致性校验
     * @param task 任务
     * @param sourceCount 源数据数量
     * @param sampleData 样本数据
     * @return 校验结果
     */
    @PostMapping("/api/milvus-sync/check-consistency")
    Map<String, Object> checkDataConsistency(@RequestBody Task task, @RequestParam("sourceCount") long sourceCount, @RequestBody List<Map<String, Object>> sampleData);

    /**
     * 获取同步状态
     * @param taskId 任务ID
     * @return 同步状态
     */
    @GetMapping("/api/milvus-sync/status/{taskId}")
    String getSyncStatus(@PathVariable("taskId") Long taskId);

    /**
     * 检查Milvus连接
     * @return 连接状态
     */
    @GetMapping("/api/milvus-sync/check-connection")
    boolean checkMilvusConnection();
}
