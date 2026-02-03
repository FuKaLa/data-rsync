# Nacos配置信息总结

## 项目概述
本文档总结了data-rsync项目中每个模块在Nacos中需要的配置信息，按照环境和模块进行组织。

## 配置结构
每个模块在Nacos中的配置应该按照以下格式组织：
- 命名空间：dev（开发环境）、test（测试环境）、prod（生产环境）
- 配置集ID：{模块名}-{环境}.yml
- 配置组：DEFAULT_GROUP

## 模块配置信息

### 1. data-rsync-admin
**配置集ID**: data-rsync-admin-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://localhost:3306/data_rsync?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
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
    host: localhost
    port: 6379
    password: 
    database: 1

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.data.rsync.admin.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-value: 1
      logic-not-delete-value: 0

# 日志配置
logging:
  level:
    root: info
    com.data.rsync.admin: debug
```

### 2. data-rsync-auth
**配置集ID**: data-rsync-auth-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://localhost:3306/data_rsync?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
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
    host: localhost
    port: 6379
    password: 
    database: 1

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
  jwt:
    secret: your-secret-key
    expire-time: 3600 # 1小时
  
  # 密码策略
  password:
    min-length: 6
    require-uppercase: true
    require-lowercase: true
    require-digit: true
    require-special: false
```

### 3. data-rsync-data-source
**配置集ID**: data-rsync-data-source-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://localhost:3306/data_rsync?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
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
    host: localhost
    port: 6379
    password: 
    database: 1

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
```

### 4. data-rsync-data-process
**配置集ID**: data-rsync-data-process-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# Redis配置
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 1

# Kafka配置
kafka:
  bootstrap-servers: localhost:9092
  producer:
    key-serializer: org.apache.kafka.common.serialization.StringSerializer
    value-serializer: org.apache.kafka.common.serialization.StringSerializer
  consumer:
    group-id: data-process-group
    key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

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
```

### 5. data-rsync-log-listener
**配置集ID**: data-rsync-log-listener-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# Kafka配置
kafka:
  bootstrap-servers: localhost:9092
  consumer:
    group-id: log-listener-group
    key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

# 日志配置
log-listener:
  # 处理配置
  process:
    batch-size: 100
    timeout: 3000 # 3秒
  
  # 存储配置
  storage:
    enabled: true
    type: elasticsearch # 可选：elasticsearch, mysql
    elasticsearch:
      hosts: localhost:9200
      index: data-rsync-logs
```

### 6. data-rsync-milvus-sync
**配置集ID**: data-rsync-milvus-sync-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# Redis配置
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 1

# Milvus配置
milvus:
  host: localhost
  port: 19530
  username: 
  password: 
  database: default

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
```

### 7. data-rsync-monitor
**配置集ID**: data-rsync-monitor-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://localhost:3306/data_rsync?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
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
    host: localhost
    port: 6379
    password: 
    database: 1

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
```

### 8. data-rsync-task-manager
**配置集ID**: data-rsync-task-manager-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# 数据库配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://localhost:3306/data_rsync?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
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
    host: localhost
    port: 6379
    password: 
    database: 1

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

# XXL-Job配置
xxl:
  job:
    admin:
      addresses: http://localhost:8080/xxl-job-admin
    executor:
      appname: data-rsync-task-manager
      ip: 
      port: 9999
      logpath: /data/applogs/xxl-job/jobhandler
      logretentiondays: 30
    accessToken: default_token

# 任务配置
task:
  scheduled:
    enabled: true
    cron: 0/30 * * * * ? # 每30秒执行一次
  
  # 线程池配置
  thread:
    core-size: 10
    max-size: 50
    queue-capacity: 1000
```

### 9. data-rsync-gateway
**配置集ID**: data-rsync-gateway-dev.yml
**配置组**: DEFAULT_GROUP
**命名空间**: dev

```yaml
# Redis配置
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 1

# 网关配置
gateway:
  filter:
    rate-limit:
      enabled: true
      redis-rate-limiter:
        replenish-rate: 10 # 每秒允许的请求数
        burst-capacity: 20 # 令牌桶的容量
        requested-tokens: 1 # 每个请求消耗的令牌数
  security:
    enabled: true
    auth-url: /auth/login # 认证URL
    token-header: Authorization # 令牌头
    token-prefix: Bearer # 令牌前缀
  health-check:
    interval: 60000 # 健康检查间隔（毫秒）
```

## 配置管理

### 如何在Nacos中添加配置
1. 登录Nacos控制台
2. 选择对应的命名空间（如dev）
3. 点击「配置管理」->「配置列表」
4. 点击「+」按钮添加新配置
5. 填写配置信息并发布

### 配置更新策略
- 开发环境：实时更新
- 测试环境：每日更新
- 生产环境：按照发布计划更新，需经过审批

## 注意事项
1. 生产环境的数据库密码、Redis密码等敏感信息应使用Nacos的加密功能进行加密
2. 不同环境的配置应分开管理，避免混淆
3. 配置变更应记录变更历史，便于追溯
4. 定期备份Nacos中的配置信息，防止配置丢失
