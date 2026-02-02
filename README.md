# Data Rsync System

基于 Java 微服务架构开发的数据同步系统，核心目标是监听多源数据库的日志，支持全量 / 增量数据同步至 Milvus 向量数据库。

## 1. 项目架构

### 1.1 整体架构

采用 "接入层 - 应用层 - 数据层 - 基础设施层" 分层架构，微服务按功能模块拆分，基于 Spring Cloud 生态实现。

### 1.2 微服务模块

| 模块名称 | 功能描述 | 技术栈 |
|---------|---------|--------|
| data-rsync-gateway | API 网关，负责路由、鉴权、限流 | Spring Cloud Gateway + Nacos + Sentinel |
| data-rsync-auth | 认证授权服务，负责用户认证、权限管理 | Spring Boot + Spring Security + JWT + Nacos |
| data-rsync-data-source | 数据源管理服务，负责数据源配置、生命周期管理 | Spring Boot + JPA + MySQL + Redis + Nacos |
| data-rsync-log-listener | 日志监听服务，负责多源数据库日志监听、CDC 捕获 | Spring Boot + Debezium + Kafka + Redis + Nacos |
| data-rsync-data-process | 数据处理服务，负责数据转换、向量化处理 | Spring Boot + Kafka + Redis + Nacos |
| data-rsync-milvus-sync | Milvus 同步服务，负责数据写入、索引管理 | Spring Boot + Milvus Java SDK + Kafka + Redis + Nacos |
| data-rsync-task-manager | 任务管理服务，负责任务配置、调度、监控 | Spring Boot + XXL-Job + JPA + MySQL + Redis + Nacos |
| data-rsync-monitor | 监控服务，负责系统监控、告警、日志收集 | Spring Boot + Prometheus + Grafana + ELK + Nacos |
| data-rsync-common | 公共模块，包含工具类、常量定义、数据模型 | Spring Boot |

## 2. 环境准备

### 2.1 硬件要求

- CPU: 8 核以上
- 内存: 16GB 以上
- 磁盘: 500GB 以上

### 2.2 软件要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Kafka 3.5+
- Zookeeper 3.8+
- Nacos 2.2+
- Prometheus 2.40+
- Grafana 9.0+
- ELK Stack (Elasticsearch 8.0+, Logstash 8.0+, Kibana 8.0+)
- Milvus 2.4+

## 3. 项目初始化

### 3.1 克隆项目

```bash
git clone <项目地址>
cd data-rsync
```

### 3.2 构建项目

```bash
# 构建整个项目
mvn clean install

# 构建单个模块
mvn clean install -pl data-rsync-common -am
mvn clean install -pl data-rsync-gateway -am
# 以此类推构建其他模块
```

### 3.3 配置文件

每个微服务模块都有自己的配置文件，主要包括：

- `application.yml` - 本地开发配置
- `application-dev.yml` - 开发环境配置
- `application-test.yml` - 测试环境配置
- `application-prod.yml` - 生产环境配置

### 3.4 数据库初始化

1. 创建 MySQL 数据库：`data_rsync`
2. 执行 SQL 脚本初始化表结构（脚本位于 `sql` 目录）

### 3.5 启动服务

按照以下顺序启动服务：

1. 启动基础设施：MySQL、Redis、Kafka、Zookeeper、Nacos
2. 启动核心服务：data-rsync-auth、data-rsync-data-source、data-rsync-task-manager
3. 启动数据处理服务：data-rsync-log-listener、data-rsync-data-process、data-rsync-milvus-sync
4. 启动辅助服务：data-rsync-monitor
5. 启动网关：data-rsync-gateway

## 4. 开发流程

### 4.1 代码风格

- 遵循 Spring Boot 代码风格
- 使用 Lombok 简化代码
- 类名使用驼峰命名法，首字母大写
- 方法名使用驼峰命名法，首字母小写
- 变量名使用驼峰命名法，首字母小写
- 常量名使用全大写，下划线分隔

### 4.2 分支管理

- `master` - 主分支，用于生产环境
- `dev` - 开发分支，用于集成测试
- `feature/xxx` - 特性分支，用于开发新功能
- `bugfix/xxx` - 修复分支，用于修复 bug

### 4.3 提交规范

- 提交信息格式：`[模块名] 描述`
- 例如：`[data-source] 添加数据源连接测试功能`

### 4.4 测试规范

- 单元测试：使用 JUnit 5 + Mockito
- 集成测试：使用 Testcontainers
- 测试覆盖率：要求达到 80% 以上

## 5. 部署流程

### 5.1 容器化部署

使用 Docker Compose 或 Kubernetes 进行容器化部署。

### 5.2 CI/CD 流程

- 使用 Jenkins 或 GitLab CI 进行持续集成和持续部署
- 代码提交后自动触发构建、测试、部署流程

## 6. 监控与告警

- 使用 Prometheus + Grafana 进行系统监控
- 使用 ELK Stack 进行日志收集和分析
- 配置告警规则，支持邮件、钉钉、企业微信等告警方式

## 7. 安全注意事项

- 数据传输加密：使用 HTTPS/TLS
- 敏感配置加密：使用 Jasypt 加密配置文件中的敏感信息
- 接口鉴权：使用 JWT/OAuth2 进行接口鉴权
- 防 SQL 注入：使用参数化查询
- 防 XSS 攻击：使用 HTML 转义
- 防 CSRF 攻击：使用 CSRF Token

## 8. 开发工具

- IDE：IntelliJ IDEA 2023+ 或 Eclipse 2023+
- 代码质量：SonarQube
- 依赖管理：Maven
- 版本控制：Git

## 9. 联系方式

- 项目负责人：xxx
- 技术支持：xxx
- 邮箱：xxx

## 10. 许可证

Apache License 2.0
