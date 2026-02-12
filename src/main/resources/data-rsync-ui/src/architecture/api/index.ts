// API请求管理

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken, removeToken, getXsrfToken } from '@/utils/tokenManager'
import { ApiResponse } from '@/architecture/types'
import { envConfig, apiConfig } from '@/architecture/config'
import { ERROR_CODE, MESSAGE } from '@/architecture/constants'

// 创建axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: envConfig.apiBaseUrl,
  timeout: apiConfig.timeout,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config: AxiosRequestConfig): AxiosRequestConfig => {
    // 添加token认证头
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加XSRF保护token
    const xsrfToken = getXsrfToken()
    if (xsrfToken && config.headers) {
      config.headers['X-XSRF-TOKEN'] = xsrfToken
    }
    
    // 添加请求时间戳，防止缓存
    if (config.headers) {
      config.headers['X-Request-Time'] = Date.now().toString()
    }
    
    // 添加请求ID
    if (config.headers) {
      config.headers['X-Request-ID'] = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse): AxiosResponse => {
    // 统一处理响应格式
    const data = response.data
    if (data) {
      // 标准API响应格式
      if (typeof data === 'object' && 'code' in data) {
        return response
      }
      // 直接返回数据的情况，包装成标准格式
      response.data = {
        code: ERROR_CODE.SUCCESS,
        message: MESSAGE.SUCCESS.SAVE,
        data
      } as ApiResponse
      return response
    }
    // 空响应处理
    response.data = {
      code: ERROR_CODE.SUCCESS,
      message: MESSAGE.SUCCESS.SAVE,
      data: null
    } as ApiResponse
    return response
  },
  (error) => {
    console.error('API Error:', error)
    // 统一错误处理
    const errorMessage = error.response?.data?.message || error.message || MESSAGE.ERROR.NETWORK
    
    // 处理401错误
    if (error.response && error.response.status === ERROR_CODE.UNAUTHORIZED) {
      // 清除token并跳转到登录页面
      removeToken()
      window.location.href = '/login'
    }
    
    // 包装错误为标准格式
    return Promise.reject({
      code: error.response?.status || ERROR_CODE.INTERNAL_SERVER_ERROR,
      message: errorMessage,
      data: null
    } as ApiResponse)
  }
)

// API请求方法封装
class ApiService {
  // GET请求
  async get<T>(url: string, params?: any): Promise<ApiResponse<T>> {
    const response = await apiClient.get(url, { params })
    return response.data as ApiResponse<T>
  }

  // POST请求
  async post<T>(url: string, data?: any): Promise<ApiResponse<T>> {
    const response = await apiClient.post(url, data)
    return response.data as ApiResponse<T>
  }

  // PUT请求
  async put<T>(url: string, data?: any): Promise<ApiResponse<T>> {
    const response = await apiClient.put(url, data)
    return response.data as ApiResponse<T>
  }

  // DELETE请求
  async delete<T>(url: string, params?: any): Promise<ApiResponse<T>> {
    const response = await apiClient.delete(url, { params })
    return response.data as ApiResponse<T>
  }

  // PATCH请求
  async patch<T>(url: string, data?: any): Promise<ApiResponse<T>> {
    const response = await apiClient.patch(url, data)
    return response.data as ApiResponse<T>
  }

  // 上传文件
  async upload<T>(url: string, formData: FormData): Promise<ApiResponse<T>> {
    const response = await apiClient.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data as ApiResponse<T>
  }

  // 下载文件
  async download(url: string, params?: any): Promise<Blob> {
    const response = await apiClient.get(url, {
      params,
      responseType: 'blob'
    })
    return response.data
  }
}

// 导出API服务实例
export const apiService = new ApiService()

// 认证相关API
export const authApi = {
  // 登录
  login: (data: { username: string; password: string }) => apiService.post('/auth/login', data),
  
  // 登出
  logout: () => apiService.post('/auth/logout'),
  
  // 获取当前用户信息
  getCurrentUser: () => apiService.get('/auth/current-user')
}

// 数据源相关API
export const dataSourceApi = {
  // 获取数据源列表
  getList: () => apiService.get('/data-source'),
  
  // 获取数据源详情
  getDetail: (id: number) => apiService.get(`/data-source/${id}`),
  
  // 创建数据源
  create: (data: any) => apiService.post('/data-source', data),
  
  // 更新数据源
  update: (id: number, data: any) => apiService.put(`/data-source/${id}`, data),
  
  // 删除数据源
  delete: (id: number) => apiService.delete(`/data-source/${id}`),
  
  // 测试连接
  testConnection: (id: number) => apiService.post(`/data-source/${id}/test-connection`),
  
  // 启用/禁用数据源
  enable: (id: number, enabled: boolean) => apiService.put(`/data-source/${id}/enable`, { enabled }),
  
  // 检查健康状态
  checkHealth: (id: number) => apiService.get(`/data-source/${id}/health`),
  
  // 批量检查健康状态
  batchCheckHealth: () => apiService.post('/data-source/batch-check-health'),
  
  // 根据类型获取数据源
  getDataSourcesByType: (type: string) => apiService.get(`/data-source/type/${type}`),
  
  // 根据启用状态获取数据源
  getDataSourcesByEnabled: (enabled: boolean) => apiService.get(`/data-source/enabled/${enabled}`),
  
  // 模板相关
  getTemplates: () => apiService.get('/data-source/templates'),
  getTemplatesByType: (type: string) => apiService.get(`/data-source/templates/type/${type}`),
  getSystemTemplates: () => apiService.get('/data-source/templates/system'),
  createTemplate: (data: any) => apiService.post('/data-source/templates', data),
  updateTemplate: (id: number, data: any) => apiService.put(`/data-source/templates/${id}`, data),
  deleteTemplate: (id: number) => apiService.delete(`/data-source/templates/${id}`),
  initSystemTemplates: () => apiService.post('/data-source/templates/init-system'),
  
  // 诊断相关
  diagnose: (id: number) => apiService.post(`/data-source/${id}/diagnose`),
  getDiagnoseReports: (id: number) => apiService.get(`/data-source/${id}/diagnose-reports`),
  getLatestDiagnoseReport: (id: number) => apiService.get(`/data-source/${id}/latest-diagnose-report`)
}

// 任务相关API
export const taskApi = {
  // 获取任务列表
  getList: () => apiService.get('/tasks'),
  
  // 获取任务详情
  getDetail: (id: number) => apiService.get(`/tasks/${id}`),
  
  // 创建任务
  create: (data: any) => apiService.post('/tasks', data),
  
  // 更新任务
  update: (data: any) => apiService.put('/tasks', data),
  
  // 删除任务
  delete: (id: number) => apiService.delete(`/tasks/${id}`),
  
  // 触发任务执行
  trigger: (id: number) => apiService.post(`/tasks/${id}/trigger`),
  
  // 暂停任务
  pause: (id: number) => apiService.post(`/tasks/${id}/pause`),
  
  // 继续任务
  resume: (id: number) => apiService.post(`/tasks/${id}/resume`),
  
  // 回滚任务
  rollback: (id: number, rollbackPoint: string) => apiService.post(`/tasks/${id}/rollback`, { rollbackPoint }),
  
  // 获取任务版本
  getVersions: (id: number) => apiService.get(`/tasks/${id}/versions`),
  
  // 获取任务错误数据
  getErrorData: (id: number) => apiService.get(`/tasks/${id}/error-data`),
  
  // 保存任务节点
  saveTaskNodes: (taskId: number, nodes: any[]) => apiService.post(`/tasks/${taskId}/nodes`, nodes),
  
  // 保存任务连接
  saveTaskConnections: (taskId: number, connections: any[]) => apiService.post(`/tasks/${taskId}/connections`, connections),
  
  // 保存任务依赖
  saveTaskDependency: (taskId: number, dependency: any) => apiService.post(`/tasks/${taskId}/dependency`, dependency),
  
  // 获取任务节点
  getTaskNodes: (taskId: number) => apiService.get(`/tasks/${taskId}/nodes`),
  
  // 获取任务连接
  getTaskConnections: (taskId: number) => apiService.get(`/tasks/${taskId}/connections`),
  
  // 获取任务依赖
  getTaskDependencies: (taskId: number) => apiService.get(`/tasks/${taskId}/dependencies`),
  
  // 验证任务流程
  validateTaskFlow: (taskId: number) => apiService.post(`/tasks/${taskId}/validate-flow`)
}

// 监控相关API
export const monitorApi = {
  // 获取系统指标
  getMetrics: () => apiService.get('/monitor/metrics'),
  
  // 获取JVM指标
  getJvmMetrics: () => apiService.get('/monitor/metrics/jvm'),
  
  // 获取系统指标
  getSystemMetrics: () => apiService.get('/monitor/metrics/system'),
  
  // 获取数据源指标
  getDatasourceMetrics: () => apiService.get('/monitor/metrics/datasource'),
  
  // 获取任务指标
  getTaskMetrics: () => apiService.get('/monitor/metrics/task'),
  
  // 获取Milvus指标
  getMilvusMetrics: () => apiService.get('/monitor/metrics/milvus')
}

// Milvus相关API
export const milvusApi = {
  // 获取集合列表
  getCollections: () => apiService.get('/api/milvus/collections'),
  
  // 获取索引列表
  getIndexes: (collectionName: string) => apiService.get(`/api/milvus/indexes/${collectionName}`),
  
  // 创建索引
  createIndex: (collectionName: string, data: any) => apiService.post(`/api/milvus/indexes/${collectionName}`, data),
  
  // 删除索引
  deleteIndex: (collectionName: string, indexName: string) => apiService.delete(`/api/milvus/indexes/${collectionName}/${indexName}`),
  
  // 获取健康状态
  getHealth: () => apiService.get('/api/milvus/health'),
  
  // 优化集合
  optimizeCollection: (collectionName: string) => apiService.post(`/api/milvus/optimize/${collectionName}`),
  
  // 获取集合统计信息
  getCollectionStats: (collectionName: string) => apiService.get(`/api/milvus/collections/${collectionName}/stats`),
  
  // 创建集合
  createCollection: (data: any) => apiService.post('/api/milvus/collections', data),
  
  // 删除集合
  deleteCollection: (collectionName: string) => apiService.delete(`/api/milvus/collections/${collectionName}`),
  
  // 插入数据
  insertData: (collectionName: string, data: any) => apiService.post(`/api/milvus/collections/${collectionName}/insert`, data),
  
  // 删除数据
  deleteData: (collectionName: string, ids: any) => apiService.delete(`/api/milvus/collections/${collectionName}/delete`, { data: ids }),
  
  // 搜索数据
  searchData: (collectionName: string, data: any) => apiService.post(`/api/milvus/collections/${collectionName}/search`, data),
  
  // 批量操作
  batchOperation: (data: any) => apiService.post('/api/milvus/batch', data),
  
  // 检查一致性
  checkConsistency: (collectionName: string) => apiService.get(`/api/milvus/consistency/${collectionName}`)
}

// 系统相关API
export const systemApi = {
  // 获取用户列表
  getUsers: () => apiService.get('/api/system/users'),
  
  // 获取角色列表
  getRoles: () => apiService.get('/api/system/roles'),
  
  // 获取审计日志
  getAuditLogs: () => apiService.get('/api/system/logs'),
  
  // 创建用户
  createUser: (data: any) => apiService.post('/api/system/users', data),
  
  // 更新用户
  updateUser: (id: number, data: any) => apiService.put(`/api/system/users/${id}`, data),
  
  // 删除用户
  deleteUser: (id: number) => apiService.delete(`/api/system/users/${id}`),
  
  // 创建角色
  createRole: (data: any) => apiService.post('/api/system/roles', data),
  
  // 更新角色
  updateRole: (id: number, data: any) => apiService.put(`/api/system/roles/${id}`, data),
  
  // 删除角色
  deleteRole: (id: number) => apiService.delete(`/api/system/roles/${id}`),
  
  // 分配用户角色
  assignUserRoles: (userId: number, roleIds: any) => apiService.post(`/api/system/users/${userId}/roles`, roleIds),
  
  // 分配角色权限
  assignRolePermissions: (roleId: number, permissionIds: any) => apiService.post(`/api/system/roles/${roleId}/permissions`, permissionIds),
  
  // 获取用户角色
  getUserRoles: (userId: number) => apiService.get(`/api/system/users/${userId}/roles`),
  
  // 获取角色权限
  getRolePermissions: (roleId: number) => apiService.get(`/api/system/roles/${roleId}/permissions`),
  
  // 获取系统配置
  getSystemConfig: () => apiService.get('/api/system/config'),
  
  // 更新系统配置
  updateSystemConfig: (data: any) => apiService.put('/api/system/config', data),
  
  // 刷新系统缓存
  refreshSystemCache: () => apiService.post('/api/system/cache/refresh'),
  
  // 清理系统日志
  cleanSystemLogs: () => apiService.post('/api/system/logs/clean'),
  
  // 获取系统信息
  getSystemInfo: () => apiService.get('/api/system/info')
}

export default apiService
