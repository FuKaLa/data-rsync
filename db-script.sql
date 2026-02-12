-- 数据库脚本
-- 生成时间: 2026-02-10
-- 企业级数据同步系统表结构

-- 删除现有表（按依赖关系顺序）
DROP TABLE IF EXISTS task_performance_monitor;
DROP TABLE IF EXISTS task_execution_log;
DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS task_error_data;
DROP TABLE IF EXISTS vectorization_config;
DROP TABLE IF EXISTS task_dependency;
DROP TABLE IF EXISTS task_connection;
DROP TABLE IF EXISTS task_node;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS data_source_diagnose_report;
DROP TABLE IF EXISTS data_source_template;
DROP TABLE IF EXISTS data_source;
DROP TABLE IF EXISTS milvus_index;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS user;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE, DISABLE',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_tenant_username (tenant_id, username) COMMENT '租户ID和用户名唯一索引',
    UNIQUE KEY uk_tenant_email (tenant_id, email) COMMENT '租户ID和邮箱唯一索引',
    UNIQUE KEY uk_tenant_phone (tenant_id, phone) COMMENT '租户ID和手机号唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    code VARCHAR(100) NOT NULL COMMENT '角色编码',
    description VARCHAR(500) COMMENT '角色描述',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE, DISABLE',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_tenant_name (tenant_id, name) COMMENT '租户ID和角色名称唯一索引',
    UNIQUE KEY uk_tenant_code (tenant_id, code) COMMENT '租户ID和角色编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL COMMENT '权限编码',
    type VARCHAR(20) NOT NULL COMMENT '权限类型：MENU, BUTTON, API, DATA',
    description VARCHAR(500) COMMENT '权限描述',
    resource_path VARCHAR(255) COMMENT '资源路径',
    parent_id BIGINT COMMENT '父权限ID',
    sort INT COMMENT '排序',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE, DISABLE',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_tenant_name (tenant_id, name) COMMENT '租户ID和权限名称唯一索引',
    UNIQUE KEY uk_tenant_code (tenant_id, code) COMMENT '租户ID和权限编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (user_id, role_id) COMMENT '联合主键'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (role_id, permission_id) COMMENT '联合主键'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 数据源表
CREATE TABLE IF NOT EXISTS data_source (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据源ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    data_source_group VARCHAR(100) COMMENT '数据源分组',
    type VARCHAR(50) NOT NULL COMMENT '数据源类型：MYSQL, POSTGRESQL, ORACLE, MONGODB等',
    host VARCHAR(100) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '连接端口',
    database_name VARCHAR(100) COMMENT '数据库名称',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    url VARCHAR(255) NOT NULL COMMENT '连接地址',
    driver_class VARCHAR(255) COMMENT '驱动类名',
    log_monitor_type VARCHAR(50) COMMENT '日志监听类型：BINLOG, WAL等',
    connection_timeout INT COMMENT '连接超时时间（毫秒）',
    connection_pool_config TEXT COMMENT '连接池配置（JSON格式）',
    health_status VARCHAR(50) COMMENT '健康状态',
    last_failure_reason TEXT COMMENT '最近一次失败原因',
    heartbeat_time INT COMMENT '心跳检测时间间隔（秒）',
    last_heartbeat_time DATETIME COMMENT '最近一次心跳时间',
    failure_count INT DEFAULT 0 COMMENT '失败次数',
    description TEXT COMMENT '描述',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_tenant_name (tenant_id, name) COMMENT '租户ID和数据源名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源表';

-- 数据源模板表
CREATE TABLE IF NOT EXISTS data_source_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    data_source_type VARCHAR(50) NOT NULL COMMENT '数据源类型',
    driver_class VARCHAR(255) NOT NULL COMMENT '默认驱动类名',
    log_monitor_type VARCHAR(50) NOT NULL COMMENT '默认日志监听类型',
    default_port INT NOT NULL COMMENT '默认端口',
    connection_timeout INT COMMENT '连接超时时间（秒）',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统预设模板',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_tenant_name_type (tenant_id, name, data_source_type) COMMENT '租户ID、模板名称和类型唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源模板表';

-- 数据源诊断报告表
CREATE TABLE IF NOT EXISTS data_source_diagnose_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    data_source_id BIGINT NOT NULL COMMENT '数据源ID',
    overall_status VARCHAR(50) NOT NULL COMMENT '整体状态：HEALTHY, WARNING, UNHEALTHY',
    network_status VARCHAR(50) NOT NULL COMMENT '网络状态：OK, ERROR',
    authentication_status VARCHAR(50) NOT NULL COMMENT '认证状态：OK, ERROR',
    log_monitor_status VARCHAR(50) NOT NULL COMMENT '日志监听状态：OK, ERROR',
    connection_status VARCHAR(50) NOT NULL COMMENT '连接状态：OK, ERROR',
    diagnose_time DATETIME NOT NULL COMMENT '诊断时间',
    diagnose_duration INT COMMENT '诊断耗时（毫秒）',
    recommendation TEXT COMMENT '诊断建议',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源诊断报告表';

-- 任务表
CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_group VARCHAR(100) COMMENT '任务分组',
    type VARCHAR(50) NOT NULL COMMENT '任务类型：FULL_SYNC, INCREMENTAL_SYNC, FULL_AND_INCREMENTAL',
    sync_strategy VARCHAR(50) COMMENT '同步策略：INSERT_ONLY, UPDATE_IF_EXISTS, UPSERT, DELETE_IF_NOT_EXISTS',
    data_source_id BIGINT NOT NULL COMMENT '数据源ID',
    database_name VARCHAR(100) COMMENT '数据库名称',
    table_name VARCHAR(100) COMMENT '表名',
    status VARCHAR(50) DEFAULT 'PENDING' COMMENT '任务状态：PENDING, RUNNING, SUCCESS, FAILED, STOPPED, PAUSED, ROLLED_BACK',
    config TEXT COMMENT '任务配置（JSON格式）',
    progress INT DEFAULT 0 COMMENT '任务进度（百分比）',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    breakpoint TEXT COMMENT '断点续传位点',
    error_threshold INT DEFAULT 100 COMMENT '错误阈值',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    last_exec_time DATETIME COMMENT '最后执行时间',
    exec_count INT DEFAULT 0 COMMENT '执行次数',
    next_exec_time DATETIME COMMENT '下次执行时间',
    concurrency INT DEFAULT 1 COMMENT '并发度',
    batch_size INT DEFAULT 1000 COMMENT '批量大小',
    retry_count INT DEFAULT 3 COMMENT '重试次数',
    timeout_seconds INT DEFAULT 3600 COMMENT '超时时间（秒）',
    schedule_type VARCHAR(50) COMMENT '调度类型：CRON, FIXED_RATE, FIXED_DELAY',
    schedule_expression VARCHAR(255) COMMENT '调度表达式',
    remark VARCHAR(500) COMMENT '备注',
    pause_time DATETIME COMMENT '暂停时间',
    resume_time DATETIME COMMENT '恢复时间',
    rollback_point VARCHAR(255) COMMENT '回滚点',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_tenant_name (tenant_id, name) COMMENT '租户ID和任务名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 任务节点表
CREATE TABLE IF NOT EXISTS task_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '节点ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    node_type VARCHAR(50) NOT NULL COMMENT '节点类型：dataSource, syncType, dataProcess, vectorization, milvusWrite',
    node_label VARCHAR(100) NOT NULL COMMENT '节点标签',
    node_order INT COMMENT '节点执行顺序',
    node_config TEXT COMMENT '节点配置（JSON格式）',
    position_x INT NOT NULL COMMENT '节点位置X坐标',
    position_y INT NOT NULL COMMENT '节点位置Y坐标',
    status VARCHAR(50) COMMENT '节点状态：READY, PROCESSING, SUCCESS, FAILED',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务节点表';

-- 任务连接表
CREATE TABLE IF NOT EXISTS task_connection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '连接ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    source_node_id BIGINT NOT NULL COMMENT '源节点ID',
    target_node_id BIGINT NOT NULL COMMENT '目标节点ID',
    source_handle_id VARCHAR(100) COMMENT '源节点句柄ID',
    target_handle_id VARCHAR(100) COMMENT '目标节点句柄ID',
    status VARCHAR(50) COMMENT '连接状态',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务连接表';

-- 任务依赖表
CREATE TABLE IF NOT EXISTS task_dependency (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '依赖ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    dependency_task_id BIGINT NOT NULL COMMENT '依赖任务ID',
    dependency_type VARCHAR(50) COMMENT '依赖类型：SUCCESS, COMPLETE, CUSTOM',
    dependency_condition TEXT COMMENT '依赖条件（JSON格式）',
    status VARCHAR(50) COMMENT '依赖状态：PENDING, MET, FAILED',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务依赖表';

-- 任务错误数据表
CREATE TABLE IF NOT EXISTS task_error_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '错误数据ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    source_data TEXT NOT NULL COMMENT '源数据（JSON格式）',
    error_message TEXT NOT NULL COMMENT '错误原因',
    error_type VARCHAR(50) NOT NULL COMMENT '错误类型：VECTOR_DIMENSION_MISMATCH, MILVUS_WRITE_TIMEOUT等',
    sync_stage VARCHAR(50) NOT NULL COMMENT '同步环节：DATA_SOURCE_READ, DATA_PROCESSING, VECTOR_GENERATION, MILVUS_WRITE',
    error_time DATETIME NOT NULL COMMENT '错误时间',
    retry_strategy VARCHAR(50) DEFAULT 'EXPONENTIAL_BACKOFF' COMMENT '重试策略：EXPONENTIAL_BACKOFF, FIXED_INTERVAL, LINEAR_BACKOFF',
    process_status VARCHAR(50) DEFAULT 'PENDING' COMMENT '处理状态：PENDING, PROCESSING, SUCCESS, FAILED',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    last_retry_time DATETIME COMMENT '最后重试时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务错误数据表';

-- 任务执行日志表
CREATE TABLE IF NOT EXISTS task_execution_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执行日志ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    execution_id VARCHAR(100) NOT NULL COMMENT '执行ID',
    status VARCHAR(50) COMMENT '执行状态：STARTED, RUNNING, SUCCESS, FAILED, STOPPED',
    success_count BIGINT DEFAULT 0 COMMENT '成功记录数',
    error_count BIGINT DEFAULT 0 COMMENT '错误记录数',
    duration_ms BIGINT COMMENT '执行时长（毫秒）',
    logs TEXT COMMENT '执行日志',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

-- 任务性能监控表
CREATE TABLE IF NOT EXISTS task_performance_monitor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '监控ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    execution_id VARCHAR(100) NOT NULL COMMENT '执行ID',
    read_throughput DOUBLE COMMENT '读取吞吐量（条/秒）',
    write_throughput DOUBLE COMMENT '写入吞吐量（条/秒）',
    cpu_usage DOUBLE COMMENT 'CPU使用率（%）',
    memory_usage DOUBLE COMMENT '内存使用率（%）',
    network_usage DOUBLE COMMENT '网络使用率（%）',
    monitor_time DATETIME NOT NULL COMMENT '监控时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务性能监控表';

-- 向量化配置表
CREATE TABLE IF NOT EXISTS vectorization_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    algorithm VARCHAR(50) NOT NULL COMMENT '向量化算法：FASTTEXT, OPENAI, BERT等',
    dimension INT NOT NULL COMMENT '向量维度',
    generation_rate INT COMMENT '生成速率（tokens/秒）',
    api_key VARCHAR(255) COMMENT 'API密钥（用于OpenAI等需要密钥的算法）',
    model_name VARCHAR(255) COMMENT '模型名称',
    model_parameters TEXT COMMENT '模型参数（JSON格式）',
    field_mappings TEXT COMMENT '字段映射配置（JSON格式）',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='向量化配置表';

-- Milvus索引表
CREATE TABLE IF NOT EXISTS milvus_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '索引ID',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    collection_name VARCHAR(100) NOT NULL COMMENT '集合名称',
    index_name VARCHAR(100) NOT NULL COMMENT '索引名称',
    index_type VARCHAR(50) NOT NULL COMMENT '索引类型：IVF_FLAT, IVF_SQ8, IVF_PQ, HNSW, FLAT, BIN_FLAT',
    metric_type VARCHAR(50) NOT NULL COMMENT '度量类型：L2, IP, COSINE',
    dimension INT NOT NULL COMMENT '向量维度',
    index_parameters TEXT COMMENT '索引参数（JSON格式）',
    nlist INT COMMENT 'nlist参数（用于IVF系列索引）',
    ef_construction INT COMMENT 'efConstruction参数（用于HNSW索引）',
    m_param INT COMMENT 'M参数（用于HNSW索引）',
    status VARCHAR(50) DEFAULT 'BUILDING' COMMENT '索引状态：READY, BUILDING, FAILED',
    progress INT DEFAULT 0 COMMENT '构建进度（百分比）',
    memory_usage INT COMMENT '内存占用（MB）',
    disk_usage INT COMMENT '磁盘占用（MB）',
    error_message TEXT COMMENT '错误信息',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_tenant_collection_index (tenant_id, collection_name, index_name) COMMENT '租户ID、集合和索引名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Milvus索引表';

-- 插入系统预设角色
INSERT INTO role (tenant_id, name, code, description, status, deleted, create_time, update_time, create_by, update_by, version) VALUES
(0, '超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 'ENABLE', 0, NOW(), NOW(), 'system', 'system', 0),
(0, '管理员', 'ADMIN', '拥有管理权限', 'ENABLE', 0, NOW(), NOW(), 'system', 'system', 0),
(0, '操作员', 'OPERATOR', '拥有操作权限', 'ENABLE', 0, NOW(), NOW(), 'system', 'system', 0);

-- 插入系统预设数据源模板
INSERT INTO data_source_template (tenant_id, name, data_source_type, driver_class, log_monitor_type, default_port, is_system, deleted, create_time, update_time, create_by, update_by, version) VALUES
(0, 'MySQL默认模板', 'MYSQL', 'com.mysql.cj.jdbc.Driver', 'BINLOG', 3306, TRUE, 0, NOW(), NOW(), 'system', 'system', 0),
(0, 'PostgreSQL默认模板', 'POSTGRESQL', 'org.postgresql.Driver', 'WAL', 5432, TRUE, 0, NOW(), NOW(), 'system', 'system', 0),
(0, 'Oracle默认模板', 'ORACLE', 'oracle.jdbc.OracleDriver', 'REDO_LOG', 1521, TRUE, 0, NOW(), NOW(), 'system', 'system', 0),
(0, 'MongoDB默认模板', 'MONGODB', 'mongodb.jdbc.MongoDriver', 'OPLOG', 27017, TRUE, 0, NOW(), NOW(), 'system', 'system', 0);

-- 插入默认管理员用户（密码：admin123，已使用 BCrypt 加密）
INSERT INTO user (tenant_id, username, password, name, email, phone, status, deleted, create_time, update_time, create_by, update_by, version) VALUES
(0, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '管理员', 'admin@example.com', '13800138000', 'ENABLE', 0, NOW(), NOW(), 'system', 'system', 0);

-- 关联管理员用户与超级管理员角色
INSERT INTO user_role (user_id, role_id, tenant_id) VALUES
((SELECT id FROM user WHERE tenant_id = 0 AND username = 'admin'), (SELECT id FROM role WHERE tenant_id = 0 AND code = 'SUPER_ADMIN'), 0);
