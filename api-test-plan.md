# API接口测试计划

## 1. 认证相关页面

### 1.1 登录页面 (Login.vue)
- **接口**: `/auth/login` (POST)
- **请求参数**:
  - `username`: 用户名 (string, 必填)
  - `password`: 密码 (string, 必填)
- **响应参数**:
  - `code`: 状态码 (number)
  - `message`: 消息 (string)
  - `data`: JWT token (string)
- **测试要点**:
  - 正确用户名密码登录
  - 错误用户名登录
  - 错误密码登录
  - 空用户名登录
  - 空密码登录

### 1.2 其他认证接口
- **获取当前用户**: `/auth/current-user` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 用户信息对象
- **注销**: `/auth/logout` (POST)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: null

## 2. 数据源管理页面

### 2.1 数据源列表页面 (data-source/List.vue)
- **接口1**: `/data-source` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 数据源数组
- **接口2**: `/data-source/{id}/test-connection` (POST)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: boolean
- **接口3**: `/data-source/{id}/enable` (PUT)
  - **请求参数**:
    - `enabled`: 是否启用 (boolean, 必填)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 数据源对象
- **接口4**: `/data-source/{id}/diagnose` (POST)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 诊断报告对象
- **接口5**: `/data-source/{id}` (DELETE)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: null

### 2.2 数据源创建页面 (data-source/Create.vue)
- **接口1**: `/data-source` (POST)
  - **请求参数**:
    - `name`: 名称 (string, 必填)
    - `type`: 类型 (string, 必填)
    - `host`: 主机 (string, 可选)
    - `port`: 端口 (number, 可选)
    - `databaseName`: 数据库名 (string, 可选)
    - `username`: 用户名 (string, 可选)
    - `password`: 密码 (string, 可选)
    - `url`: 连接URL (string, 必填)
    - `enabled`: 是否启用 (boolean, 必填)
    - `logMonitorType`: 日志监控类型 (string, 可选)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 数据源对象
- **接口2**: `/data-source/test-connection` (POST)
  - **请求参数**:
    - 同创建接口
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: { success: boolean, message: string }

### 2.3 数据源详情页面 (data-source/Detail.vue)
- **接口1**: `/data-source/{id}` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 数据源对象

### 2.4 数据源模板页面 (data-source/TemplateList.vue)
- **接口1**: `/data-source/templates` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 模板数组
- **接口2**: `/data-source/templates/{id}` (DELETE)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: null
- **接口3**: `/data-source/templates/init-system` (POST)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: null

## 3. 任务管理页面

### 3.1 任务列表页面 (task/List.vue)
- **接口1**: `/tasks` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 任务数组
- **接口2**: `/tasks/{id}` (DELETE)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: null
- **接口3**: `/tasks/{id}/trigger` (POST)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: { success: boolean, message: string }
- **接口4**: `/tasks/{id}/pause` (POST)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: { success: boolean, message: string }
- **接口5**: `/tasks/{id}/resume` (POST)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: { success: boolean, message: string }
- **接口6**: `/tasks/{id}/rollback` (POST)
  - **请求参数**:
    - `rollbackPoint`: 回滚点 (string, 必填)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: { success: boolean, message: string }
- **接口7**: `/tasks/{id}/versions` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 版本数组

### 3.2 任务创建页面 (task/Create.vue)
- **接口1**: `/tasks` (POST)
  - **请求参数**:
    - `name`: 名称 (string, 必填)
    - `type`: 类型 (string, 必填)
    - `dataSourceId`: 数据源ID (number, 必填)
    - `databaseName`: 数据库名 (string, 可选)
    - `tableName`: 表名 (string, 可选)
    - `enabled`: 是否启用 (boolean, 可选)
    - 其他可选参数
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 任务对象
- **接口2**: `/data-source` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 数据源数组

### 3.3 任务详情页面 (task/Detail.vue)
- **接口1**: `/tasks/{id}` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 任务对象

### 3.4 错误数据页面 (task/ErrorData.vue)
- **接口1**: `/tasks/{id}/error-data` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 错误数据数组

## 4. 监控页面

### 4.1 监控仪表板 (monitor/Dashboard.vue)
- **接口**: `/monitor/metrics` (GET)
  - **响应参数**:
    - `code`: 状态码 (number)
    - `message`: 消息 (string)
    - `data`: 监控指标对象

### 4.2 指标监控页面 (monitor/MetricsMonitoring.vue)
- **接口**: `/monitor/metrics/*` (GET)
  - 各种监控指标接口

## 5. Milvus页面

### 5.1 集合页面 (milvus/Collections.vue)
- **接口1**: `/milvus/collections` (GET)
  - **响应参数**:
    - 集合列表
- **接口2**: `/milvus/collections` (POST)
  - **请求参数**:
    - 集合配置
  - **响应参数**:
    - 创建结果
- **接口3**: `/milvus/collections/{collectionName}` (DELETE)
  - **响应参数**:
    - 删除结果

### 5.2 索引页面 (milvus/Indexes.vue)
- **接口1**: `/milvus/indexes/{collectionName}` (GET)
  - **响应参数**:
    - 索引列表
- **接口2**: `/milvus/indexes/{collectionName}` (POST)
  - **请求参数**:
    - 索引配置
  - **响应参数**:
    - 创建结果
- **接口3**: `/milvus/indexes/{collectionName}/{indexName}` (DELETE)
  - **响应参数**:
    - 删除结果

### 5.3 健康检查页面 (milvus/Health.vue)
- **接口**: `/milvus/health` (GET)
  - **响应参数**:
    - 健康状态

## 6. 系统管理页面

### 6.1 用户管理页面 (system/Users.vue)
- **接口1**: `/system/users` (GET)
  - **响应参数**:
    - 用户列表
- **接口2**: `/system/users` (POST)
  - **请求参数**:
    - 用户信息
  - **响应参数**:
    - 创建结果
- **接口3**: `/system/users/{id}` (PUT)
  - **请求参数**:
    - 用户信息
  - **响应参数**:
    - 更新结果
- **接口4**: `/system/users/{id}` (DELETE)
  - **响应参数**:
    - 删除结果

### 6.2 角色管理页面 (system/Roles.vue)
- **接口1**: `/system/roles` (GET)
  - **响应参数**:
    - 角色列表
- **接口2**: `/system/roles` (POST)
  - **请求参数**:
    - 角色信息
  - **响应参数**:
    - 创建结果
- **接口3**: `/system/roles/{id}` (PUT)
  - **请求参数**:
    - 角色信息
  - **响应参数**:
    - 更新结果
- **接口4**: `/system/roles/{id}` (DELETE)
  - **响应参数**:
    - 删除结果

### 6.3 配置管理页面 (system/ConfigManagement.vue)
- **接口1**: `/system/config` (GET)
  - **响应参数**:
    - 配置信息
- **接口2**: `/system/config` (PUT)
  - **请求参数**:
    - 配置信息
  - **响应参数**:
    - 更新结果

### 6.4 日志页面 (system/Logs.vue)
- **接口1**: `/system/logs` (GET)
  - **响应参数**:
    - 日志列表
- **接口2**: `/system/logs/clean` (POST)
  - **响应参数**:
    - 清理结果

## 7. 字典管理页面

### 7.1 字典类型页面 (dict/DictTypeList.vue)
- **接口**: 字典类型相关接口

### 7.2 字典项页面 (dict/DictItemList.vue)
- **接口**: 字典项相关接口

## 8. 测试策略

### 8.1 测试步骤
1. **准备测试环境**: 启动所有必要的服务
2. **认证测试**: 测试登录接口获取token
3. **页面接口测试**: 按页面顺序测试所有接口
4. **参数验证**: 验证请求参数和响应参数
5. **错误处理**: 测试异常情况
6. **性能测试**: 测试接口响应时间

### 8.2 测试工具
- **Postman**: 用于API测试
- **curl**: 用于命令行测试
- **浏览器开发者工具**: 用于前端调试

### 8.3 测试标准
- **请求参数**: 所有必填参数必须正确传递
- **响应参数**: 响应结构必须符合前端期望
- **状态码**: 成功操作返回200，错误返回对应错误码
- **响应时间**: 接口响应时间应在合理范围内
- **错误处理**: 接口应返回清晰的错误信息

## 9. 修复流程
1. **问题识别**: 记录测试中发现的问题
2. **问题分析**: 分析问题原因
3. **代码修复**: 修改相关代码
4. **服务重启**: 重启服务
5. **验证测试**: 重新测试修复的接口
6. **回归测试**: 确保修复不影响其他功能

## 10. 测试报告

测试完成后，将生成详细的测试报告，包括：
- 测试覆盖情况
- 发现的问题及修复情况
- 接口性能分析
- 改进建议
