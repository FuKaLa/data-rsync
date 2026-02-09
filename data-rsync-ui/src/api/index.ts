import axios from 'axios'
import { getToken, removeToken, getXsrfToken } from '../utils/tokenManager'

// 定义API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

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
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

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
  error => {
    console.error('API Error:', error)
    // 统一错误处理
    const errorMessage = error.response?.data?.message || error.message || '网络请求失败'
    
    // 处理401错误
    if (error.response && error.response.status === 401) {
      // 清除token并跳转到登录页面
      removeToken()
      window.location.href = '/login'
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

// 认证相关
export const authApi = {
  // 登录接口 - /api/auth/login → 网关 → /auth/login
  login: (data: { username: string; password: string }): Promise<ApiResponse<string>> => api.post('/api/auth/login', data),
  // 注销接口 - /api/auth/logout → 网关 → /auth/logout
  logout: (): Promise<ApiResponse<void>> => api.post('/api/auth/logout'),
  // 当前用户接口 - /api/auth/current-user → 网关 → /auth/current-user
  getCurrentUser: (): Promise<ApiResponse<UserInfo>> => api.get('/api/auth/current-user')
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
  name: string
  description?: string
  dataSourceId: number
  targetDataSourceId?: number
  cronExpression?: string
  status: string
  createTime: string
  updateTime: string
  lastExecuteTime?: string
  nextExecuteTime?: string
}

// 创建任务请求类型
export interface CreateTaskRequest {
  name: string
  description?: string
  dataSourceId: number
  targetDataSourceId?: number
  cronExpression?: string
}

// 更新任务请求类型
export interface UpdateTaskRequest {
  name?: string
  description?: string
  dataSourceId?: number
  targetDataSourceId?: number
  cronExpression?: string
  status?: string
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
  getList: (): Promise<ApiResponse<DataSource[]>> => api.get('/api/data-source'),
  getDetail: (id: number): Promise<ApiResponse<DataSource>> => api.get(`/api/data-source/${id}`),
  create: (data: CreateDataSourceRequest): Promise<ApiResponse<DataSource>> => api.post('/api/data-source', data),
  update: (id: number, data: UpdateDataSourceRequest): Promise<ApiResponse<DataSource>> => api.put(`/api/data-source/${id}`, data),
  delete: (id: number): Promise<ApiResponse<void>> => api.delete(`/api/data-source/${id}`),
  testConnection: (id: number): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/api/data-source/${id}/test-connection`),
  enable: (id: number, enabled: boolean): Promise<ApiResponse<DataSource>> => api.put(`/api/data-source/${id}/enable?enabled=${enabled}`),
  checkHealth: (id: number): Promise<ApiResponse<{ status: string; message: string }>> => api.get(`/api/data-source/${id}/health`),
  batchCheckHealth: (): Promise<ApiResponse<Array<{ id: number; status: string; message: string }>>> => api.post('/api/data-source/batch-check-health'),
  getDataSourcesByType: (type: string): Promise<ApiResponse<DataSource[]>> => api.get(`/api/data-source/type/${type}`),
  getDataSourcesByEnabled: (enabled: boolean): Promise<ApiResponse<DataSource[]>> => api.get(`/api/data-source/enabled/${enabled}`),
  // 模板相关
  getTemplates: (): Promise<ApiResponse<DataSourceTemplate[]>> => api.get('/api/data-source/templates'),
  getTemplatesByType: (type: string): Promise<ApiResponse<DataSourceTemplate[]>> => api.get(`/api/data-source/templates/type/${type}`),
  getSystemTemplates: (): Promise<ApiResponse<DataSourceTemplate[]>> => api.get('/api/data-source/templates/system'),
  createTemplate: (data: CreateDataSourceTemplateRequest): Promise<ApiResponse<DataSourceTemplate>> => api.post('/api/data-source/templates', data),
  updateTemplate: (id: number, data: UpdateDataSourceTemplateRequest): Promise<ApiResponse<DataSourceTemplate>> => api.put(`/api/data-source/templates/${id}`, data),
  deleteTemplate: (id: number): Promise<ApiResponse<void>> => api.delete(`/api/data-source/templates/${id}`),
  initSystemTemplates: (): Promise<ApiResponse<void>> => api.post('/api/data-source/templates/init-system'),
  // 诊断相关
  diagnose: (id: number): Promise<ApiResponse<DataSourceDiagnoseReport>> => api.post(`/api/data-source/${id}/diagnose`),
  getDiagnoseReports: (id: number): Promise<ApiResponse<DataSourceDiagnoseReport[]>> => api.get(`/api/data-source/${id}/diagnose-reports`),
  getLatestDiagnoseReport: (id: number): Promise<ApiResponse<DataSourceDiagnoseReport>> => api.get(`/api/data-source/${id}/latest-diagnose-report`)
}

// 任务相关
export const taskApi = {
  getList: (): Promise<ApiResponse<Task[]>> => api.get('/api/tasks'),
  getDetail: (id: number): Promise<ApiResponse<Task>> => api.get(`/api/tasks/${id}`),
  create: (data: CreateTaskRequest): Promise<ApiResponse<Task>> => api.post('/api/tasks', data),
  update: (data: UpdateTaskRequest): Promise<ApiResponse<Task>> => api.put('/api/tasks', data),
  delete: (id: number): Promise<ApiResponse<void>> => api.delete(`/api/tasks/${id}`),
  start: (id: number): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/api/tasks/${id}/trigger`),
  pause: (id: number): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/api/tasks/${id}/pause`),
  resume: (id: number): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/api/tasks/${id}/resume`),
  rollback: (id: number, rollbackPoint: string): Promise<ApiResponse<{ success: boolean; message: string }>> => api.post(`/api/tasks/${id}/rollback?rollbackPoint=${rollbackPoint}`),
  getVersions: (id: number): Promise<ApiResponse<Array<{ version: string; time: string }>>> => api.get(`/api/tasks/${id}/versions`),
  getErrorData: (id: number): Promise<ApiResponse<Array<any>>> => api.get(`/api/tasks/${id}/error-data`)
}

// 监控相关
export const monitorApi = {
  getMetrics: (): Promise<ApiResponse<Metrics>> => api.get('/api/monitor/metrics'),
  getJvmMetrics: (): Promise<ApiResponse<any>> => api.get('/api/monitor/metrics/jvm'),
  getSystemMetrics: (): Promise<ApiResponse<any>> => api.get('/api/monitor/metrics/system'),
  getDatasourceMetrics: (): Promise<ApiResponse<any>> => api.get('/api/monitor/metrics/datasource'),
  getTaskMetrics: (): Promise<ApiResponse<any>> => api.get('/api/monitor/metrics/task'),
  getMilvusMetrics: (): Promise<ApiResponse<any>> => api.get('/api/monitor/metrics/milvus')
}

// Milvus相关 (暂时注释，因为milvus-sync服务没有Controller)
/*
export const milvusApi = {
  getCollections: () => api.get('/api/milvus/collections'),
  getIndexes: (collectionName: string) => api.get(`/api/milvus/indexes/${collectionName}`),
  createIndex: (collectionName: string, data: any) => api.post(`/api/milvus/indexes/${collectionName}`, data),
  deleteIndex: (collectionName: string, indexName: string) => api.delete(`/api/milvus/indexes/${collectionName}/${indexName}`),
  getHealth: () => api.get('/api/milvus/health'),
  optimizeCollection: (collectionName: string) => api.post(`/api/milvus/optimize/${collectionName}`)
}
*/

// 系统相关 (暂时注释，因为auth服务可能没有这些接口)
/*
export const systemApi = {
  getUsers: () => api.get('/api/system/users'),
  getRoles: () => api.get('/api/system/roles'),
  getAuditLogs: () => api.get('/api/system/logs')
}
*/

export default api