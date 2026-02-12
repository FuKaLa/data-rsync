<template>
  <div class="system-config">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统配置管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="loadConfig">刷新配置</el-button>
            <el-button @click="saveConfig" :loading="saving">保存配置</el-button>
            <el-button @click="resetConfig">重置</el-button>
            <el-button @click="refreshCache" type="info">刷新缓存</el-button>
            <el-button @click="cleanLogs" type="warning">清理日志</el-button>
          </div>
        </div>
      </template>
      
      <div class="config-content">
        <!-- 基本配置 -->
        <el-collapse v-model="activeCollapse">
          <el-collapse-item title="基本配置" name="basic">
            <el-form :model="configForm" label-width="160px" class="mb-4">
              <el-form-item label="应用名称">
                <el-input v-model="configForm.appName" placeholder="请输入应用名称" />
              </el-form-item>
              <el-form-item label="应用版本">
                <el-input v-model="configForm.appVersion" placeholder="请输入应用版本" />
              </el-form-item>
              <el-form-item label="最大连接数">
                <el-input-number v-model="configForm.maxConnections" :min="1" :max="1000" placeholder="请输入最大连接数" />
              </el-form-item>
              <el-form-item label="任务线程池大小">
                <el-input-number v-model="configForm.taskThreadPoolSize" :min="1" :max="100" placeholder="请输入线程池大小" />
              </el-form-item>
            </el-form>
          </el-collapse-item>
          
          <!-- 数据同步配置 -->
          <el-collapse-item title="数据同步配置" name="sync">
            <el-form :model="configForm" label-width="160px" class="mb-4">
              <el-form-item label="数据同步批量大小">
                <el-input-number v-model="configForm.dataSyncBatchSize" :min="1" :max="10000" placeholder="请输入批量大小" />
              </el-form-item>
              <el-form-item label="同步重试次数">
                <el-input-number v-model="configForm.syncRetryCount" :min="0" :max="10" placeholder="请输入重试次数" />
              </el-form-item>
              <el-form-item label="同步超时时间(ms)">
                <el-input-number v-model="configForm.syncTimeout" :min="1000" :max="600000" placeholder="请输入超时时间" />
              </el-form-item>
            </el-form>
          </el-collapse-item>
          
          <!-- Milvus配置 -->
          <el-collapse-item title="Milvus配置" name="milvus">
            <el-form :model="configForm" label-width="160px" class="mb-4">
              <el-form-item label="Milvus连接超时(ms)">
                <el-input-number v-model="configForm.milvusConnectionTimeout" :min="1000" :max="60000" placeholder="请输入超时时间" />
              </el-form-item>
              <el-form-item label="Milvus批量写入大小">
                <el-input-number v-model="configForm.milvusBatchSize" :min="1" :max="1000" placeholder="请输入批量大小" />
              </el-form-item>
              <el-form-item label="Milvus索引构建阈值">
                <el-input-number v-model="configForm.milvusIndexBuildThreshold" :min="1000" :max="100000" placeholder="请输入索引构建阈值" />
              </el-form-item>
            </el-form>
          </el-collapse-item>
          
          <!-- 监控配置 -->
          <el-collapse-item title="监控配置" name="monitor">
            <el-form :model="configForm" label-width="160px" class="mb-4">
              <el-form-item label="指标收集间隔(ms)">
                <el-input-number v-model="configForm.metricsCollectionInterval" :min="1000" :max="3600000" placeholder="请输入收集间隔" />
              </el-form-item>
              <el-form-item label="告警阈值检查间隔(ms)">
                <el-input-number v-model="configForm.alertCheckInterval" :min="1000" :max="3600000" placeholder="请输入检查间隔" />
              </el-form-item>
              <el-form-item label="监控数据保留天数">
                <el-input-number v-model="configForm.metricsRetentionDays" :min="1" :max="365" placeholder="请输入保留天数" />
              </el-form-item>
            </el-form>
          </el-collapse-item>
          
          <!-- 缓存配置 -->
          <el-collapse-item title="缓存配置" name="cache">
            <el-form :model="configForm" label-width="160px" class="mb-4">
              <el-form-item label="缓存过期时间(秒)">
                <el-input-number v-model="configForm.cacheExpirationSeconds" :min="1" :max="86400" placeholder="请输入过期时间" />
              </el-form-item>
              <el-form-item label="缓存最大容量">
                <el-input-number v-model="configForm.cacheMaxSize" :min="100" :max="100000" placeholder="请输入最大容量" />
              </el-form-item>
              <el-form-item label="缓存清理间隔(秒)">
                <el-input-number v-model="configForm.cacheCleanupInterval" :min="1" :max="3600" placeholder="请输入清理间隔" />
              </el-form-item>
            </el-form>
          </el-collapse-item>
          
          <!-- 日志配置 -->
          <el-collapse-item title="日志配置" name="log">
            <el-form :model="configForm" label-width="160px" class="mb-4">
              <el-form-item label="日志级别">
                <el-select v-model="configForm.logLevel" placeholder="请选择日志级别">
                  <el-option label="DEBUG" value="DEBUG" />
                  <el-option label="INFO" value="INFO" />
                  <el-option label="WARN" value="WARN" />
                  <el-option label="ERROR" value="ERROR" />
                </el-select>
              </el-form-item>
              <el-form-item label="日志文件大小限制(MB)">
                <el-input-number v-model="configForm.logFileSizeLimit" :min="1" :max="1024" placeholder="请输入文件大小限制" />
              </el-form-item>
              <el-form-item label="日志文件保留数量">
                <el-input-number v-model="configForm.logFileRetentionCount" :min="1" :max="100" placeholder="请输入保留数量" />
              </el-form-item>
            </el-form>
          </el-collapse-item>
        </el-collapse>
      </div>
      
      <!-- 系统信息 -->
      <div class="system-info mt-8">
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="info-header">
              <span>系统信息</span>
              <el-button size="small" @click="loadSystemInfo">刷新信息</el-button>
            </div>
          </template>
          
          <div class="info-content">
            <el-descriptions :column="3">
              <el-descriptions-item label="Java版本">{{ systemInfo.javaVersion }}</el-descriptions-item>
              <el-descriptions-item label="操作系统">{{ systemInfo.osName }}</el-descriptions-item>
              <el-descriptions-item label="操作系统版本">{{ systemInfo.osVersion }}</el-descriptions-item>
              <el-descriptions-item label="可用处理器">{{ systemInfo.availableProcessors }}</el-descriptions-item>
              <el-descriptions-item label="最大内存">{{ systemInfo.maxMemory }}</el-descriptions-item>
              <el-descriptions-item label="总内存">{{ systemInfo.totalMemory }}</el-descriptions-item>
              <el-descriptions-item label="可用内存">{{ systemInfo.freeMemory }}</el-descriptions-item>
              <el-descriptions-item label="系统时间">{{ systemInfo.systemTime }}</el-descriptions-item>
              <el-descriptions-item label="应用启动时间">{{ systemInfo.startupTime }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { systemApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 折叠面板
const activeCollapse = ref(['basic', 'sync', 'milvus', 'monitor', 'cache', 'log'])

// 配置表单
const configForm = reactive({
  appName: '',
  appVersion: '',
  maxConnections: 0,
  taskThreadPoolSize: 0,
  dataSyncBatchSize: 0,
  milvusConnectionTimeout: 0,
  metricsCollectionInterval: 0,
  alertCheckInterval: 30000,
  metricsRetentionDays: 7,
  cacheExpirationSeconds: 3600,
  cacheMaxSize: 10000,
  cacheCleanupInterval: 600,
  logLevel: 'INFO',
  logFileSizeLimit: 100,
  logFileRetentionCount: 10,
  syncRetryCount: 3,
  syncTimeout: 30000,
  milvusBatchSize: 100,
  milvusIndexBuildThreshold: 10000
})

// 系统信息
const systemInfo = reactive({
  javaVersion: '',
  osName: '',
  osVersion: '',
  availableProcessors: '',
  maxMemory: '',
  totalMemory: '',
  freeMemory: '',
  systemTime: '',
  startupTime: ''
})

// 加载状态
const saving = ref(false)

onMounted(() => {
  loadConfig()
  loadSystemInfo()
})

// 加载系统配置
const loadConfig = async () => {
  try {
    const response = await systemApi.getSystemConfig()
    const config = response.data
    Object.assign(configForm, {
      appName: config.appName || 'Data-Rsync',
      appVersion: config.appVersion || '1.0.0',
      maxConnections: config.maxConnections || 100,
      taskThreadPoolSize: config.taskThreadPoolSize || 20,
      dataSyncBatchSize: config.dataSyncBatchSize || 1000,
      milvusConnectionTimeout: config.milvusConnectionTimeout || 30000,
      metricsCollectionInterval: config.metricsCollectionInterval || 60000,
      alertCheckInterval: config.alertCheckInterval || 30000,
      metricsRetentionDays: config.metricsRetentionDays || 7,
      cacheExpirationSeconds: config.cacheExpirationSeconds || 3600,
      cacheMaxSize: config.cacheMaxSize || 10000,
      cacheCleanupInterval: config.cacheCleanupInterval || 600,
      logLevel: config.logLevel || 'INFO',
      logFileSizeLimit: config.logFileSizeLimit || 100,
      logFileRetentionCount: config.logFileRetentionCount || 10,
      syncRetryCount: config.syncRetryCount || 3,
      syncTimeout: config.syncTimeout || 30000,
      milvusBatchSize: config.milvusBatchSize || 100,
      milvusIndexBuildThreshold: config.milvusIndexBuildThreshold || 10000
    })
    ElMessage.success('配置加载成功')
  } catch (error) {
    console.error('Failed to load config:', error)
    ElMessage.error('加载配置失败')
  }
}

// 保存系统配置
const saveConfig = async () => {
  try {
    saving.value = true
    const response = await systemApi.updateSystemConfig({
      appName: configForm.appName,
      appVersion: configForm.appVersion,
      maxConnections: configForm.maxConnections,
      taskThreadPoolSize: configForm.taskThreadPoolSize,
      dataSyncBatchSize: configForm.dataSyncBatchSize,
      milvusConnectionTimeout: configForm.milvusConnectionTimeout,
      metricsCollectionInterval: configForm.metricsCollectionInterval,
      alertCheckInterval: configForm.alertCheckInterval,
      metricsRetentionDays: configForm.metricsRetentionDays,
      cacheExpirationSeconds: configForm.cacheExpirationSeconds,
      cacheMaxSize: configForm.cacheMaxSize,
      cacheCleanupInterval: configForm.cacheCleanupInterval,
      logLevel: configForm.logLevel,
      logFileSizeLimit: configForm.logFileSizeLimit,
      logFileRetentionCount: configForm.logFileRetentionCount,
      syncRetryCount: configForm.syncRetryCount,
      syncTimeout: configForm.syncTimeout,
      milvusBatchSize: configForm.milvusBatchSize,
      milvusIndexBuildThreshold: configForm.milvusIndexBuildThreshold
    })
    ElMessage.success('配置保存成功')
  } catch (error) {
    console.error('Failed to save config:', error)
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

// 重置配置
const resetConfig = () => {
  ElMessageBox.confirm('确定要重置配置吗？这将恢复默认值', '重置确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      Object.assign(configForm, {
        appName: 'Data-Rsync',
        appVersion: '1.0.0',
        maxConnections: 100,
        taskThreadPoolSize: 20,
        dataSyncBatchSize: 1000,
        milvusConnectionTimeout: 30000,
        metricsCollectionInterval: 60000,
        alertCheckInterval: 30000,
        metricsRetentionDays: 7,
        cacheExpirationSeconds: 3600,
        cacheMaxSize: 10000,
        cacheCleanupInterval: 600,
        logLevel: 'INFO',
        logFileSizeLimit: 100,
        logFileRetentionCount: 10,
        syncRetryCount: 3,
        syncTimeout: 30000,
        milvusBatchSize: 100,
        milvusIndexBuildThreshold: 10000
      })
      ElMessage.success('配置已重置为默认值')
    } catch (error) {
      console.error('Failed to reset config:', error)
      ElMessage.error('重置配置失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 刷新缓存
const refreshCache = async () => {
  try {
    await ElMessageBox.confirm('确定要刷新系统缓存吗？', '刷新缓存确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const response = await systemApi.refreshSystemCache()
    ElMessage.success('系统缓存刷新成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to refresh cache:', error)
      ElMessage.error('刷新缓存失败')
    }
  }
}

// 清理日志
const cleanLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清理系统日志吗？', '清理日志确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await systemApi.cleanSystemLogs()
    ElMessage.success('系统日志清理成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to clean logs:', error)
      ElMessage.error('清理日志失败')
    }
  }
}

// 加载系统信息
const loadSystemInfo = async () => {
  try {
    const response = await systemApi.getSystemInfo()
    const info = response.data
    Object.assign(systemInfo, {
      javaVersion: info.javaVersion || '',
      osName: info.osName || '',
      osVersion: info.osVersion || '',
      availableProcessors: info.availableProcessors || '',
      maxMemory: info.maxMemory || '',
      totalMemory: info.totalMemory || '',
      freeMemory: info.freeMemory || '',
      systemTime: info.systemTime || '',
      startupTime: info.startupTime || ''
    })
    ElMessage.success('系统信息加载成功')
  } catch (error) {
    console.error('Failed to load system info:', error)
    ElMessage.error('加载系统信息失败')
  }
}
</script>

<style scoped>
.system-config {
  padding: 20px;
  min-height: 100vh;
  animation: fadeIn 0.8s ease-out;
  position: relative;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  animation: slideInLeft 0.6s ease-out;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.card-header span {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  padding: 12px 0;
}

.card-header span::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border-radius: 2px;
  animation: expandWidth 1s ease-out 0.3s forwards;
  transform: scaleX(0);
  transform-origin: left;
}

@keyframes expandWidth {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.header-actions :deep(.el-button--primary) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border: none;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  color: #ffffff;
}

.header-actions :deep(.el-button--primary::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.header-actions :deep(.el-button--primary:active::before) {
  width: 300px;
  height: 300px;
}

.header-actions :deep(.el-button--primary:hover) {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.5);
}

.config-content {
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-collapse) {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-collapse-item) {
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-collapse-item:last-child) {
  border-bottom: none;
}

:deep(.el-collapse-item__header) {
  background: rgba(15, 23, 42, 0.9);
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  padding: 20px 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-collapse-item__header:hover) {
  background: rgba(59, 130, 246, 0.1);
  transform: translateX(8px);
}

:deep(.el-collapse-item__content) {
  background: rgba(15, 23, 42, 0.7);
  padding: 24px;
  border-top: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-form) {
  background: transparent;
  padding: 0;
  box-shadow: none;
  border: none;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-input-number .el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover),
:deep(.el-input-number .el-input__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

:deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select-dropdown) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
}

:deep(.el-select-dropdown__item) {
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  margin: 4px 8px;
}

:deep(.el-select-dropdown__item:hover) {
  background: rgba(59, 130, 246, 0.1);
  transform: translateX(8px);
}

:deep(.el-select-dropdown__item.selected) {
  background: rgba(59, 130, 246, 0.2);
  color: #ffffff;
}

/* 系统信息 */
.system-info {
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.info-card {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

.info-header span {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.info-content {
  padding: 24px;
}

:deep(.el-descriptions) {
  background: rgba(15, 23, 42, 0.7);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

:deep(.el-descriptions__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

:deep(.el-descriptions__content) {
  color: rgba(255, 255, 255, 0.8);
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  margin-right: 8px;
  padding: 8px 16px;
  font-size: 12px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

:deep(.el-button:active::before) {
  width: 200px;
  height: 200px;
}

:deep(.el-button:hover) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  color: #ffffff;
}

:deep(.el-button--primary) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border: none;
  color: #ffffff;
}

:deep(.el-button--info) {
  background: linear-gradient(90deg, #06b6d4, #0ea5e9);
  border: none;
  color: #ffffff;
}

:deep(.el-button--warning) {
  background: linear-gradient(90deg, #f59e0b, #ef4444);
  border: none;
  color: #ffffff;
}

:deep(.el-button:disabled) {
  opacity: 0.5;
  transform: none;
  box-shadow: none;
  background: rgba(15, 23, 42, 0.6) !important;
  color: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
}

:deep(.el-button:disabled:hover) {
  transform: none;
  box-shadow: none;
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.2);
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  animation: fadeInUp 0.8s ease-out;
}

:deep(.el-card__body) {
  padding: 32px;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 5px;
}

::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.5);
  border-radius: 5px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(15, 23, 42, 0.6);
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.8);
  transform: scale(1.1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 0 20px;
  }
  
  .header-actions {
    align-self: flex-start;
  }
  
  .header-actions :deep(.el-button) {
    padding: 10px 20px;
    font-size: 14px;
  }
  
  .card-header span {
    font-size: 18px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  :deep(.el-collapse-item__header) {
    padding: 16px 20px;
  }
  
  :deep(.el-collapse-item__content) {
    padding: 20px;
  }
  
  :deep(.el-descriptions) {
    padding: 20px;
  }
  
  :deep(.el-descriptions) {
    :deep(.el-descriptions__item) {
      padding: 8px 0;
    }
  }
}

@media (max-width: 992px) {
  .system-config {
    padding: 16px;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .header-actions :deep(.el-button) {
    padding: 8px 16px;
    font-size: 12px;
  }
  
  :deep(.el-collapse-item__header) {
    padding: 14px 16px;
    font-size: 14px;
  }
  
  :deep(.el-collapse-item__content) {
    padding: 16px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
  
  :deep(.el-form-item__label) {
    width: 140px;
    font-size: 14px;
  }
  
  :deep(.el-descriptions) {
    :deep(.el-descriptions__item) {
      padding: 6px 0;
    }
  }
}

@media (max-width: 768px) {
  .card-header {
    padding: 0 16px;
  }
  
  .card-header span {
    font-size: 16px;
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
  
  :deep(.el-collapse-item__header) {
    padding: 12px 14px;
    font-size: 13px;
  }
  
  :deep(.el-collapse-item__content) {
    padding: 12px;
  }
  
  :deep(.el-form-item__label) {
    width: 120px;
    font-size: 13px;
  }
  
  :deep(.el-descriptions) {
    :deep(.el-descriptions__column) {
      column-count: 2;
    }
  }
  
  .info-content {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  :deep(.el-descriptions) {
    :deep(.el-descriptions__column) {
      column-count: 1;
    }
  }
  
  :deep(.el-form-item) {
    flex-direction: column;
    align-items: flex-start;
  }
  
  :deep(.el-form-item__label) {
    width: 100%;
    margin-bottom: 8px;
  }
  
  :deep(.el-form-item__content) {
    width: 100%;
  }
}
</style>