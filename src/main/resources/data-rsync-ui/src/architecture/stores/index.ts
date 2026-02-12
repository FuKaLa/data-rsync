// 状态管理层

import { defineStore } from 'pinia'
import { UserInfo, DataSource, Task, Metrics } from '@/architecture/types'
import { authService, dataSourceService, taskService, monitorService } from '@/architecture/services'
import { getToken, setToken, removeToken } from '@/utils/tokenManager'

// 用户状态管理
export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null as UserInfo | null,
    token: getToken() || '',
    isLoggedIn: !!getToken(),
    isLoading: false,
    error: null as string | null
  }),

  getters: {
    getUserName: (state) => state.userInfo?.name || state.userInfo?.username || '',
    getUserRoles: (state) => state.userInfo?.roles || [],
    getUserPermissions: (state) => state.userInfo?.permissions || [],
    hasPermission: (state) => (permission: string) => {
      return state.userInfo?.permissions?.includes(permission) || false
    },
    hasRole: (state) => (role: string) => {
      return state.userInfo?.roles?.includes(role) || false
    }
  },

  actions: {
    async login(username: string, password: string) {
      this.isLoading = true
      this.error = null
      
      try {
        const data = await authService.login(username, password)
        if (data.token) {
          setToken(data.token)
          this.token = data.token
          this.isLoggedIn = true
          
          // 获取用户信息
          if (data.user) {
            this.userInfo = data.user
          } else {
            await this.fetchUserInfo()
          }
        }
        return data
      } catch (error: any) {
        this.error = error.message || '登录失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async logout() {
      this.isLoading = true
      
      try {
        await authService.logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        removeToken()
        this.token = ''
        this.userInfo = null
        this.isLoggedIn = false
        this.error = null
        this.isLoading = false
      }
    },

    async fetchUserInfo() {
      this.isLoading = true
      
      try {
        const userInfo = await authService.getCurrentUser()
        this.userInfo = userInfo
        return userInfo
      } catch (error: any) {
        this.error = error.message || '获取用户信息失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    clearError() {
      this.error = null
    }
  }
})

// 数据源状态管理
export const useDataSourceStore = defineStore('dataSource', {
  state: () => ({
    dataSources: [] as DataSource[],
    currentDataSource: null as DataSource | null,
    isLoading: false,
    error: null as string | null,
    total: 0
  }),

  getters: {
    getDataSourceById: (state) => (id: number) => {
      return state.dataSources.find(source => source.id === id) || null
    },
    getEnabledDataSources: (state) => {
      return state.dataSources.filter(source => source.enabled)
    },
    getDataSourceByType: (state) => (type: string) => {
      return state.dataSources.filter(source => source.type === type)
    }
  },

  actions: {
    async fetchDataSources() {
      this.isLoading = true
      this.error = null
      
      try {
        const dataSources = await dataSourceService.getDataSourceList()
        this.dataSources = dataSources
        this.total = dataSources.length
        return dataSources
      } catch (error: any) {
        this.error = error.message || '获取数据源列表失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchDataSourceDetail(id: number) {
      this.isLoading = true
      this.error = null
      
      try {
        const dataSource = await dataSourceService.getDataSourceDetail(id)
        this.currentDataSource = dataSource
        
        // 更新列表中的数据
        const index = this.dataSources.findIndex(source => source.id === id)
        if (index !== -1) {
          this.dataSources[index] = dataSource
        }
        
        return dataSource
      } catch (error: any) {
        this.error = error.message || '获取数据源详情失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async createDataSource(data: any) {
      this.isLoading = true
      this.error = null
      
      try {
        const dataSource = await dataSourceService.createDataSource(data)
        this.dataSources.push(dataSource)
        this.total++
        return dataSource
      } catch (error: any) {
        this.error = error.message || '创建数据源失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async updateDataSource(id: number, data: any) {
      this.isLoading = true
      this.error = null
      
      try {
        const dataSource = await dataSourceService.updateDataSource(id, data)
        
        // 更新列表中的数据
        const index = this.dataSources.findIndex(source => source.id === id)
        if (index !== -1) {
          this.dataSources[index] = dataSource
        }
        
        // 更新当前数据源
        if (this.currentDataSource?.id === id) {
          this.currentDataSource = dataSource
        }
        
        return dataSource
      } catch (error: any) {
        this.error = error.message || '更新数据源失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async deleteDataSource(id: number) {
      this.isLoading = true
      this.error = null
      
      try {
        await dataSourceService.deleteDataSource(id)
        
        // 从列表中移除
        this.dataSources = this.dataSources.filter(source => source.id !== id)
        this.total--
        
        // 清除当前数据源
        if (this.currentDataSource?.id === id) {
          this.currentDataSource = null
        }
      } catch (error: any) {
        this.error = error.message || '删除数据源失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    clearError() {
      this.error = null
    },

    clearCurrentDataSource() {
      this.currentDataSource = null
    }
  }
})

// 任务状态管理
export const useTaskStore = defineStore('task', {
  state: () => ({
    tasks: [] as Task[],
    currentTask: null as Task | null,
    isLoading: false,
    error: null as string | null,
    total: 0
  }),

  getters: {
    getTaskById: (state) => (id: number) => {
      return state.tasks.find(task => task.id === id) || null
    },
    getRunningTasks: (state) => {
      return state.tasks.filter(task => task.status === 'running')
    },
    getPendingTasks: (state) => {
      return state.tasks.filter(task => task.status === 'pending')
    },
    getCompletedTasks: (state) => {
      return state.tasks.filter(task => task.status === 'success')
    },
    getFailedTasks: (state) => {
      return state.tasks.filter(task => task.status === 'failed')
    }
  },

  actions: {
    async fetchTasks() {
      this.isLoading = true
      this.error = null
      
      try {
        const tasks = await taskService.getTaskList()
        this.tasks = tasks
        this.total = tasks.length
        return tasks
      } catch (error: any) {
        this.error = error.message || '获取任务列表失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchTaskDetail(id: number) {
      this.isLoading = true
      this.error = null
      
      try {
        const task = await taskService.getTaskDetail(id)
        this.currentTask = task
        
        // 更新列表中的数据
        const index = this.tasks.findIndex(t => t.id === id)
        if (index !== -1) {
          this.tasks[index] = task
        }
        
        return task
      } catch (error: any) {
        this.error = error.message || '获取任务详情失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async createTask(data: any) {
      this.isLoading = true
      this.error = null
      
      try {
        const task = await taskService.createTask(data)
        this.tasks.push(task)
        this.total++
        return task
      } catch (error: any) {
        this.error = error.message || '创建任务失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async updateTask(data: any) {
      this.isLoading = true
      this.error = null
      
      try {
        const task = await taskService.updateTask(data)
        
        // 更新列表中的数据
        const index = this.tasks.findIndex(t => t.id === data.id)
        if (index !== -1) {
          this.tasks[index] = task
        }
        
        // 更新当前任务
        if (this.currentTask?.id === data.id) {
          this.currentTask = task
        }
        
        return task
      } catch (error: any) {
        this.error = error.message || '更新任务失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async deleteTask(id: number) {
      this.isLoading = true
      this.error = null
      
      try {
        await taskService.deleteTask(id)
        
        // 从列表中移除
        this.tasks = this.tasks.filter(task => task.id !== id)
        this.total--
        
        // 清除当前任务
        if (this.currentTask?.id === id) {
          this.currentTask = null
        }
      } catch (error: any) {
        this.error = error.message || '删除任务失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async triggerTask(id: number) {
      this.isLoading = true
      this.error = null
      
      try {
        const result = await taskService.triggerTask(id)
        
        // 更新任务状态
        await this.fetchTaskDetail(id)
        
        return result
      } catch (error: any) {
        this.error = error.message || '触发任务失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async pauseTask(id: number) {
      this.isLoading = true
      this.error = null
      
      try {
        const result = await taskService.pauseTask(id)
        
        // 更新任务状态
        await this.fetchTaskDetail(id)
        
        return result
      } catch (error: any) {
        this.error = error.message || '暂停任务失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async resumeTask(id: number) {
      this.isLoading = true
      this.error = null
      
      try {
        const result = await taskService.resumeTask(id)
        
        // 更新任务状态
        await this.fetchTaskDetail(id)
        
        return result
      } catch (error: any) {
        this.error = error.message || '恢复任务失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    clearError() {
      this.error = null
    },

    clearCurrentTask() {
      this.currentTask = null
    }
  }
})

// 监控状态管理
export const useMonitorStore = defineStore('monitor', {
  state: () => ({
    systemMetrics: null as Metrics | null,
    jvmMetrics: null as any,
    taskMetrics: null as any,
    datasourceMetrics: null as any,
    milvusMetrics: null as any,
    isLoading: false,
    error: null as string | null,
    lastUpdated: null as string | null
  }),

  getters: {
    getCpuUsage: (state) => state.systemMetrics?.cpu.usage || 0,
    getMemoryUsage: (state) => state.systemMetrics?.memory.usage || 0,
    getDiskUsage: (state) => state.systemMetrics?.disk.usage || 0
  },

  actions: {
    async fetchSystemMetrics() {
      this.isLoading = true
      this.error = null
      
      try {
        const metrics = await monitorService.getSystemMetrics()
        this.systemMetrics = metrics
        this.lastUpdated = new Date().toISOString()
        return metrics
      } catch (error: any) {
        this.error = error.message || '获取系统指标失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchJvmMetrics() {
      this.isLoading = true
      
      try {
        const metrics = await monitorService.getJvmMetrics()
        this.jvmMetrics = metrics
        return metrics
      } catch (error: any) {
        this.error = error.message || '获取JVM指标失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchTaskMetrics() {
      this.isLoading = true
      
      try {
        const metrics = await monitorService.getTaskMetrics()
        this.taskMetrics = metrics
        return metrics
      } catch (error: any) {
        this.error = error.message || '获取任务指标失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchDatasourceMetrics() {
      this.isLoading = true
      
      try {
        const metrics = await monitorService.getDatasourceMetrics()
        this.datasourceMetrics = metrics
        return metrics
      } catch (error: any) {
        this.error = error.message || '获取数据源指标失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchMilvusMetrics() {
      this.isLoading = true
      
      try {
        const metrics = await monitorService.getMilvusMetrics()
        this.milvusMetrics = metrics
        return metrics
      } catch (error: any) {
        this.error = error.message || '获取Milvus指标失败'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchAllMetrics() {
      try {
        await Promise.all([
          this.fetchSystemMetrics(),
          this.fetchJvmMetrics(),
          this.fetchTaskMetrics(),
          this.fetchDatasourceMetrics(),
          this.fetchMilvusMetrics()
        ])
      } catch (error) {
        console.error('Fetch all metrics error:', error)
      }
    },

    clearError() {
      this.error = null
    }
  }
})

// 应用状态管理
export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    currentTheme: 'default',
    currentLanguage: 'zh-CN',
    breadcrumb: [] as Array<{ name: string; path: string }>,
    loading: false,
    notifications: [] as Array<{ id: string; type: 'success' | 'error' | 'warning' | 'info'; message: string; duration: number }>,
    modalVisible: false,
    modalContent: null as any
  }),

  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    setTheme(theme: string) {
      this.currentTheme = theme
    },

    setLanguage(language: string) {
      this.currentLanguage = language
    },

    setBreadcrumb(breadcrumb: Array<{ name: string; path: string }>) {
      this.breadcrumb = breadcrumb
    },

    showLoading() {
      this.loading = true
    },

    hideLoading() {
      this.loading = false
    },

    showNotification(type: 'success' | 'error' | 'warning' | 'info', message: string, duration = 3000) {
      const id = `notification_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      this.notifications.push({ id, type, message, duration })
      
      setTimeout(() => {
        this.removeNotification(id)
      }, duration)
    },

    removeNotification(id: string) {
      this.notifications = this.notifications.filter(notification => notification.id !== id)
    },

    openModal(content: any) {
      this.modalContent = content
      this.modalVisible = true
    },

    closeModal() {
      this.modalVisible = false
      this.modalContent = null
    }
  }
})

export default {
  useUserStore,
  useDataSourceStore,
  useTaskStore,
  useMonitorStore,
  useAppStore
}
