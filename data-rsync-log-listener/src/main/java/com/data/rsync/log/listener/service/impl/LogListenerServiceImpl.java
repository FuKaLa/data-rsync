package com.data.rsync.log.listener.service.impl;

import com.data.rsync.common.constants.DataRsyncConstants;
import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Task;
import com.data.rsync.common.utils.DatabaseUtils;
import com.data.rsync.log.listener.service.LogListenerService;
import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 日志监听服务实现类
 */
@Service
@Slf4j
public class LogListenerServiceImpl implements LogListenerService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 监听任务状态缓存
     */
    private final Map<Long, String> listenerStatusMap = new ConcurrentHashMap<>();

    /**
     * Debezium 引擎缓存
     */
    private final Map<Long, EmbeddedEngine> debeziumEngineMap = new ConcurrentHashMap<>();

    /**
     * 执行器服务
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 关闭资源
     */
    public void shutdown() {
        log.info("Shutting down log listener service");
        
        // 1. 停止所有Debezium引擎
        for (Map.Entry<Long, EmbeddedEngine> entry : debeziumEngineMap.entrySet()) {
            Long taskId = entry.getKey();
            EmbeddedEngine engine = entry.getValue();
            try {
                log.info("Stopping Debezium engine for task: {}", taskId);
                engine.stop();
                engine.close();
            } catch (IOException e) {
                log.error("Failed to stop Debezium engine for task {}: {}", taskId, e.getMessage(), e);
            }
        }
        debeziumEngineMap.clear();
        
        // 2. 关闭线程池
        log.info("Shutting down executor service");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Executor service did not terminate in 30 seconds, forcing shutdown");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while shutting down executor service", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 3. 清理状态缓存
        listenerStatusMap.clear();
        
        log.info("Log listener service shut down successfully");
    }

    /**
     * 启动日志监听
     * @param task 任务
     * @return 监听结果
     */
    @Override
    public boolean startLogListener(Task task) {
        log.info("Starting log listener for task: {}", task.getName());
        try {
            // 1. 检查任务状态
            if (listenerStatusMap.containsKey(task.getId()) && "RUNNING".equals(listenerStatusMap.get(task.getId()))) {
                log.warn("Log listener for task {} is already running", task.getId());
                return false;
            }

            // 2. 初始化监听配置
            initListenerConfig(task);

            // 3. 加载断点续传位点
            String breakpoint = getBreakpoint(task.getId());
            if (breakpoint != null && !breakpoint.isEmpty()) {
                log.info("Loaded breakpoint for task {}: {}", task.getId(), breakpoint);
            }

            // 4. 构建 Debezium 配置
            Configuration config = buildDebeziumConfig(task, breakpoint);

            // 5. 创建并启动 Debezium 引擎
            EmbeddedEngine engine = EmbeddedEngine.create()
                    .using(config)
                    .notifying(record -> {
                        try {
                            // 处理变更事件
                            processChangeEvent(record, task);
                            // 保存断点续传位点
                            saveBreakpoint(record, task.getId());
                        } catch (Exception e) {
                            log.error("Failed to process change event: {}", e.getMessage(), e);
                        }
                    })
                    .build();

            // 6. 启动引擎
            executorService.submit(() -> {
                try {
                    engine.run();
                } catch (Exception e) {
                    log.error("Failed to run debezium engine for task {}: {}", task.getId(), e.getMessage(), e);
                    listenerStatusMap.put(task.getId(), "FAILED");
                    redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.LOG_LISTENER_PREFIX + task.getId(), "FAILED");
                }
            });

            // 7. 缓存引擎和状态
            debeziumEngineMap.put(task.getId(), engine);
            listenerStatusMap.put(task.getId(), "RUNNING");
            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.LOG_LISTENER_PREFIX + task.getId(), "RUNNING");

            log.info("Started log listener for task: {}", task.getName());
            return true;
        } catch (Exception e) {
            log.error("Failed to start log listener for task {}: {}", task.getId(), e.getMessage(), e);
            listenerStatusMap.put(task.getId(), "FAILED");
            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.LOG_LISTENER_PREFIX + task.getId(), "FAILED");
            return false;
        }
    }

    /**
     * 停止日志监听
     * @param taskId 任务ID
     * @return 停止结果
     */
    @Override
    public boolean stopLogListener(Long taskId) {
        log.info("Stopping log listener for task: {}", taskId);
        try {
            // 1. 检查任务状态
            if (!listenerStatusMap.containsKey(taskId)) {
                log.warn("Log listener for task {} is not running", taskId);
                return false;
            }

            // 2. 停止 Debezium 引擎
            EmbeddedEngine engine = debeziumEngineMap.get(taskId);
            if (engine != null) {
                try {
                    engine.stop();
                    engine.close();
                } catch (IOException e) {
                    log.error("Failed to stop debezium engine for task {}: {}", taskId, e.getMessage(), e);
                }
                debeziumEngineMap.remove(taskId);
            }

            // 3. 更新监听状态
            listenerStatusMap.remove(taskId);
            redisTemplate.delete(DataRsyncConstants.RedisKey.LOG_LISTENER_PREFIX + taskId);

            log.info("Stopped log listener for task: {}", taskId);
            return true;
        } catch (Exception e) {
            log.error("Failed to stop log listener for task {}: {}", taskId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取监听状态
     * @param taskId 任务ID
     * @return 监听状态
     */
    @Override
    public String getListenerStatus(Long taskId) {
        log.info("Getting listener status for task: {}", taskId);
        try {
            // 1. 从缓存获取状态
            String status = listenerStatusMap.get(taskId);
            if (status != null) {
                return status;
            }

            // 2. 从 Redis 获取状态
            status = redisTemplate.opsForValue().get(DataRsyncConstants.RedisKey.LOG_LISTENER_PREFIX + taskId);
            if (status != null) {
                listenerStatusMap.put(taskId, status);
                return status;
            }

            return "STOPPED";
        } catch (Exception e) {
            log.error("Failed to get listener status for task {}: {}", taskId, e.getMessage(), e);
            return "UNKNOWN";
        }
    }

    /**
     * 执行全量扫描
     * @param task 任务
     * @return 扫描结果
     */
    @Override
    public boolean executeFullScan(Task task) {
        log.info("Executing full scan for task: {}", task.getName());
        try {
            // 1. 更新任务状态
            task.setStatus("RUNNING");
            task.setStartTime(LocalDateTime.now());
            task.setProgress(0);

            // 2. 计算分片数
            int shardCount = calculateShardCount(task);
            log.info("Calculated shard count for task {}: {}", task.getId(), shardCount);

            // 3. 创建分片扫描任务
            List<Map<String, Object>> shards = createShards(task, shardCount);

            // 4. 并行执行分片扫描
            CountDownLatch latch = new CountDownLatch(shards.size());
            int[] progress = {0};
            final int totalShards = shards.size();

            for (int i = 0; i < shards.size(); i++) {
                final int shardIndex = i;
                final Map<String, Object> shard = shards.get(i);
                executorService.submit(() -> {
                    try {
                        // 执行分片扫描
                        executeShardScan(task, shard, shardIndex);
                        // 更新进度
                        synchronized (progress) {
                            progress[0]++;
                            int currentProgress = (progress[0] * 100) / totalShards;
                            task.setProgress(currentProgress);
                            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.TASK_PROGRESS_PREFIX + task.getId(), String.valueOf(currentProgress));
                        }
                    } catch (Exception e) {
                        log.error("Failed to execute shard scan for task {} shard {}: {}", task.getId(), shardIndex, e.getMessage(), e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 5. 等待所有分片扫描完成
            latch.await();

            // 6. 更新任务状态
            task.setStatus("SUCCESS");
            task.setEndTime(LocalDateTime.now());
            task.setProgress(100);
            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.TASK_PROGRESS_PREFIX + task.getId(), "100");

            log.info("Executed full scan for task: {}", task.getName());
            return true;
        } catch (Exception e) {
            log.error("Failed to execute full scan for task {}: {}", task.getId(), e.getMessage(), e);
            task.setStatus("FAILED");
            task.setEndTime(LocalDateTime.now());
            task.setErrorMessage(e.getMessage());
            return false;
        }
    }

    /**
     * 获取断点续传位点
     * @param taskId 任务ID
     * @return 断点续传位点
     */
    @Override
    public String getBreakpoint(Long taskId) {
        log.info("Getting breakpoint for task: {}", taskId);
        try {
            String breakpoint = redisTemplate.opsForValue().get(DataRsyncConstants.RedisKey.BREAKPOINT_PREFIX + taskId);
            log.info("Got breakpoint for task {}: {}", taskId, breakpoint);
            return breakpoint;
        } catch (Exception e) {
            log.error("Failed to get breakpoint for task {}: {}", taskId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 设置断点续传位点
     * @param taskId 任务ID
     * @param breakpoint 断点续传位点
     */
    @Override
    public void setBreakpoint(Long taskId, String breakpoint) {
        log.info("Setting breakpoint for task {}: {}", taskId, breakpoint);
        try {
            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.BREAKPOINT_PREFIX + taskId, breakpoint);
            log.info("Set breakpoint for task {}: {}", taskId, breakpoint);
        } catch (Exception e) {
            log.error("Failed to set breakpoint for task {}: {}", taskId, e.getMessage(), e);
        }
    }

    /**
     * 清理断点续传位点
     * @param taskId 任务ID
     */
    @Override
    public void clearBreakpoint(Long taskId) {
        log.info("Clearing breakpoint for task: {}", taskId);
        try {
            redisTemplate.delete(DataRsyncConstants.RedisKey.BREAKPOINT_PREFIX + taskId);
            log.info("Cleared breakpoint for task: {}", taskId);
        } catch (Exception e) {
            log.error("Failed to clear breakpoint for task {}: {}", taskId, e.getMessage(), e);
        }
    }

    /**
     * 检查监听健康状态
     * @param taskId 任务ID
     * @return 健康状态
     */
    @Override
    public String checkListenerHealth(Long taskId) {
        log.info("Checking listener health for task: {}", taskId);
        try {
            // 1. 获取监听状态
            String status = getListenerStatus(taskId);

            // 2. 检查 Debezium 引擎状态
            boolean engineExists = debeziumEngineMap.containsKey(taskId);

            // 3. 返回健康状态
            if ("RUNNING".equals(status) && engineExists) {
                return "HEALTHY";
            } else if ("STOPPED".equals(status)) {
                return "STOPPED";
            } else {
                return "UNHEALTHY";
            }
        } catch (Exception e) {
            log.error("Failed to check listener health for task {}: {}", taskId, e.getMessage(), e);
            return "UNKNOWN";
        }
    }

    /**
     * 初始化监听配置
     * @param task 任务
     */
    private void initListenerConfig(Task task) {
        log.info("Initialized listener config for task: {}", task.getName());
        // 可以在这里初始化额外的监听配置
    }

    /**
     * 构建 Debezium 配置
     * @param task 任务
     * @param breakpoint 断点续传位点
     * @return Debezium 配置
     */
    private Configuration buildDebeziumConfig(Task task, String breakpoint) {
        // 根据不同的数据源类型构建不同的 Debezium 配置
        String dataSourceType = task.getDataSourceType();
        Configuration.Builder builder = Configuration.create()
                .with("name", "task-" + task.getId())
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", "/tmp/debezium/offsets/task-" + task.getId() + ".offset")
                .with("offset.flush.interval.ms", "60000")
                .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename", "/tmp/debezium/history/task-" + task.getId() + ".history");

        // 从数据源配置中获取连接信息
        DataSource dataSource = task.getDataSource();
        if (dataSource != null) {
            builder.with("database.hostname", dataSource.getHost())
                    .with("database.port", String.valueOf(dataSource.getPort()))
                    .with("database.user", dataSource.getUsername())
                    .with("database.password", dataSource.getPassword());
        }

        // 根据数据源类型构建特定配置
        switch (dataSourceType) {
            case DataRsyncConstants.DataSourceType.MYSQL:
                builder.with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                        .with("database.server.name", "mysql-server")
                        .with("database.include.list", task.getDatabaseName())
                        .with("table.include.list", task.getTableName());
                break;
            case DataRsyncConstants.DataSourceType.POSTGRESQL:
                builder.with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
                        .with("database.server.name", "postgres-server")
                        .with("database.dbname", task.getDatabaseName())
                        .with("table.include.list", task.getTableName());
                break;
            case DataRsyncConstants.DataSourceType.ORACLE:
                builder.with("connector.class", "io.debezium.connector.oracle.OracleConnector")
                        .with("database.server.name", "oracle-server")
                        .with("database.dbname", task.getDatabaseName())
                        .with("table.include.list", task.getTableName());
                break;
            case DataRsyncConstants.DataSourceType.SQL_SERVER:
                builder.with("connector.class", "io.debezium.connector.sqlserver.SqlServerConnector")
                        .with("database.server.name", "sqlserver-server")
                        .with("database.dbname", task.getDatabaseName())
                        .with("table.include.list", task.getTableName());
                break;
            default:
                throw new IllegalArgumentException("Unsupported data source type: " + dataSourceType);
        }

        return builder.build();
    }

    /**
     * 处理变更事件
     * @param record 变更事件
     * @param task 任务
     */
    private void processChangeEvent(SourceRecord record, Task task) {
        try {
            // 1. 解析变更事件
            String key = record.key().toString();
            Struct value = (Struct) record.value();
            Struct source = value.getStruct("source");
            String op = value.getString("op");
            Struct after = value.getStruct("after");
            Struct before = value.getStruct("before");

            // 2. 构建消息
            Map<String, Object> message = new HashMap<>();
            message.put("taskId", task.getId());
            message.put("op", op);
            message.put("key", key);
            message.put("source", source);
            message.put("after", after);
            message.put("before", before);
            message.put("timestamp", System.currentTimeMillis());

            // 3. 发送消息到 Kafka
            String topic = DataRsyncConstants.KafkaTopic.DATA_CHANGE_TOPIC;
            kafkaTemplate.send(topic, key, message.toString());
            log.debug("Sent change event to Kafka topic {} for task {}: {}", topic, task.getId(), op);
        } catch (Exception e) {
            log.error("Failed to process change event for task {}: {}", task.getId(), e.getMessage(), e);
        }
    }

    /**
     * 保存断点续传位点
     * @param record 变更事件
     * @param taskId 任务ID
     */
    private void saveBreakpoint(SourceRecord record, Long taskId) {
        try {
            // 从 SourceRecord 中提取位点信息
            Map<String, ?> sourcePartition = record.sourcePartition();
            Map<String, ?> sourceOffset = record.sourceOffset();
            String breakpoint = sourcePartition.toString() + ":" + sourceOffset.toString();
            setBreakpoint(taskId, breakpoint);
        } catch (Exception e) {
            log.error("Failed to save breakpoint for task {}: {}", taskId, e.getMessage(), e);
        }
    }



    /**
     * 计算分片数
     * @param task 任务
     * @return 分片数
     */
    private int calculateShardCount(Task task) {
        // 根据表大小和任务配置计算分片数
        // 这里简化处理，返回固定值
        return Math.min(task.getConcurrency(), 10);
    }

    /**
     * 创建分片
     * @param task 任务
     * @param shardCount 分片数
     * @return 分片列表
     */
    private List<Map<String, Object>> createShards(Task task, int shardCount) {
        List<Map<String, Object>> shards = new ArrayList<>();
        // 根据主键范围创建分片
        for (int i = 0; i < shardCount; i++) {
            Map<String, Object> shard = new HashMap<>();
            shard.put("shardIndex", i);
            shard.put("startId", i * (1000000 / shardCount));
            shard.put("endId", (i + 1) * (1000000 / shardCount) - 1);
            shards.add(shard);
        }
        return shards;
    }

    /**
     * 执行分片扫描
     * @param task 任务
     * @param shard 分片信息
     * @param shardIndex 分片索引
     */
    private void executeShardScan(Task task, Map<String, Object> shard, int shardIndex) {
        log.info("Executing shard scan for task {} shard {}: startId={}, endId={}", 
                task.getId(), shardIndex, shard.get("startId"), shard.get("endId"));
        try {
            // 1. 连接数据库
            DataSource dataSource = task.getDataSource();
            String jdbcUrl = DatabaseUtils.buildJdbcUrl(dataSource);
            java.sql.Connection connection = null;
            java.sql.Statement statement = null;
            java.sql.ResultSet resultSet = null;

            try {
                // 加载驱动
                Class.forName(DatabaseUtils.getDriverClassName(dataSource.getType()));
                // 建立连接
                connection = java.sql.DriverManager.getConnection(jdbcUrl, dataSource.getUsername(), dataSource.getPassword());
                
                // 2. 执行分片查询
                String query = buildShardQuery(task, shard);
                log.debug("Executing shard query for task {} shard {}: {}", task.getId(), shardIndex, query);
                
                statement = connection.createStatement();
                resultSet = statement.executeQuery(query);
                
                // 3. 处理查询结果
                int batchSize = 1000;
                List<Map<String, Object>> batchData = new ArrayList<>(batchSize);
                int count = 0;
                
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>();
                    java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = resultSet.getObject(i);
                        row.put(columnName, value);
                    }
                    
                    batchData.add(row);
                    count++;
                    
                    // 4. 批量发送数据到 Kafka
                    if (batchData.size() >= batchSize) {
                        sendBatchToKafka(task, batchData, shardIndex);
                        batchData.clear();
                    }
                }
                
                // 发送剩余数据
                if (!batchData.isEmpty()) {
                    sendBatchToKafka(task, batchData, shardIndex);
                }
                
                log.info("Processed {} records for task {} shard {}", count, task.getId(), shardIndex);
            } finally {
                // 关闭资源
                DatabaseUtils.closeResources(connection, statement, resultSet);
            }
        } catch (Exception e) {
            log.error("Failed to execute shard scan for task {} shard {}: {}", task.getId(), shardIndex, e.getMessage(), e);
        }
    }

    /**
     * 构建分片查询语句
     * @param task 任务
     * @param shard 分片信息
     * @return 查询语句
     */
    private String buildShardQuery(Task task, Map<String, Object> shard) {
        String tableName = task.getTableName();
        String primaryKey = task.getPrimaryKey();
        long startId = (long) shard.get("startId");
        long endId = (long) shard.get("endId");
        
        return DatabaseUtils.buildShardQuery(tableName, primaryKey, startId, endId);
    }

    /**
     * 批量发送数据到 Kafka
     * @param task 任务
     * @param batchData 批量数据
     * @param shardIndex 分片索引
     */
    private void sendBatchToKafka(Task task, List<Map<String, Object>> batchData, int shardIndex) {
        try {
            String topic = DataRsyncConstants.KafkaTopic.DATA_FULL_SYNC_TOPIC;
            for (Map<String, Object> row : batchData) {
                // 构建消息
                Map<String, Object> message = new HashMap<>();
                message.put("taskId", task.getId());
                message.put("op", "READ");
                message.put("data", row);
                message.put("shardIndex", shardIndex);
                message.put("timestamp", System.currentTimeMillis());
                
                // 发送消息
                String key = task.getId() + "-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
                kafkaTemplate.send(topic, key, message.toString());
            }
            log.debug("Sent {} records to Kafka for task {} shard {}", batchData.size(), task.getId(), shardIndex);
        } catch (Exception e) {
            log.error("Failed to send batch data to Kafka for task {} shard {}: {}", task.getId(), shardIndex, e.getMessage(), e);
        }
    }

}

