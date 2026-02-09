# Nacos配置信息总结

## 项目概述
本文档总结了data-rsync项目中每个模块在Nacos中需要的配置信息，按照环境和模块进行组织。

## 配置结构变更说明

最近，我们对项目的配置结构进行了进一步简化和标准化，采用了以下配置管理方式：

1. **配置文件精简**：每个模块只保留两个配置文件
   - `bootstrap.yml`：只包含基本服务器配置和环境激活
   - `bootstrap-dev.yml`：只包含Nacos连接配置

2. **配置集中管理**：所有业务配置统一从Nacos中获取

3. **环境分离**：通过profiles实现不同环境的配置分离

## 本地配置文件结构

### 标准模块配置

每个模块的本地配置文件结构如下：

#### 1. bootstrap.yml

```yaml
server:
  port: {模块端口}
  servlet:
    context-path: /{模块上下文路径}
spring:
  application:
    name: {模块名称}
  profiles:
    active: dev
```

#### 2. bootstrap-dev.yml

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: ${spring.application.name}.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

### 模块配置示例

#### data-rsync-common

**bootstrap.yml**
```yaml
spring:
  application:
    name: data-rsync-common
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-common.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

#### data-rsync-task-manager

**bootstrap.yml**
```yaml
server:
  port: 8083
  servlet:
    context-path: /manager
spring:
  application:
    name: data-rsync-task-manager
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-task-manager.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

#### data-rsync-log-listener

**bootstrap.yml**
```yaml
server:
  port: 8705
  servlet:
    context-path: /listener
spring:
  application:
    name: data-rsync-log-listener
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-log-listener.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

#### data-rsync-data-source

**bootstrap.yml**
```yaml
server:
  port: 8704
  servlet:
    context-path: /source
spring:
  application:
    name: data-rsync-data-source
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-data-source.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

#### data-rsync-auth

**bootstrap.yml**
```yaml
server:
  port: 8706
  servlet:
    context-path: /auth
spring:
  application:
    name: data-rsync-auth
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-auth.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

#### data-rsync-gateway

**bootstrap.yml**
```yaml
server:
  port: 8701
  servlet:
    context-path: /gateway
spring:
  application:
    name: data-rsync-gateway
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-gateway.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

#### data-rsync-data-process

**bootstrap.yml**
```yaml
server:
  port: 8702
  servlet:
    context-path: /process
spring:
  application:
    name: data-rsync-data-process
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-data-process.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

#### data-rsync-milvus-sync

**bootstrap.yml**
```yaml
server:
  port: 8707
  servlet:
    context-path: /sync
spring:
  application:
    name: data-rsync-milvus-sync
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-milvus-sync.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

#### data-rsync-monitor

**bootstrap.yml**
```yaml
server:
  port: 8708
  servlet:
    context-path: /monitor
spring:
  application:
    name: data-rsync-monitor
  profiles:
    active: dev
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
        shared-configs:
          - dataId: data-rsync-monitor.yml
            group: DEFAULT_GROUP
            refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
```

## 配置结构

当前项目中，所有模块在Nacos中的配置按照以下格式组织：

- **命名空间**：`dev`（开发环境）
- **配置集ID**：{模块名}.yml
- **配置组**：DEFAULT_GROUP

### 配置文件示例

每个模块的主要配置文件应命名为：
- data-rsync-common.yml
- data-rsync-admin.yml
- data-rsync-auth.yml
- data-rsync-data-source.yml
- data-rsync-data-process.yml
- data-rsync-gateway.yml
- data-rsync-log-listener.yml
- data-rsync-milvus-sync.yml
- data-rsync-monitor.yml
- data-rsync-task-manager.yml

## 模块配置信息

### 1. data-rsync-common
**配置集ID**: data-rsync-common.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 系统优化配置文件
# 提升同步性能和可靠性

# 线程池配置
thread-pool:
  # 任务执行线程池
  task:
    core-size: 20
    max-size: 100
    queue-capacity: 10000
    keep-alive-seconds: 60
    thread-name-prefix: "task-executor-"
  # 数据处理线程池
  data-process:
    core-size: 30
    max-size: 150
    queue-capacity: 20000
    keep-alive-seconds: 60
    thread-name-prefix: "data-process-"
  # 网络请求线程池
  network:
    core-size: 10
    max-size: 50
    queue-capacity: 5000
    keep-alive-seconds: 60
    thread-name-prefix: "network-executor-"

# 数据库连接池配置（Druid）
spring:
  datasource:
    druid:
      # 基本配置
      initial-size: 10
      min-idle: 10
      max-active: 100
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      max-evictable-idle-time-millis: 600000
      validation-query: SELECT 1 FROM DUAL
      validation-query-timeout: 5000
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      # 连接属性
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000;druid.stat.logSlowSql=true
      # 过滤器
      filters: stat,wall,log4j2
      # 监控配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin
        reset-enable: false
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"

# Redis配置
redis:
  host: ${REDIS_HOST:localhost}
  port: ${REDIS_PORT:6379}
  password: ${REDIS_PASSWORD:}
  database: ${REDIS_DATABASE:1}
  # 连接池配置
  lettuce:
    pool:
      max-active: 100
      max-wait: 60000
      max-idle: 50
      min-idle: 10
  # 超时配置
  timeout: 10000

# Kafka配置
kafka:
  bootstrap-servers: ${KAFKA_HOST:localhost}:${KAFKA_PORT:9092}
  # 生产者配置
  producer:
    bootstrap-servers: ${kafka.bootstrap-servers}
    key-serializer: org.apache.kafka.common.serialization.StringSerializer
    value-serializer: org.apache.kafka.common.serialization.StringSerializer
    # 批量发送配置
    batch-size: 16384
    # 缓存大小
    buffer-memory: 33554432
    # 重试配置
    retries: 3
    # acks配置
    acks: all
    # 事务配置
    enable-idempotence: true
  # 消费者配置
  consumer:
    bootstrap-servers: ${kafka.bootstrap-servers}
    key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    # 组ID
    group-id: data-rsync-group
    # 自动提交偏移量
    enable-auto-commit: true
    # 自动提交间隔
    auto-commit-interval: 1000
    # 自动重置偏移量
    auto-offset-reset: earliest
    # 批量拉取配置
    max-poll-records: 500
    # 拉取超时
    fetch-max-wait: 500
    # 拉取大小
    fetch-min-size: 1
  # 监听器配置
  listener:
    missing-topics-fatal: false
    type: batch

# Feign配置
feign:
  client:
    config:
      default:
        # 连接超时
        connectTimeout: 10000
        # 读取超时
        readTimeout: 30000
        # 日志级别
        loggerLevel: basic
  # 重试配置
  retry:
    enabled: true
    max-attempts: 3
    backoff-period: 1000
    max-backoff-period: 5000

# 任务配置
task:
  # 调度配置
  scheduled:
    enabled: true
    cron: 0/30 * * * * ? # 每30秒执行一次
  # 执行配置
  execution:
    # 超时配置
    timeout-seconds: 3600
    # 重试配置
    retry-count: 3
    # 并发配置
    concurrency: 10
  # 批处理配置
  batch:
    size: 1000
    timeout: 5000
  # 分片配置
  shard:
    count: 10
    max-size: 100000

# 数据处理配置
data-process:
  # 清理配置
  cleaning:
    batch-size: 500
    thread-count: 20
  # 转换配置
  transformation:
    batch-size: 500
    thread-count: 20
  # 向量化配置
  vectorization:
    batch-size: 100
    thread-count: 10
    timeout: 10000
  # 缓存配置
  cache:
    enabled: true
    size: 10000
    expire-seconds: 3600

# Milvus配置
milvus:
  # 连接配置
  connection:
    host: ${MILVUS_HOST:localhost}
    port: ${MILVUS_PORT:19530}
    timeout: 30000
    max-connections: 50
  # 批处理配置
  batch:
    size: 100
    timeout: 5000
  # 重试配置
  retry:
    max-attempts: 3
    backoff-millis: 1000

# 监控配置
monitor:
  # 指标采集配置
  metrics:
    enabled: true
    frequency: 30 # 秒
  # 健康检查配置
  health:
    enabled: true
    frequency: 10 # 秒
  # 告警配置
  alert:
    enabled: true
    rate-limit: 60 # 秒

# 缓存配置
cache:
  # 本地缓存配置
  caffeine:
    enabled: true
    maximum-size: 10000
    expire-after-write: 3600 # 秒
    expire-after-access: 1800 # 秒
  # Redis缓存配置
  redis:
    enabled: true
    default-ttl: 3600 # 秒
    key-prefix: "data_rsync:"

# 安全配置
security:
  # 加密配置
  encryption:
    enabled: true
    algorithm: "AES"
    key: "your-secure-encryption-key"
  # 签名配置
  signature:
    enabled: true
    algorithm: "HMAC-SHA256"
    key: "your-secure-signature-key"

# 日志配置
logging:
  level:
    root: info
    com.data.rsync: debug
  # 异步日志配置
  async:
    enabled: true
    queue-size: 10000
    ring-buffer-size: 262144
  # 滚动策略配置
  file:
    max-size: 10MB
    max-history: 7

# 系统配置
system:
  # 环境配置
  env: ${spring.profiles.active}
  # 时区配置
  timezone: Asia/Shanghai
  # 字符集配置
  charset: UTF-8
  # 最大线程数
  max-threads: 200
  # 内存配置
  memory:
    max-heap: 2g
    min-heap: 512m

# 性能优化配置
performance:
  # 并行处理配置
  parallel:
    enabled: true
    core-threads: 20
    max-threads: 100
    queue-capacity: 10000
  # 批处理配置
  batching:
    enabled: true
    default-size: 1000
    max-size: 10000
  # 预热配置
  warmup:
    enabled: true
    delay: 30 # 秒
  # 降级配置
  fallback:
    enabled: true
    timeout: 5000

# 可靠性配置
reliability:
  # 幂等性配置
  idempotent:
    enabled: true
    cache-size: 10000
    expire-seconds: 3600
  # 事务配置
  transaction:
    enabled: true
    timeout: 30000
  # 补偿配置
  compensation:
    enabled: true
    max-attempts: 5
    interval: 60 # 秒

# 网络配置
network:
  # 连接池配置
  pool:
    max-total: 200
    max-per-route: 50
    validate-after-inactivity: 30000
  # 超时配置
  timeout:
    connect: 10000
    read: 30000
    write: 30000
  # 重试配置
  retry:
    enabled: true
    max-attempts: 3
    backoff: 1000
```

### 2. data-rsync-task-manager
**配置集ID**: data-rsync-task-manager.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:data_rsync}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver
    # Druid连接池配置
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,log4j2
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000

# Redis配置
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:1}

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.data.rsync.task.manager.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-value: 1
      logic-not-delete-value: 0

# 任务配置
task:
  # 调度配置
  scheduled:
    enabled: true
    cron: 0/30 * * * * ? # 每30秒执行一次
  # 线程池配置
  thread:
    core-size: 10
    max-size: 50
    queue-capacity: 1000
  # 分布式锁配置
  lock:
    expire-time: 60 # 锁过期时间（秒）
    retry-times: 3 # 重试次数
    retry-interval: 1000 # 重试间隔（毫秒）

# 日志配置
logging:
  level:
    root: info
    com.data.rsync.task.manager: debug
```

### 3. data-rsync-log-listener
**配置集ID**: data-rsync-log-listener.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# Kafka配置
spring:
  kafka:
    bootstrap-servers: ${KAFKA_HOST:localhost}:${KAFKA_PORT:9092}
    consumer:
      group-id: log-listener-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

# Redis配置
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:1}

# Debezium配置
debezium:
  # 偏移量存储
  offset:
    storage: org.apache.kafka.connect.storage.FileOffsetBackingStore
    file: /tmp/debezium/offsets/{task-id}.offset
    flush-interval: 60000
  # 数据库历史
  database:
    history: io.debezium.relational.history.FileDatabaseHistory
    history-file: /tmp/debezium/history/{task-id}.history

# 日志监听配置
log-listener:
  # 处理配置
  process:
    batch-size: 100
    timeout: 3000 # 3秒
  # 分片配置
  shard:
    enabled: true
    max-shards: 10
  # 断点续传
  breakpoint:
    enabled: true
    storage: redis

# 日志配置
logging:
  level:
    root: info
    com.data.rsync.log.listener: debug
```

### 4. data-rsync-data-source
**配置集ID**: data-rsync-data-source.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:data_rsync}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver
    # Druid连接池配置
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,log4j2
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000

# Redis配置
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:1}

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.data.rsync.datasource.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-value: 1
      logic-not-delete-value: 0

# 数据源配置
data-source:
  # 诊断配置
  diagnose:
    timeout: 30000 # 30秒
    retry-count: 3
  # 连接池配置
  pool:
    max-size: 100
    min-idle: 10
    max-wait: 60000
  # 监控配置
  monitor:
    enabled: true
    interval: 60000 # 1分钟

# 日志配置
logging:
  level:
    root: info
    com.data.rsync.datasource: debug
```

### 5. data-rsync-auth
**配置集ID**: data-rsync-auth.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:data_rsync}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver
    # Druid连接池配置
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,log4j2
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000

# Redis配置
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:1}

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.data.rsync.auth.model
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-value: 1
      logic-not-delete-value: 0

# 认证配置
auth:
  # JWT配置
  jwt:
    secret: ${JWT_SECRET:your-secret-key}
    expire-time: 3600 # 1小时
    refresh-time: 7200 # 2小时
  # 密码策略
  password:
    min-length: 6
    require-uppercase: true
    require-lowercase: true
    require-digit: true
    require-special: false
  # 登录配置
  login:
    max-attempts: 5
    lock-duration: 300 # 5分钟

# 日志配置
logging:
  level:
    root: info
    com.data.rsync.auth: debug
```

### 6. data-rsync-gateway
**配置集ID**: data-rsync-gateway.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# Redis配置
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:1}

# 网关配置
gateway:
  # 路由配置
  routes:
    - id: auth-route
      uri: lb://data-rsync-auth
      predicates:
        - Path=/auth/**
    - id: data-source-route
      uri: lb://data-rsync-data-source
      predicates:
        - Path=/source/**
    - id: task-manager-route
      uri: lb://data-rsync-task-manager
      predicates:
        - Path=/manager/**
    - id: log-listener-route
      uri: lb://data-rsync-log-listener
      predicates:
        - Path=/listener/**
  # 过滤器配置
  filter:
    rate-limit:
      enabled: true
      redis-rate-limiter:
        replenish-rate: 10 # 每秒允许的请求数
        burst-capacity: 20 # 令牌桶的容量
        requested-tokens: 1 # 每个请求消耗的令牌数
  # 安全配置
  security:
    enabled: true
    auth-url: /auth/login # 认证URL
    token-header: Authorization # 令牌头
    token-prefix: Bearer # 令牌前缀
  # 健康检查
  health-check:
    interval: 60000 # 健康检查间隔（毫秒）

# 日志配置
logging:
  level:
    root: info
    org.springframework.cloud.gateway: debug
```

### 7. data-rsync-data-process
**配置集ID**: data-rsync-data-process.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# Kafka配置
spring:
  kafka:
    bootstrap-servers: ${KAFKA_HOST:localhost}:${KAFKA_PORT:9092}
    consumer:
      group-id: data-process-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

# Redis配置
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:1}

# 数据处理配置
data-process:
  # 批处理配置
  batch:
    size: 1000
    timeout: 5000 # 5秒
  # 线程池配置
  thread:
    core-size: 10
    max-size: 50
    queue-capacity: 1000
  # 重试配置
  retry:
    max-attempts: 3
    backoff-multiplier: 1.5
    max-backoff: 5000
  # 监控配置
  monitor:
    enabled: true
    interval: 60000 # 1分钟

# 日志配置
logging:
  level:
    root: info
    com.data.rsync.data.process: debug
```

### 8. data-rsync-milvus-sync
**配置集ID**: data-rsync-milvus-sync.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# Redis配置
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:1}

# Milvus配置
milvus:
  host: ${MILVUS_HOST:localhost}
  port: ${MILVUS_PORT:19530}
  username: ${MILVUS_USERNAME:}
  password: ${MILVUS_PASSWORD:}
  database: ${MILVUS_DATABASE:default}
  connect-timeout: 30000
  read-timeout: 60000

# 同步配置
milvus-sync:
  # 批处理配置
  batch:
    size: 100
    timeout: 5000 # 5秒
  # 向量配置
  vector:
    dimension: 768
    metric-type: L2
  # 索引配置
  index:
    type: IVF_FLAT
    nlist: 128
  # 监控配置
  monitor:
    enabled: true
    interval: 60000 # 1分钟

# 日志配置
logging:
  level:
    root: info
    com.data.rsync.milvus.sync: debug
```

### 9. data-rsync-monitor
**配置集ID**: data-rsync-monitor.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:data_rsync}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver
    # Druid连接池配置
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall,log4j2
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000

# Redis配置
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:1}

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.data.rsync.monitor.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-value: 1
      logic-not-delete-value: 0

# 监控配置
monitor:
  # 指标配置
  metrics:
    enabled: true
    interval: 60000 # 1分钟
    collectors:
      - cpu
      - memory
      - disk
      - network
      - jvm
      - datasource
      - task
  # 告警配置
  alert:
    enabled: true
    threshold:
      cpu: 80
      memory: 80
      disk: 90
    notify:
      email:
        enabled: false
        recipients: admin@example.com
      webhook:
        enabled: false
        url: http://localhost:8080/alert

# 日志配置
logging:
  level:
    root: info
    com.data.rsync.monitor: debug
```

## 配置管理

### 如何在Nacos中添加配置
1. 登录Nacos控制台
2. 选择命名空间：`dev`
3. 点击「配置管理」->「配置列表」
4. 点击「+」按钮添加新配置
5. 填写配置信息并发布

### 配置文件命名规则
- **配置集ID**：{模块名}.yml（如data-rsync-task-manager.yml）
- **配置组**：DEFAULT_GROUP
- **命名空间**：dev

### 配置更新策略
- **开发环境**：实时更新
- **测试环境**：每日更新
- **生产环境**：按照发布计划更新，需经过审批

## 环境变量配置

### 常用环境变量

| 环境变量 | 默认值 | 说明 |
|---------|-------|------|
| NACOS_SERVER_ADDR | localhost:8848 | Nacos服务器地址 |
| NACOS_NAMESPACE | dev | Nacos命名空间 |
| NACOS_GROUP | DEFAULT_GROUP | Nacos配置组 |
| NACOS_USERNAME | nacos | Nacos用户名 |
| NACOS_PASSWORD | nacos | Nacos密码 |
| MYSQL_HOST | localhost | MySQL主机地址 |
| MYSQL_PORT | 3306 | MySQL端口 |
| MYSQL_DATABASE | data_rsync | MySQL数据库名 |
| MYSQL_USERNAME | root | MySQL用户名 |
| MYSQL_PASSWORD | 123456 | MySQL密码 |
| REDIS_HOST | localhost | Redis主机地址 |
| REDIS_PORT | 6379 | Redis端口 |
| REDIS_PASSWORD |  | Redis密码 |
| REDIS_DATABASE | 1 | Redis数据库索引 |
| KAFKA_HOST | localhost | Kafka主机地址 |
| KAFKA_PORT | 9092 | Kafka端口 |
| MILVUS_HOST | localhost | Milvus主机地址 |
| MILVUS_PORT | 19530 | Milvus端口 |
| MILVUS_USERNAME |  | Milvus用户名 |
| MILVUS_PASSWORD |  | Milvus密码 |
| MILVUS_DATABASE | default | Milvus数据库名 |
| JWT_SECRET | your-secret-key | JWT密钥 |

### 如何使用环境变量

在启动应用时，可以通过以下方式设置环境变量：

#### Linux/Mac
```bash
export NACOS_SERVER_ADDR=192.168.1.100:8848
export MYSQL_HOST=192.168.1.101
java -jar data-rsync-task-manager.jar
```

#### Windows
```cmd
set NACOS_SERVER_ADDR=192.168.1.100:8848
set MYSQL_HOST=192.168.1.101
java -jar data-rsync-task-manager.jar
```

## 注意事项

1. **敏感信息保护**：生产环境的数据库密码、Redis密码等敏感信息应使用Nacos的加密功能进行加密
2. **环境隔离**：不同环境的配置应分开管理，避免混淆
3. **配置变更记录**：配置变更应记录变更历史，便于追溯
4. **配置备份**：定期备份Nacos中的配置信息，防止配置丢失
5. **Nacos高可用**：生产环境应部署Nacos集群，确保配置中心的高可用性

## 故障排查

### 常见问题及解决方案

1. **Nacos连接失败**
   - 检查Nacos服务是否运行
   - 检查网络连接是否正常
   - 检查Nacos认证信息是否正确
   - 检查环境变量配置是否正确

2. **配置不生效**
   - 检查配置文件命名是否正确
   - 检查配置组和命名空间是否正确
   - 检查配置内容是否符合YAML格式
   - 检查应用是否正确加载了配置

3. **服务发现失败**
   - 检查服务是否正确注册到Nacos
   - 检查服务名称是否一致
   - 检查Nacos的服务列表是否包含该服务

4. **环境变量覆盖失败**
   - 检查环境变量名称是否正确
   - 检查环境变量值是否符合要求
   - 检查应用是否正确读取环境变量

## 总结

data-rsync项目采用了极简的本地配置文件结构，每个模块只保留两个配置文件：
- `bootstrap.yml`：只包含基本服务器配置和环境激活
- `bootstrap-dev.yml`：只包含Nacos连接配置

所有业务配置统一从Nacos中获取，实现了配置的集中管理和动态更新。这种配置架构具有以下优势：

1. **配置简化**：本地配置文件简洁明了，只包含必要信息
2. **集中管理**：所有业务配置统一存储在Nacos中
3. **动态更新**：配置变更后无需重启应用即可生效
4. **环境灵活**：通过环境变量和profiles实现不同环境的灵活部署
5. **易于维护**：配置结构清晰，便于管理和维护

通过这种配置管理方式，可以提高系统的可靠性、可维护性和可扩展性，为data-rsync项目的稳定运行提供有力保障。