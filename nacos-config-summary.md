# Nacos配置信息总结

## 项目概述
本文档总结了data-rsync项目中每个模块在Nacos中需要的配置信息，按照环境和模块进行组织。

## 配置结构变更说明

最近，我们对项目的配置结构进行了调整，所有模块的本地配置文件现在只保留基本配置，剩下的所有配置都从Nacos中读取。这样可以实现配置的集中管理，便于在不同环境中统一配置。

## 本地配置文件结构

### 标准模块配置

大多数模块的本地配置文件（bootstrap.yml）现在使用以下配置结构：

```yaml
server:
  port: {模块端口}
  servlet:
    context-path: /{模块上下文路径}
spring:
  application:
    name: {模块名称}
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        username: nacos
        password: nacos
        namespace: a9617f63-b450-4315-8ae7-7be6ac6611bf
      config:
        server-addr: localhost:8848
        file-extension: yml
        group: DEFAULT_GROUP
        username: nacos
        password: nacos
        namespace: a9617f63-b450-4315-8ae7-7be6ac6611bf
        shared-configs:
          - dataId: {模块名称}.yml
            group: DEFAULT_GROUP
            refresh: true
```

### 公共模块配置

data-rsync-common模块使用了更灵活的配置方式，支持环境变量：

```yaml
# Bootstrap配置文件
spring:
  application:
    name: data-rsync-common
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        shared-configs:
          - dataId: data-rsync.yml
            group: DEFAULT_GROUP
            refresh: true
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}

# 环境变量配置
---
spring:
  profiles:
    active: dev
```

所有其他配置（如数据源配置、MyBatis-Plus配置、业务特定配置等）都从Nacos中读取。

## 配置结构

当前项目中，所有模块在Nacos中的配置按照以下格式组织：

- 命名空间：使用统一的命名空间ID `a9617f63-b450-4315-8ae7-7be6ac6611bf`
- 配置集ID：{模块名}.yml
- 配置组：DEFAULT_GROUP

### 配置文件示例

每个模块的主要配置文件应命名为：
- data-rsync-admin.yml
- data-rsync-auth.yml
- data-rsync-data-source.yml
- data-rsync-data-process.yml
- data-rsync-gateway.yml
- data-rsync-log-listener.yml
- data-rsync-milvus-sync.yml
- data-rsync-monitor.yml
- data-rsync-task-manager.yml

### 共享配置

对于所有模块共享的配置，可以创建一个 data-rsync.yml 配置文件，然后在各模块中通过 shared-configs 引用。

## 模块配置信息

### 1. data-rsync-admin
**配置集ID**: data-rsync-admin.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
**配置集ID**: data-rsync-auth.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
**配置集ID**: data-rsync-data-source.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
**配置集ID**: data-rsync-data-process.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
**配置集ID**: data-rsync-log-listener.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
**配置集ID**: data-rsync-milvus-sync.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
**配置集ID**: data-rsync-monitor.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
**配置集ID**: data-rsync-task-manager.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
**配置集ID**: data-rsync-gateway.yml
**配置组**: DEFAULT_GROUP
**命名空间**: a9617f63-b450-4315-8ae7-7be6ac6611bf

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
2. 选择对应的命名空间（如dev、test、prod）
3. 点击「配置管理」->「配置列表」
4. 点击「+」按钮添加新配置
5. 填写配置信息并发布

### 配置文件命名规则
- 配置集ID：{模块名}.yml（如data-rsync-admin.yml）
- 配置组：DEFAULT_GROUP
- 命名空间：根据环境选择（dev、test、prod）

### 配置更新策略
- 开发环境：实时更新
- 测试环境：每日更新
- 生产环境：按照发布计划更新，需经过审批

## Nacos认证配置

### 认证配置说明

为了确保Nacos的安全性，我们需要配置Nacos的认证信息。根据之前的配置操作，我们已经设置了以下认证配置：

```yaml
nacos:
  core:
    auth:
      plugin:
        nacos:
          token:
            secret:
              key: dGhpcyBpcyBhIHRlc3QgY2xhdmUga2V5IGZvciBuYWNvcyAxMjM0NTY=
      server:
        identity:
          key: dGhpcyBpcyBhIHRlc3QgY2xhdmUga2V5IGZvciBuYWNvcyAxMjM0NTY=
          value: dGhpcyBpcyBhIHRlc3QgY2xhdmUga2V5IGZvciBuYWNvcyAxMjM0NTY=
```

### 如何获取这些配置

1. 启动Nacos服务后，首次登录控制台时，系统会提示设置token.secret.key
2. 按照提示，输入一个长度大于32的字符串，系统会自动将其转换为Base64编码
3. 同时，系统会提示设置identity.key和identity.value
4. 记录下这些配置值，并在所有模块的配置中使用相同的值

### 配置生效方式

这些配置会在Nacos服务启动时生效，所有客户端在连接Nacos时需要提供正确的认证信息。

## 注意事项
1. 生产环境的数据库密码、Redis密码等敏感信息应使用Nacos的加密功能进行加密
2. 不同环境的配置应分开管理，避免混淆
3. 配置变更应记录变更历史，便于追溯
4. 定期备份Nacos中的配置信息，防止配置丢失
5. Nacos的认证配置（token.secret.key和identity配置）应在所有环境中保持一致，确保所有模块能够正确连接到Nacos