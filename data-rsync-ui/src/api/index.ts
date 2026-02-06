import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 添加token认证头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 检查响应格式，处理不同的后端响应格式
    const data = response.data
    if (data) {
      // 如果是标准的Response格式
      if (data.code !== undefined) {
        return data
      }
      // 如果是GlobalExceptionHandler返回的格式
      if (data.code && data.message) {
        return {
          code: data.code === 'SYSTEM_ERROR' ? 500 : 400,
          message: data.message,
          data: null
        }
      }
    }
    return data
  },
  error => {
    console.error('API Error:', error)
    // 处理401错误
    if (error.response && error.response.status === 401) {
      // 清除token并跳转到登录页面
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)

  }
)

// 认证相关
export const authApi = {
  // 登录接口 - /api/auth/login → 网关 → /auth/login
  login: (data: { username: string; password: string }) => api.post('/api/auth/login', data),
  // 注销接口 - /api/auth/logout → 网关 → /auth/logout
  logout: () => api.post('/api/auth/logout'),
  // 当前用户接口 - /api/auth/current-user → 网关 → /auth/current-user
  getCurrentUser: () => api.get('/api/auth/current-user')
}

// 数据源相关
export const dataSourceApi = {
  getList: () => api.get('/api/data-source'),
  getDetail: (id: number) => api.get(`/api/data-source/${id}`),
  create: (data: any) => api.post('/api/data-source', data),
  update: (id: number, data: any) => api.put(`/api/data-source/${id}`, data),
  delete: (id: number) => api.delete(`/api/data-source/${id}`),
  testConnection: (id: number) => api.post(`/api/data-source/${id}/test-connection`),
  enable: (id: number, enabled: boolean) => api.put(`/api/data-source/${id}/enable?enabled=${enabled}`),
  checkHealth: (id: number) => api.get(`/api/data-source/${id}/health`),
  batchCheckHealth: () => api.post('/api/data-source/batch-check-health'),
  getDataSourcesByType: (type: string) => api.get(`/api/data-source/type/${type}`),
  getDataSourcesByEnabled: (enabled: boolean) => api.get(`/api/data-source/enabled/${enabled}`),
  // 模板相关
  getTemplates: () => api.get('/api/data-source/templates'),
  getTemplatesByType: (type: string) => api.get(`/api/data-source/templates/type/${type}`),
  getSystemTemplates: () => api.get('/api/data-source/templates/system'),
  createTemplate: (data: any) => api.post('/api/data-source/templates', data),
  updateTemplate: (id: number, data: any) => api.put(`/api/data-source/templates/${id}`, data),
  deleteTemplate: (id: number) => api.delete(`/api/data-source/templates/${id}`),
  initSystemTemplates: () => api.post('/api/data-source/templates/init-system'),
  // 诊断相关
  diagnose: (id: number) => api.post(`/api/data-source/${id}/diagnose`),
  getDiagnoseReports: (id: number) => api.get(`/api/data-source/${id}/diagnose-reports`),
  getLatestDiagnoseReport: (id: number) => api.get(`/api/data-source/${id}/latest-diagnose-report`)
}

// 任务相关
export const taskApi = {
  getList: () => api.get('/api/tasks'),
  getDetail: (id: number) => api.get(`/api/tasks/${id}`),
  create: (data: any) => api.post('/api/tasks', data),
  update: (data: any) => api.put('/api/tasks', data),
  delete: (id: number) => api.delete(`/api/tasks/${id}`),
  start: (id: number) => api.post(`/api/tasks/${id}/trigger`),
  pause: (id: number) => api.post(`/api/tasks/${id}/pause`),
  resume: (id: number) => api.post(`/api/tasks/${id}/resume`),
  rollback: (id: number, rollbackPoint: string) => api.post(`/api/tasks/${id}/rollback?rollbackPoint=${rollbackPoint}`),
  getVersions: (id: number) => api.get(`/api/tasks/${id}/versions`),
  getErrorData: (id: number) => api.get(`/api/tasks/${id}/error-data`)
}

// 监控相关
export const monitorApi = {
  getMetrics: () => api.get('/api/monitor/metrics'),
  getJvmMetrics: () => api.get('/api/monitor/metrics/jvm'),
  getSystemMetrics: () => api.get('/api/monitor/metrics/system'),
  getDatasourceMetrics: () => api.get('/api/monitor/metrics/datasource'),
  getTaskMetrics: () => api.get('/api/monitor/metrics/task'),
  getMilvusMetrics: () => api.get('/api/monitor/metrics/milvus')
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