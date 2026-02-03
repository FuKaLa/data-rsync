import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)

  }
)

// 认证相关
export const authApi = {
  login: (data: { username: string; password: string }) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  getCurrentUser: () => api.get('/auth/current-user')
}

// 数据源相关
export const dataSourceApi = {
  getList: () => api.get('/data-source/list'),
  getDetail: (id: number) => api.get(`/data-source/detail/${id}`),
  create: (data: any) => api.post('/data-source/create', data),
  update: (id: number, data: any) => api.put(`/data-source/update/${id}`, data),
  delete: (id: number) => api.delete(`/data-source/delete/${id}`),
  testConnection: (id: number) => api.post(`/data-source/test-connection/${id}`)
}

// 任务相关
export const taskApi = {
  getList: () => api.get('/task/list'),
  getDetail: (id: number) => api.get(`/task/detail/${id}`),
  create: (data: any) => api.post('/task/create', data),
  update: (data: any) => api.put('/task/update', data),
  delete: (id: number) => api.delete(`/task/delete/${id}`),
  start: (id: number) => api.post(`/task/start/${id}`),
  pause: (id: number) => api.post(`/task/pause/${id}`),
  resume: (id: number) => api.post(`/task/resume/${id}`),
  rollback: (id: number, rollbackPoint: string) => api.post(`/task/rollback/${id}?rollbackPoint=${rollbackPoint}`),
  getVersions: (id: number) => api.get(`/task/versions/${id}`)
}

// 监控相关
export const monitorApi = {
  getMetrics: () => api.get('/monitor/metrics'),
  getBusinessMetrics: () => api.get('/monitor/business-metrics'),
  getDashboard: () => api.get('/monitor/dashboard'),
  getDatasourceMetrics: () => api.get('/monitor/datasource-metrics'),
  getTaskMetrics: () => api.get('/monitor/task-metrics'),
  getMilvusMetrics: () => api.get('/monitor/milvus-metrics'),
  getDelayMetrics: () => api.get('/monitor/delay-metrics'),
  getTopology: () => api.get('/monitor/topology')
}

// Milvus相关
export const milvusApi = {
  getCollections: () => api.get('/milvus/collections'),
  getIndexes: (collectionName: string) => api.get(`/milvus/indexes/${collectionName}`),
  createIndex: (collectionName: string, data: any) => api.post(`/milvus/indexes/${collectionName}`, data),
  deleteIndex: (collectionName: string, indexName: string) => api.delete(`/milvus/indexes/${collectionName}/${indexName}`),
  getHealth: () => api.get('/milvus/health'),
  optimizeCollection: (collectionName: string) => api.post(`/milvus/optimize/${collectionName}`)
}

// 系统相关
export const systemApi = {
  getUsers: () => api.get('/system/users'),
  getRoles: () => api.get('/system/roles'),
  getAuditLogs: () => api.get('/system/logs')
}

export default api