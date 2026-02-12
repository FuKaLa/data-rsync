// 业务服务层

import { authApi, dataSourceApi, taskApi, monitorApi, milvusApi, systemApi } from '@/architecture/api'
import { UserInfo, DataSource, Task, Metrics, MilvusIndex, DataSourceTemplate } from '@/architecture/types'
import { ERROR_CODE, MESSAGE } from '@/architecture/constants'

// 认证服务
export class AuthService {
  // 登录
  async login(username: string, password: string) {
    const response = await authApi.login({ username, password })
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.LOGIN)
    }
  }

  // 登出
  async logout() {
    const response = await authApi.logout()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.LOGOUT)
    }
  }

  // 获取当前用户信息
  async getCurrentUser(): Promise<UserInfo> {
    const response = await authApi.getCurrentUser()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as UserInfo
    } else {
      throw new Error(response.message || MESSAGE.ERROR.AUTH)
    }
  }
}

// 数据源服务
export class DataSourceService {
  // 获取数据源列表
  async getDataSourceList(): Promise<DataSource[]> {
    const response = await dataSourceApi.getList()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as DataSource[]
    } else {
      throw new Error(response.message || MESSAGE.ERROR.DATA_SOURCE)
    }
  }

  // 获取数据源详情
  async getDataSourceDetail(id: number): Promise<DataSource> {
    const response = await dataSourceApi.getDetail(id)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as DataSource
    } else {
      throw new Error(response.message || MESSAGE.ERROR.DATA_SOURCE)
    }
  }

  // 创建数据源
  async createDataSource(data: any): Promise<DataSource> {
    const response = await dataSourceApi.create(data)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as DataSource
    } else {
      throw new Error(response.message || MESSAGE.ERROR.CREATE)
    }
  }

  // 更新数据源
  async updateDataSource(id: number, data: any): Promise<DataSource> {
    const response = await dataSourceApi.update(id, data)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as DataSource
    } else {
      throw new Error(response.message || MESSAGE.ERROR.UPDATE)
    }
  }

  // 删除数据源
  async deleteDataSource(id: number): Promise<void> {
    const response = await dataSourceApi.delete(id)
    if (response.code !== ERROR_CODE.SUCCESS) {
      throw new Error(response.message || MESSAGE.ERROR.DELETE)
    }
  }

  // 测试连接
  async testConnection(id: number): Promise<{ success: boolean; message: string }> {
    const response = await dataSourceApi.testConnection(id)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.TEST_CONNECTION)
    }
  }

  // 检查健康状态
  async checkHealth(id: number): Promise<{ status: string; message: string }> {
    const response = await dataSourceApi.checkHealth(id)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.HEALTH_CHECK)
    }
  }

  // 获取数据源模板
  async getDataSourceTemplates(): Promise<DataSourceTemplate[]> {
    const response = await dataSourceApi.getTemplates()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as DataSourceTemplate[]
    } else {
      throw new Error(response.message || MESSAGE.ERROR.DATA_SOURCE)
    }
  }
}

// 任务服务
export class TaskService {
  // 获取任务列表
  async getTaskList(): Promise<Task[]> {
    const response = await taskApi.getList()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as Task[]
    } else {
      throw new Error(response.message || MESSAGE.ERROR.TASK)
    }
  }

  // 获取任务详情
  async getTaskDetail(id: number): Promise<Task> {
    const response = await taskApi.getDetail(id)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as Task
    } else {
      throw new Error(response.message || MESSAGE.ERROR.TASK)
    }
  }

  // 创建任务
  async createTask(data: any): Promise<Task> {
    const response = await taskApi.create(data)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as Task
    } else {
      throw new Error(response.message || MESSAGE.ERROR.CREATE)
    }
  }

  // 更新任务
  async updateTask(data: any): Promise<Task> {
    const response = await taskApi.update(data)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as Task
    } else {
      throw new Error(response.message || MESSAGE.ERROR.UPDATE)
    }
  }

  // 删除任务
  async deleteTask(id: number): Promise<void> {
    const response = await taskApi.delete(id)
    if (response.code !== ERROR_CODE.SUCCESS) {
      throw new Error(response.message || MESSAGE.ERROR.DELETE)
    }
  }

  // 触发任务执行
  async triggerTask(id: number): Promise<{ success: boolean; message: string }> {
    const response = await taskApi.trigger(id)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.TASK_TRIGGER)
    }
  }

  // 暂停任务
  async pauseTask(id: number): Promise<{ success: boolean; message: string }> {
    const response = await taskApi.pause(id)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.TASK_PAUSE)
    }
  }

  // 继续任务
  async resumeTask(id: number): Promise<{ success: boolean; message: string }> {
    const response = await taskApi.resume(id)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.TASK_RESUME)
    }
  }

  // 回滚任务
  async rollbackTask(id: number, rollbackPoint: string): Promise<{ success: boolean; message: string }> {
    const response = await taskApi.rollback(id, rollbackPoint)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.TASK_ROLLBACK)
    }
  }
}

// 监控服务
export class MonitorService {
  // 获取系统指标
  async getSystemMetrics(): Promise<Metrics> {
    const response = await monitorApi.getMetrics()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as Metrics
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MONITOR)
    }
  }

  // 获取JVM指标
  async getJvmMetrics() {
    const response = await monitorApi.getJvmMetrics()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MONITOR)
    }
  }

  // 获取任务指标
  async getTaskMetrics() {
    const response = await monitorApi.getTaskMetrics()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MONITOR)
    }
  }

  // 获取数据源指标
  async getDatasourceMetrics() {
    const response = await monitorApi.getDatasourceMetrics()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MONITOR)
    }
  }

  // 获取Milvus指标
  async getMilvusMetrics() {
    const response = await monitorApi.getMilvusMetrics()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MONITOR)
    }
  }
}

// Milvus服务
export class MilvusService {
  // 获取集合列表
  async getCollections() {
    const response = await milvusApi.getCollections()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MILVUS)
    }
  }

  // 获取索引列表
  async getIndexes(collectionName: string) {
    const response = await milvusApi.getIndexes(collectionName)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as MilvusIndex[]
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MILVUS)
    }
  }

  // 创建索引
  async createIndex(collectionName: string, data: any) {
    const response = await milvusApi.createIndex(collectionName, data)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data as MilvusIndex
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MILVUS)
    }
  }

  // 删除索引
  async deleteIndex(collectionName: string, indexName: string) {
    const response = await milvusApi.deleteIndex(collectionName, indexName)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MILVUS)
    }
  }

  // 获取健康状态
  async getHealth() {
    const response = await milvusApi.getHealth()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MILVUS)
    }
  }

  // 优化集合
  async optimizeCollection(collectionName: string) {
    const response = await milvusApi.optimizeCollection(collectionName)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.MILVUS)
    }
  }
}

// 系统服务
export class SystemService {
  // 获取用户列表
  async getUserList() {
    const response = await systemApi.getUsers()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.SYSTEM)
    }
  }

  // 获取角色列表
  async getRoleList() {
    const response = await systemApi.getRoles()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.SYSTEM)
    }
  }

  // 获取审计日志
  async getAuditLogs() {
    const response = await systemApi.getAuditLogs()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.SYSTEM)
    }
  }

  // 获取系统配置
  async getSystemConfig() {
    const response = await systemApi.getSystemConfig()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.SYSTEM)
    }
  }

  // 更新系统配置
  async updateSystemConfig(data: any) {
    const response = await systemApi.updateSystemConfig(data)
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.SYSTEM)
    }
  }

  // 获取系统信息
  async getSystemInfo() {
    const response = await systemApi.getSystemInfo()
    if (response.code === ERROR_CODE.SUCCESS) {
      return response.data
    } else {
      throw new Error(response.message || MESSAGE.ERROR.SYSTEM)
    }
  }
}

// 导出服务实例
export const authService = new AuthService()
export const dataSourceService = new DataSourceService()
export const taskService = new TaskService()
export const monitorService = new MonitorService()
export const milvusService = new MilvusService()
export const systemService = new SystemService()

export default {
  auth: authService,
  dataSource: dataSourceService,
  task: taskService,
  monitor: monitorService,
  milvus: milvusService,
  system: systemService
}
