-- 数据库脚本
-- 生成时间: 2026-02-02
-- 包含详细字段注释

-- 删除现有表（按依赖关系顺序）
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
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE, DISABLE',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_username (username) COMMENT '用户名唯一索引',
    UNIQUE KEY uk_email (email) COMMENT '邮箱唯一索引',
    UNIQUE KEY uk_phone (phone) COMMENT '手机号唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    code VARCHAR(100) NOT NULL COMMENT '角色编码',
    description VARCHAR(500) COMMENT '角色描述',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE, DISABLE',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_name (name) COMMENT '角色名称唯一索引',
    UNIQUE KEY uk_code (code) COMMENT '角色编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL COMMENT '权限编码',
    type VARCHAR(20) NOT NULL COMMENT '权限类型：MENU, BUTTON, API, DATA',
    description VARCHAR(500) COMMENT '权限描述',
    resource_path VARCHAR(255) COMMENT '资源路径',
    parent_id BIGINT COMMENT '父权限ID',
    sort INT COMMENT '排序',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE, DISABLE',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_name (name) COMMENT '权限名称唯一索引',
    UNIQUE KEY uk_code (code) COMMENT '权限编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id) COMMENT '联合主键'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id) COMMENT '联合主键'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 数据源表
CREATE TABLE IF NOT EXISTS data_source (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据源ID',
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
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
    health_status VARCHAR(50) COMMENT '健康状态',
    last_failure_reason TEXT COMMENT '最近一次失败原因',
    heartbeat_time INT COMMENT '心跳检测时间间隔（秒）',
    last_heartbeat_time DATETIME COMMENT '最近一次心跳时间',
    failure_count INT DEFAULT 0 COMMENT '失败次数',
    description TEXT COMMENT '描述',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    create_by VARCHAR(100) COMMENT '创建人',
    update_by VARCHAR(100) COMMENT '更新人',
    version INT DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_name (name) COMMENT '数据源名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源表';

-- 数据源模板表
CREATE TABLE IF NOT EXISTS data_source_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    data_source_type VARCHAR(50) NOT NULL COMMENT '数据源类型',
    driver_class VARCHAR(255) NOT NULL COMMENT '默认驱动类名',
    log_monitor_type VARCHAR(50) NOT NULL COMMENT '默认日志监听类型',
    default_port INT NOT NULL COMMENT '默认端口',
    connection_timeout INT COMMENT '连接超时时间（秒）',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统预设模板',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_name_type (name, data_source_type) COMMENT '模板名称和类型唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源模板表';

-- 数据源诊断报告表
CREATE TABLE IF NOT EXISTS data_source_diagnose_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    data_source_id BIGINT NOT NULL COMMENT '数据源ID',
    overall_status VARCHAR(50) NOT NULL COMMENT '整体状态：HEALTHY, WARNING, UNHEALTHY',
    network_status VARCHAR(50) NOT NULL COMMENT '网络状态：OK, ERROR',
    authentication_status VARCHAR(50) NOT NULL COMMENT '认证状态：OK, ERROR',
    log_monitor_status VARCHAR(50) NOT NULL COMMENT '日志监听状态：OK, ERROR',
    connection_status VARCHAR(50) NOT NULL COMMENT '连接状态：OK, ERROR',
    diagnose_time DATETIME NOT NULL COMMENT '诊断时间',
    diagnose_duration INT COMMENT '诊断耗时（毫秒）',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源诊断报告表';

-- 任务表
CREATE TABLE IF NOT EXISTS task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    type VARCHAR(50) NOT NULL COMMENT '任务类型：FULL_SYNC, INCREMENTAL_SYNC, FULL_AND_INCREMENTAL',
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
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
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
    UNIQUE KEY uk_name (name) COMMENT '任务名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 任务节点表
CREATE TABLE IF NOT EXISTS task_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '节点ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    node_type VARCHAR(50) NOT NULL COMMENT '节点类型：dataSource, syncType, dataProcess, vectorization, milvusWrite',
    node_label VARCHAR(100) NOT NULL COMMENT '节点标签',
    node_config TEXT COMMENT '节点配置（JSON格式）',
    position_x INT NOT NULL COMMENT '节点位置X坐标',
    position_y INT NOT NULL COMMENT '节点位置Y坐标',
    status VARCHAR(50) COMMENT '节点状态：READY, PROCESSING, SUCCESS, FAILED',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务节点表';

-- 任务连接表
CREATE TABLE IF NOT EXISTS task_connection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '连接ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    source_node_id BIGINT NOT NULL COMMENT '源节点ID',
    target_node_id BIGINT NOT NULL COMMENT '目标节点ID',
    source_handle_id VARCHAR(100) COMMENT '源节点句柄ID',
    target_handle_id VARCHAR(100) COMMENT '目标节点句柄ID',
    status VARCHAR(50) COMMENT '连接状态',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务连接表';

-- 任务依赖表
CREATE TABLE IF NOT EXISTS task_dependency (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '依赖ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    dependency_task_id BIGINT NOT NULL COMMENT '依赖任务ID',
    dependency_condition TEXT COMMENT '依赖条件（JSON格式）',
    status VARCHAR(50) COMMENT '依赖状态：PENDING, MET, FAILED',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务依赖表';

-- 任务错误数据表
CREATE TABLE IF NOT EXISTS task_error_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '错误数据ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    source_data TEXT NOT NULL COMMENT '源数据（JSON格式）',
    error_message TEXT NOT NULL COMMENT '错误原因',
    error_type VARCHAR(50) NOT NULL COMMENT '错误类型：VECTOR_DIMENSION_MISMATCH, MILVUS_WRITE_TIMEOUT等',
    sync_stage VARCHAR(50) NOT NULL COMMENT '同步环节：DATA_SOURCE_READ, DATA_PROCESSING, VECTOR_GENERATION, MILVUS_WRITE',
    error_time DATETIME NOT NULL COMMENT '错误时间',
    process_status VARCHAR(50) DEFAULT 'PENDING' COMMENT '处理状态：PENDING, PROCESSING, SUCCESS, FAILED',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    last_retry_time DATETIME COMMENT '最后重试时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务错误数据表';

-- 向量化配置表
CREATE TABLE IF NOT EXISTS vectorization_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    algorithm VARCHAR(50) NOT NULL COMMENT '向量化算法：FASTTEXT, OPENAI, BERT等',
    dimension INT NOT NULL COMMENT '向量维度',
    generation_rate INT COMMENT '生成速率（tokens/秒）',
    api_key VARCHAR(255) COMMENT 'API密钥（用于OpenAI等需要密钥的算法）',
    model_name VARCHAR(255) COMMENT '模型名称',
    field_mappings TEXT COMMENT '字段映射配置（JSON格式）',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='向量化配置表';

-- Milvus索引表
CREATE TABLE IF NOT EXISTS milvus_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '索引ID',
    collection_name VARCHAR(100) NOT NULL COMMENT '集合名称',
    index_name VARCHAR(100) NOT NULL COMMENT '索引名称',
    index_type VARCHAR(50) NOT NULL COMMENT '索引类型：IVF_FLAT, IVF_SQ8, IVF_PQ, HNSW, FLAT, BIN_FLAT',
    metric_type VARCHAR(50) NOT NULL COMMENT '度量类型：L2, IP, COSINE',
    dimension INT NOT NULL COMMENT '向量维度',
    nlist INT COMMENT 'nlist参数（用于IVF系列索引）',
    ef_construction INT COMMENT 'efConstruction参数（用于HNSW索引）',
    m_param INT COMMENT 'M参数（用于HNSW索引）',
    status VARCHAR(50) DEFAULT 'BUILDING' COMMENT '索引状态：READY, BUILDING, FAILED',
    progress INT DEFAULT 0 COMMENT '构建进度（百分比）',
    memory_usage INT COMMENT '内存占用（MB）',
    disk_usage INT COMMENT '磁盘占用（MB）',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_collection_index (collection_name, index_name) COMMENT '集合和索引名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Milvus索引表';

-- 插入系统预设角色
INSERT INTO role (name, code, description, status, create_time, update_time) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 'ENABLE', NOW(), NOW()),
('管理员', 'ADMIN', '拥有管理权限', 'ENABLE', NOW(), NOW()),
('操作员', 'OPERATOR', '拥有操作权限', 'ENABLE', NOW(), NOW());

-- 插入系统预设数据源模板
INSERT INTO data_source_template (name, data_source_type, driver_class, log_monitor_type, default_port, is_system, create_time, update_time) VALUES
('MySQL默认模板', 'MYSQL', 'com.mysql.cj.jdbc.Driver', 'BINLOG', 3306, TRUE, NOW(), NOW()),
('PostgreSQL默认模板', 'POSTGRESQL', 'org.postgresql.Driver', 'WAL', 5432, TRUE, NOW(), NOW()),
('Oracle默认模板', 'ORACLE', 'oracle.jdbc.OracleDriver', 'REDO_LOG', 1521, TRUE, NOW(), NOW()),
('MongoDB默认模板', 'MONGODB', 'mongodb.jdbc.MongoDriver', 'OPLOG', 27017, TRUE, NOW(), NOW());

-- 插入默认管理员用户（密码：admin123，已使用 BCrypt 加密）
INSERT INTO user (username, password, name, email, phone, status, create_time, update_time) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '管理员', 'admin@example.com', '13800138000', 'ENABLE', NOW(), NOW());

-- 关联管理员用户与超级管理员角色
INSERT INTO user_role (user_id, role_id) VALUES
((SELECT id FROM user WHERE username = 'admin'), (SELECT id FROM role WHERE code = 'SUPER_ADMIN'));