package com.data.rsync.common.constants;

/**
 * 数据同步系统常量类
 */
public class DataRsyncConstants {

    /**
     * 系统常量
     */
    public static class System {
        public static final String SYSTEM_NAME = "Data Rsync System";
        public static final String SYSTEM_VERSION = "1.0.0";
        public static final String DEFAULT_CHARSET = "UTF-8";
    }

    /**
     * 数据源类型
     */
    public static class DataSourceType {
        public static final String MYSQL = "MYSQL";
        public static final String POSTGRESQL = "POSTGRESQL";
        public static final String ORACLE = "ORACLE";
        public static final String SQL_SERVER = "SQL_SERVER";
        public static final String MONGODB = "MONGODB";
        public static final String REDIS = "REDIS";
    }

    /**
     * 任务类型
     */
    public static class TaskType {
        public static final String FULL = "FULL";
        public static final String INCREMENTAL = "INCREMENTAL";
        public static final String FULL_AND_INCREMENTAL = "FULL_AND_INCREMENTAL";
    }

    /**
     * 任务状态
     */
    public static class TaskStatus {
        public static final String PENDING = "PENDING";
        public static final String RUNNING = "RUNNING";
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        public static final String PAUSED = "PAUSED";
    }

    /**
     * 操作类型
     */
    public static class OperationType {
        public static final String INSERT = "INSERT";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
    }

    /**
     * 错误码
     */
    public static class ErrorCode {
        public static final int SUCCESS = 200;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int SERVICE_UNAVAILABLE = 503;

        // 业务错误码
        public static final int DATA_SOURCE_ERROR = 1001;
        public static final int LOG_LISTENER_ERROR = 1002;
        public static final int DATA_PROCESS_ERROR = 1003;
        public static final int MILVUS_SYNC_ERROR = 1004;
        public static final int TASK_MANAGER_ERROR = 1005;
        public static final int MONITOR_ERROR = 1006;
    }

    /**
     * 配置键
     */
    public static class ConfigKey {
        public static final String NACOS_SERVER_ADDR = "spring.cloud.nacos.discovery.server-addr";
        public static final String NACOS_NAMESPACE = "spring.cloud.nacos.discovery.namespace";
        public static final String REDIS_HOST = "spring.redis.host";
        public static final String REDIS_PORT = "spring.redis.port";
        public static final String KAFKA_BOOTSTRAP_SERVERS = "spring.kafka.bootstrap-servers";
        public static final String MILVUS_HOST = "milvus.host";
        public static final String MILVUS_PORT = "milvus.port";
        public static final String MILVUS_TOKEN = "milvus.token";
        public static final String MILVUS_NAMESPACE = "milvus.namespace";
    }

    /**
     * Redis 键前缀
     */
    public static class RedisKey {
        public static final String DATA_SOURCE_PREFIX = "data_source:";
        public static final String TASK_PREFIX = "task:";
        public static final String TASK_CONFIG_PREFIX = "task_config:";
        public static final String TASK_PROGRESS_PREFIX = "task_progress:";
        public static final String LOG_LISTENER_PREFIX = "log_listener:";
        public static final String DATA_PROCESS_PREFIX = "data_process:";
        public static final String MILVUS_SYNC_PREFIX = "milvus_sync:";
        public static final String MONITOR_PREFIX = "monitor:";
        public static final String BREAKPOINT_PREFIX = "breakpoint:";
        public static final String HEARTBEAT_PREFIX = "heartbeat:";
        public static final String PROCESSED_RECORD_PREFIX = "processed_record:";
    }

    /**
     * Kafka 主题
     */
    public static class KafkaTopic {
        public static final String DATA_CHANGE_TOPIC = "data_change_topic";
        public static final String DATA_FULL_SYNC_TOPIC = "data_full_sync_topic";
        public static final String DATA_PROCESSED_TOPIC = "data_processed_topic";
        public static final String MILVUS_SYNC_TOPIC = "milvus_sync_topic";
        public static final String ERROR_TOPIC = "error_topic";
    }

}
