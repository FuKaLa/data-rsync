// 全局类型定义

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 用户信息类型
export interface UserInfo {
  id: number
  username: string
  name: string
  email: string
  phone: string
  status: string
  roles: string[]
  permissions: string[]
}

// 数据源类型
export interface DataSource {
  id: number
  name: string
  type: string
  host?: string
  port?: number
  databaseName?: string
  username?: string
  password?: string
  url: string
  enabled: boolean
  healthStatus: string
  logMonitorType?: string
  createTime: string
  updateTime: string
}

// 数据源模板类型
export interface DataSourceTemplate {
  id: number
  name: string
  dataSourceType: string
  driverClass: string
  logMonitorType: string
  defaultPort: number
  connectionTimeout: number
  description: string
  isSystem: boolean
  createTime: string
  updateTime: string
}

// 数据源诊断报告类型
export interface DataSourceDiagnoseReport {
  id: number
  dataSourceId: number
  diagnoseTime: string
  status: string
  details: any
}

// 任务类型
export interface Task {
  id: number
  name: string
  type: string
  dataSourceId: number
  databaseName?: string
  tableName?: string
  enabled: boolean
  concurrency?: number
  batchSize?: number
  retryCount?: number
  timeoutSeconds?: number
  scheduleType?: string
  scheduleExpression?: string
  interval?: number
  remark?: string
  config?: string
  status: string
  createTime: string
  updateTime: string
  lastExecuteTime?: string
  nextExecuteTime?: string
}

// 任务节点类型
export interface TaskNode {
  id: number
  taskId: number
  name: string
  type: string
  config: string
  position: string
  createTime: string
  updateTime: string
}

// 任务连接类型
export interface TaskConnection {
  id: number
  taskId: number
  sourceNodeId: number
  targetNodeId: number
  config: string
  createTime: string
  updateTime: string
}

// 任务依赖类型
export interface TaskDependency {
  id: number
  taskId: number
  dependentTaskId: number
  dependencyType: string
  config: string
  createTime: string
  updateTime: string
}

// 监控指标类型
export interface Metrics {
  cpu: {
    usage: number
    cores: number
  }
  memory: {
    used: number
    total: number
    usage: number
  }
  disk: {
    used: number
    total: number
    usage: number
  }
  network: {
    in: number
    out: number
  }
}

// 向量化配置类型
export interface VectorizationConfig {
  id: number
  taskId: number
  algorithm: string
  dimension: number
  modelName: string
  config: string
  createTime: string
  updateTime: string
}

// Milvus索引类型
export interface MilvusIndex {
  id: number
  collectionName: string
  indexName: string
  indexType: string
  metricType: string
  params: string
  status: string
  progress: number
  createTime: string
  updateTime: string
}

// 创建数据源请求类型
export interface CreateDataSourceRequest {
  name: string
  type: string
  host?: string
  port?: number
  databaseName?: string
  username?: string
  password?: string
  url: string
  enabled: boolean
  logMonitorType?: string
}

// 更新数据源请求类型
export interface UpdateDataSourceRequest {
  name?: string
  host?: string
  port?: number
  databaseName?: string
  username?: string
  password?: string
  url?: string
  enabled?: boolean
  logMonitorType?: string
}

// 创建任务请求类型
export interface CreateTaskRequest {
  name: string
  type: string
  dataSourceId: number
  databaseName?: string
  tableName?: string
  enabled?: boolean
  concurrency?: number
  batchSize?: number
  retryCount?: number
  timeoutSeconds?: number
  scheduleType?: string
  scheduleExpression?: string
  interval?: number
  remark?: string
  config?: string
}

// 更新任务请求类型
export interface UpdateTaskRequest {
  id?: number
  name?: string
  type?: string
  dataSourceId?: number
  databaseName?: string
  tableName?: string
  enabled?: boolean
  concurrency?: number
  batchSize?: number
  retryCount?: number
  timeoutSeconds?: number
  scheduleType?: string
  scheduleExpression?: string
  interval?: number
  remark?: string
  config?: string
  status?: string
}

// 登录请求类型
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应类型
export interface LoginResponse {
  token: string
  user: UserInfo
}

// 分页请求类型
export interface PaginationRequest {
  page: number
  size: number
  sort?: string
  order?: 'asc' | 'desc'
}

// 分页响应类型
export interface PaginationResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
  pages: number
}

// 错误响应类型
export interface ErrorResponse {
  code: number
  message: string
  data?: any
}

// 操作结果类型
export interface OperationResult {
  success: boolean
  message: string
  data?: any
}
