import axios from 'axios'
import type { AxiosRequestConfig, AxiosInstance, AxiosError } from 'axios'
import { getToken, removeToken, getXsrfToken } from '../utils/tokenManager'
import { envConfig } from '../architecture/config'

// 定义API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 定义请求配置类型
export interface ApiRequestConfig extends AxiosRequestConfig {
  retry?: number
  retryDelay?: number
  cancelToken?: any
}

const api = axios.create({
  baseURL: envConfig.apiBaseUrl,
  timeout: 30000
})

// 请求取消令牌管理
const cancelTokens: Map<string, any> = new Map()

// 生成请求键
const generateRequestKey = (config: AxiosRequestConfig): string => {
  const { method, url, params, data } = config
  return `${method}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`
}

// 取消请求
export const cancelRequest = (requestKey: string): void => {
  const cancelToken = cancelTokens.get(requestKey)
  if (cancelToken) {
    cancelToken()
    cancelTokens.delete(requestKey)
  }
}

// 取消所有请求
export const cancelAllRequests = (): void => {
  cancelTokens.forEach((cancelToken) => {
    cancelToken()
  })
  cancelTokens.clear()
}

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 添加token认证头
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加XSRF保护token
    const xsrfToken = getXsrfToken()
    if (xsrfToken) {
      config.headers['X-XSRF-TOKEN'] = xsrfToken
    }
    
    // 添加请求时间戳，防止缓存
    config.headers['X-Request-Time'] = Date.now().toString()
    
    // 添加请求取消令牌
    const requestKey = generateRequestKey(config)
    const source = axios.CancelToken.source()
    config.cancelToken = source.token
    cancelTokens.set(requestKey, source.cancel)
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 请求重试机制
const handleRetry = (error: AxiosError, apiInstance: AxiosInstance): Promise<any> => {
  const config = error.config as ApiRequestConfig
  if (!config || !config.retry || config.retryCount === undefined) return Promise.reject(error)
  
  // 检查是否已经重试过
  if (!config.retryCount) {
    config.retryCount = 0
  }
  
  // 检查是否超过重试次数
  if (config.retryCount >= config.retry) {
    return Promise.reject(error)
  }
  
  // 增加重试次数
  config.retryCount++
  
  // 计算重试延迟
  const retryDelay = config.retryDelay || 1000
  
  // 重试请求
  return new Promise(resolve => {
    setTimeout(() => {
      resolve(apiInstance(config))
    }, retryDelay)
  })
}

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 统一处理响应格式
    const data = response.data
    if (data) {
      // 标准API响应格式
      if (typeof data === 'object' && 'code' in data) {
        return response
      }
      // 直接返回数据的情况，包装成标准格式
      response.data = {
        code: 200,
        message: 'success',
        data
      } as ApiResponse
      return response
    }
    // 空响应处理
    response.data = {
      code: 200,
      message: 'success',
      data: null
    } as ApiResponse
    return response
  },
  async error => {
    // 使用控制台分组和更详细的错误信息
    console.group('API Error')
    console.error('Error:', error)
    console.error('Error Message:', error.message)
    console.error('Error Response:', error.response)
    console.error('Error Config:', error.config)
    console.groupEnd()
    
    // 处理网络错误和5xx错误，进行重试
    if (error.code === 'ECONNABORTED' || (error.response && error.response.status >= 500)) {
      try {
        return await handleRetry(error, api)
      } catch (retryError) {
        error = retryError
      }
    }
    
    // 统一错误处理
    const errorMessage = error.response?.data?.message || error.message || '网络请求失败'
    
    // 处理401错误
    if (error.response && error.response.status === 401) {
      // 清除token并跳转到登录页面
      removeToken()
      window.location.href = '/login'
    }
    
    // 处理403错误
    if (error.response && error.response.status === 403) {
      console.error('权限不足，请联系管理员')
    }
    
    // 处理404错误
    if (error.response && error.response.status === 404) {
      console.error('请求的资源不存在')
    }
    
    // 包装错误为标准格式
    return Promise.reject({
      code: error.response?.status || 500,
      message: errorMessage,
      data: null
    } as ApiResponse)
  }
)

// 用户信息类型
export interface UserInfo {
  username: string
  name: string
  email: string
  phone: string
  status: string
}

// 数据源类型
export interface DataSource {
  id: number
  tenantId?: number
  name: string
  dataSourceGroup?: string
  type: string
  host?: string
  port?: number
  databaseName?: string
  username?: string
  password?: string
  url: string
  enabled: boolean
  healthStatus: string
  lastFailureReason?: string
  heartbeatTime?: number
  lastHeartbeatTime?: string
  failureCount?: number
  description?: string
  driverClass?: string
  logMonitorType?: string
  connectionTimeout?: number
  connectionPoolConfig?: string
  deleted?: number
  createTime: string
  updateTime: string
  createBy?: string
  updateBy?: string
  version?: number
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
  overallStatus: string
  diagnoseDuration?: string
  network?: string
  authentication?: string
  logMonitor?: string
  connection?: string
  networkStatus?: string
  authenticationStatus?: string
  logMonitorStatus?: string
  connectionStatus?: string
}

// 认证相关
export const authApi = {
  // 登录接口
  login: (data: { username: string; password: string }): Promise<ApiResponse<string>> => api.post('/auth/login', data),
  // 注销接口
  logout: (): Promise<ApiResponse<void>> => api.post('/auth/logout'),
  // 当前用户接口
  getCurrentUser: (): Promise<ApiResponse<UserInfo>> => api.get('/auth/current-user')
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

// 创建数据源模板请求类型
export interface CreateDataSourceTemplateRequest {
  name: string
  dataSourceType: string
  driverClass: string
  logMonitorType: string
  defaultPort: number
  connectionTimeout: number
  description: string
}

// 更新数据源模板请求类型
export interface UpdateDataSourceTemplateRequest {
  name?: string
  driverClass?: string
  logMonitorType?: string
  defaultPort?: number
  connectionTimeout?: number
  description?: string
}

// 任务类型
export interface Task {
  id: number
  tenantId?: number
  name: string
  taskGroup?: string
  type: string
  syncStrategy?: string
  dataSourceId: number
  databaseName?: string
  tableName?: string
  status: string
  config?: string
  progress?: number
  startTime?: string
  endTime?: string
  errorMessage?: string
  breakpoint?: string
  createTime: string
  updateTime: string
  enabled?: boolean
  lastExecTime?: string
  execCount?: number
  nextExecTime?: string
  concurrency?: number
  batchSize?: number
  retryCount?: number
  timeoutSeconds?: number
  scheduleType?: string
  scheduleExpression?: string
  interval?: number
  remark?: string
  pauseTime?: string
  resumeTime?: string
  rollbackPoint?: string
  errorThreshold?: number
  deleted?: number
  createBy?: string
  updateBy?: string
  version?: number
}

// 创建任务请求类型
export interface CreateTaskRequest {
  name: string
  taskGroup?: string
  type: string
  syncStrategy?: string
  dataSourceId: number
  databaseName?: string
  tableName?: string
  config?: string
  enabled?: boolean
  concurrency?: number
  batchSize?: number
  retryCount?: number
  timeoutSeconds?: number
  scheduleType?: string
  scheduleExpression?: string
  interval?: number
  remark?: string
  errorThreshold?: number
  createBy?: string
}

// 更新任务请求类型
export interface UpdateTaskRequest {
  id?: number
  tenantId?: number
  name?: string
  taskGroup?: string
  type?: string
  syncStrategy?: string
  dataSourceId?: number
  databaseName?: string
  tableName?: string
  status?: string
  config?: string
  progress?: number
  errorMessage?: string
  breakpoint?: string
  enabled?: boolean
  concurrency?: number
  batchSize?: number
  retryCount?: number
  timeoutSeconds?: number
  scheduleType?: string
  scheduleExpression?: string
  interval?: number
  remark?: string
  rollbackPoint?: string
  errorThreshold?: number
  updateBy?: string
  version?: number
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

// 数据源相关
export const dataSourceApi = {
  getList: (): Promise<ApiResponse<DataSource[]>> => api.get('/data-source'),
  getDetail: (id: number): Promise<ApiResponse<DataSource>> => api.get(`/data-source/${id}`),
  create: (data: CreateDataSourceRequest): Promise<ApiResponse<DataSource>> => api.post('/data-source', data),
  update: (id: number, data: UpdateDataSourceRequest): Promise<ApiResponse<DataSource>> => api.put(`/data-source/${id}`, data),
  delete: (id: number): Promise<ApiResponse<void>> => api.delete(`/data-source/${id}`),
  testConnection: (id: number): Promise<ApiResponse<boolean>> => api.post(`/data-source/${id}/test-connection`),
  testNewConnection: (data: CreateDataSourceRequest): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post('/data-source/test-connection', data),
  enable: (id: number, enabled: boolean): Promise<ApiResponse<DataSource>> => api.put(`/data-source/${id}/enable?enabled=${enabled}`),
  checkHealth: (id: number): Promise<ApiResponse<{ healthy: boolean; status: string; responseTimeMs: number }>> => api.get(`/data-source/${id}/health`),
  batchCheckHealth: (): Promise<ApiResponse<Array<{ id: number; status: string; message: string }>>> => api.post('/data-source/batch-check-health'),
  getDataSourcesByType: (type: string): Promise<ApiResponse<DataSource[]>> => api.get(`/data-source/type/${type}`),
  getDataSourcesByEnabled: (enabled: boolean): Promise<ApiResponse<DataSource[]>> => api.get(`/data-source/enabled/${enabled}`),
  // 模板相关
  getTemplates: (): Promise<ApiResponse<DataSourceTemplate[]>> => api.get('/data-source/templates'),
  getTemplatesByType: (type: string): Promise<ApiResponse<DataSourceTemplate[]>> => api.get(`/data-source/templates/type/${type}`),
  getSystemTemplates: (): Promise<ApiResponse<DataSourceTemplate[]>> => api.get('/data-source/templates/system'),
  createTemplate: (data: CreateDataSourceTemplateRequest): Promise<ApiResponse<DataSourceTemplate>> => api.post('/data-source/templates', data),
  updateTemplate: (id: number, data: UpdateDataSourceTemplateRequest): Promise<ApiResponse<DataSourceTemplate>> => api.put(`/data-source/templates/${id}`, data),
  deleteTemplate: (id: number): Promise<ApiResponse<void>> => api.delete(`/data-source/templates/${id}`),
  initSystemTemplates: (): Promise<ApiResponse<void>> => api.post('/data-source/templates/init-system'),
  // 诊断相关
  diagnose: (id: number): Promise<ApiResponse<DataSourceDiagnoseReport>> => api.post(`/data-source/${id}/diagnose`),
  getDiagnoseReports: (id: number): Promise<ApiResponse<DataSourceDiagnoseReport[]>> => api.get(`/data-source/${id}/diagnose-reports`),
  getLatestDiagnoseReport: (id: number): Promise<ApiResponse<DataSourceDiagnoseReport>> => api.get(`/data-source/${id}/latest-diagnose-report`)
}

// 任务相关
export const taskApi = {
  getList: (): Promise<ApiResponse<Task[]>> => api.get('/tasks'),
  getDetail: (id: number): Promise<ApiResponse<Task>> => api.get(`/tasks/${id}`),
  create: (data: CreateTaskRequest): Promise<ApiResponse<Task>> => api.post('/tasks', data),
  update: (data: UpdateTaskRequest): Promise<ApiResponse<Task>> => api.put('/tasks', data),
  delete: (id: number): Promise<ApiResponse<void>> => api.delete(`/tasks/${id}`),
  start: (id: number): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/tasks/${id}/trigger`),
  pause: (id: number): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/tasks/${id}/pause`),
  resume: (id: number): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/tasks/${id}/resume`),
  rollback: (id: number, rollbackPoint: string): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/tasks/${id}/rollback?rollbackPoint=${rollbackPoint}`),
  getVersions: (id: number): Promise<ApiResponse<Array<{ version: string; time: string }>>> => api.get(`/tasks/${id}/versions`),
  getErrorData: (id: number): Promise<ApiResponse<Array<any>>> => api.get(`/tasks/${id}/error-data`)
}

// 监控相关
export const monitorApi = {
  getAllMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics'),
  getJvmMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/jvm'),
  getSystemMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/system'),
  getDatasourceMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/datasource'),
  getTaskMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/task'),
  getMilvusMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/milvus'),
  getApiMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/api'),
  getRedisMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/redis'),
  getThreadPoolMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/thread-pool'),
  getErrorMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/error'),
  getBusinessProcessMetrics: (): Promise<ApiResponse<any>> => api.get('/monitor/metrics/business-process'),
  sendAlert: (severity: string, message: string, metrics: any): Promise<ApiResponse<boolean>> => api.post('/monitor/alert/send', metrics, {
    params: { severity, message }
  })
}

// Milvus相关
export const milvusApi = {
  getCollections: () => api.get('/milvus/collections'),
  getIndexes: (collectionName: string) => api.get(`/milvus/indexes/${collectionName}`),
  createIndex: (collectionName: string, data: any) => api.post(`/milvus/indexes/${collectionName}`, data),
  deleteIndex: (collectionName: string, indexName: string) => api.delete(`/milvus/indexes/${collectionName}/${indexName}`),
  getHealth: () => api.get('/milvus/health'),
  optimizeCollection: (collectionName: string) => api.post(`/milvus/optimize/${collectionName}`),
  getCollectionStats: (collectionName: string) => api.get(`/milvus/collections/${collectionName}/stats`),
  createCollection: (data: any) => api.post('/milvus/collections', data),
  deleteCollection: (collectionName: string) => api.delete(`/milvus/collections/${collectionName}`),
  insertData: (collectionName: string, data: any) => api.post(`/milvus/collections/${collectionName}/insert`, data),
  deleteData: (collectionName: string, ids: any) => api.delete(`/milvus/collections/${collectionName}/delete`, { data: ids }),
  searchData: (collectionName: string, data: any) => api.post(`/milvus/collections/${collectionName}/search`, data),
  batchOperation: (data: any) => api.post('/milvus/batch', data),
  checkConsistency: (collectionName: string) => api.get(`/milvus/consistency/${collectionName}`),
  rebuildIndex: (collectionName: string, indexName: string) => api.post(`/milvus/indexes/${collectionName}/rebuild`, { indexName })
}

// 系统相关
export const systemApi = {
  getUsers: () => api.get('/system/users'),
  getRoles: () => api.get('/system/roles'),
  getAuditLogs: () => api.get('/system/logs'),
  createUser: (data: any) => api.post('/system/users', data),
  updateUser: (id: number, data: any) => api.put(`/system/users/${id}`, data),
  deleteUser: (id: number) => api.delete(`/system/users/${id}`),
  createRole: (data: any) => api.post('/system/roles', data),
  updateRole: (id: number, data: any) => api.put(`/system/roles/${id}`, data),
  deleteRole: (id: number) => api.delete(`/system/roles/${id}`),
  assignUserRoles: (userId: number, roleIds: any) => api.post(`/system/users/${userId}/roles`, roleIds),
  assignRolePermissions: (roleId: number, permissionIds: any) => api.post(`/system/roles/${roleId}/permissions`, permissionIds),
  getUserRoles: (userId: number) => api.get(`/system/users/${userId}/roles`),
  getRolePermissions: (roleId: number) => api.get(`/system/roles/${roleId}/permissions`),
  getSystemConfig: () => api.get('/system/config'),
  updateSystemConfig: (data: any) => api.put('/system/config', data),
  refreshSystemCache: () => api.post('/system/cache/refresh'),
  cleanSystemLogs: () => api.post('/system/logs/clean'),
  getSystemInfo: () => api.get('/system/info')
}

export default api