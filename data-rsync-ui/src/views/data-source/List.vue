<template>
  <div class="data-source-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon class="title-icon"><DataAnalysis /></el-icon>
        数据源管理
      </h1>
      <p class="page-subtitle">管理所有数据同步的数据源连接</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <h3 class="stat-number">{{ totalDataSources }}</h3>
          <p class="stat-label">总数据源</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <h3 class="stat-number">{{ unstableDataSources }}</h3>
          <p class="stat-label">不稳定</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">
          <el-icon><Close /></el-icon>
        </div>
        <div class="stat-content">
          <h3 class="stat-number">{{ unhealthyDataSources }}</h3>
          <p class="stat-label">连接失败</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon info">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div class="stat-content">
          <h3 class="stat-number">{{ healthyDataSources }}</h3>
          <p class="stat-label">正常</p>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索区域 -->
    <div class="filter-section">
      <div class="filter-controls">
        <el-input
          v-model="searchQuery"
          placeholder="搜索数据源名称"
          class="search-input"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="filterType"
          placeholder="按类型筛选"
          class="filter-select"
          clearable
          @change="handleFilter"
        >
          <el-option label="MySQL" value="MYSQL" />
          <el-option label="PostgreSQL" value="POSTGRESQL" />
          <el-option label="Oracle" value="ORACLE" />
          <el-option label="SQL Server" value="SQL_SERVER" />
          <el-option label="MongoDB" value="MONGODB" />
          <el-option label="Redis" value="REDIS" />
        </el-select>
        <el-select
          v-model="filterStatus"
          placeholder="按状态筛选"
          class="filter-select"
          clearable
          @change="handleFilter"
        >
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
      </div>
      <el-button type="primary" class="create-btn" @click="handleCreate">
        <el-icon class="btn-icon"><Plus /></el-icon>
        创建数据源
      </el-button>
    </div>

    <!-- 数据源网格 -->
    <div v-if="!loading" class="data-source-grid">
      <div
        v-for="(dataSource, index) in filteredDataSources"
        :key="dataSource.id"
        class="data-source-card"
        :class="{
          'status-healthy': dataSource.healthStatus === 'HEALTHY',
          'status-unstable': dataSource.healthStatus === 'UNSTABLE',
          'status-unhealthy': dataSource.healthStatus === 'UNHEALTHY'
        }"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="card-title">
            <h3>{{ dataSource.name }}</h3>
            <el-tag :type="getTypeTagType(dataSource.type)" class="type-tag">
              {{ getTypeName(dataSource.type) }}
            </el-tag>
          </div>
          <el-switch
            v-model="dataSource.enabled"
            @change="handleEnableChange(dataSource)"
            active-color="#4361ee"
            inactive-color="#6b7280"
            inline-prompt
            active-text="启用"
            inactive-text="禁用"
            size="small"
          />
        </div>

        <!-- 卡片内容 -->
        <div class="card-content">
          <div class="connection-info">
            <div class="info-item">
              <el-icon class="info-icon"><Link /></el-icon>
              <span class="info-label">连接地址:</span>
              <span class="info-value">{{ dataSource.url }}</span>
            </div>
            <div class="info-item">
              <el-icon class="info-icon"><UserFilled /></el-icon>
              <span class="info-label">用户名:</span>
              <span class="info-value">{{ dataSource.username }}</span>
            </div>
            <div class="info-item">
              <el-icon class="info-icon"><Timer /></el-icon>
              <span class="info-label">创建时间:</span>
              <span class="info-value">{{ dataSource.createTime }}</span>
            </div>
          </div>

          <!-- 健康状态 -->
          <div class="health-status">
            <div class="status-indicator">
              <div class="status-dot" :class="dataSource.healthStatus.toLowerCase()"></div>
              <span class="status-text">{{ getHealthStatusText(dataSource.healthStatus) }}</span>
            </div>
            <el-tooltip :content="getHealthTooltip(dataSource)" placement="top" :effect="'dark'">
              <div class="status-details">
                <el-icon><InfoFilled /></el-icon>
              </div>
            </el-tooltip>
          </div>

          <!-- 日志监控状态 -->
          <div class="log-monitor-status" v-if="dataSource.logMonitorType">
            <div class="status-indicator">
              <div class="status-dot" :class="getLogMonitorStatusClass(dataSource.logMonitorStatus)"></div>
              <span class="status-text">{{ getLogMonitorStatusText(dataSource.logMonitorStatus) }}</span>
            </div>
            <el-tooltip :content="getLogMonitorTooltip(dataSource)" placement="top" :effect="'dark'">
              <div class="status-details">
                <el-icon><InfoFilled /></el-icon>
              </div>
            </el-tooltip>
          </div>
        </div>

        <!-- 卡片底部操作 -->
        <div class="card-actions">
          <el-button size="small" class="action-btn edit" @click="handleEdit(dataSource)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button size="small" class="action-btn test" @click="handleTestConnection(dataSource.id)">
            <el-icon><CircleCheck /></el-icon>
            测试
          </el-button>
          <el-button size="small" class="action-btn diagnose" @click="handleDiagnose(dataSource.id)">
            <el-icon><Warning /></el-icon>
            诊断
          </el-button>
          <el-button size="small" class="action-btn delete" @click="handleDelete(dataSource.id)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredDataSources.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon><DocumentRemove /></el-icon>
        </div>
        <h3>暂无数据源</h3>
        <p>点击"创建数据源"按钮开始添加</p>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建数据源
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else class="loading-state">
      <div class="loading-spinner">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      <p>正在加载数据源...</p>
    </div>

    <!-- 诊断报告对话框 -->
    <el-dialog
      v-model="diagnoseDialogVisible"
      title="数据源诊断报告"
      width="800px"
      destroy-on-close
    >
      <div v-if="diagnoseReport" class="diagnose-report">
        <div class="diagnose-header">
          <h3>诊断结果: <span :class="diagnoseReport.overallStatus === 'SUCCESS' ? 'success' : 'error'">{{ getDiagnoseStatusText(diagnoseReport.overallStatus) }}</span></h3>
          <p class="diagnose-time">诊断时间: {{ diagnoseReport.diagnoseTime }}</p>
        </div>
        <el-divider />
        <div class="diagnose-items">
          <template v-for="(value, key) in diagnoseReport" :key="key">
            <div class="diagnose-item" v-if="key !== 'overallStatus' && key !== 'dataSourceId' && key !== 'diagnoseTime' && key !== 'diagnoseDuration'">
              <div class="diagnose-item-header">
                <h4>{{ getItemTitle(key as string) }}</h4>
                <el-tag :type="value === 'SUCCESS' ? 'success' : 'danger'"> {{ value }} </el-tag>
              </div>
            </div>
          </template>
        </div>
      </div>
      <div v-else class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在诊断中...</span>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="diagnoseDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 测试连接结果对话框 -->
    <el-dialog
      v-model="testResultVisible"
      title="连接测试结果"
      width="400px"
    >
      <div class="test-result">
        <el-icon :class="testResult.success ? 'success-icon' : 'error-icon'">
          {{ testResult.success ? 'Check' : 'Close' }}
        </el-icon>
        <h4 :class="testResult.success ? 'success' : 'error'">
          {{ testResult.success ? '连接成功' : '连接失败' }}
        </h4>
        <p v-if="testResult.message">{{ testResult.message }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="testResultVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { dataSourceApi } from '@/api'
import { Check, Warning, Close, DataAnalysis, Link, UserFilled, Timer, InfoFilled, Edit, Delete, Warning as WarningIcon, CircleCheck, Plus, Search, Loading, DocumentRemove } from '@element-plus/icons-vue'

const router = useRouter()
const dataSources = ref<any[]>([])
const filteredDataSources = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const searchQuery = ref('')
const filterType = ref('')
const filterStatus = ref('')

// 诊断相关
const diagnoseDialogVisible = ref(false)
const diagnoseReport = ref<any>(null)

// 测试连接结果
const testResultVisible = ref(false)
const testResult = ref({ success: false, message: '' })

onMounted(() => {
  loadDataSourceList()
})

const loadDataSourceList = async () => {
  loading.value = true
  try {
    const response = await dataSourceApi.getList()
    dataSources.value = response.data || []
    total.value = dataSources.value.length
    applyFilters()
  } catch (error) {
    console.error('Failed to load data sources:', error)
    dataSources.value = []
    total.value = 0
    applyFilters()
  } finally {
    loading.value = false
  }
}

const applyFilters = () => {
  let result = [...dataSources.value]
  
  // 应用搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(item => 
      item.name.toLowerCase().includes(query)
    )
  }
  
  // 应用类型过滤
  if (filterType.value) {
    result = result.filter(item => item.type === filterType.value)
  }
  
  // 应用状态过滤
  if (filterStatus.value) {
    if (filterStatus.value === 'ENABLED') {
      result = result.filter(item => item.enabled)
    } else if (filterStatus.value === 'DISABLED') {
      result = result.filter(item => !item.enabled)
    }
  }
  
  filteredDataSources.value = result
  total.value = filteredDataSources.value.length
}

const handleSearch = () => {
  currentPage.value = 1
  applyFilters()
}

const handleFilter = () => {
  currentPage.value = 1
  applyFilters()
}

const handleCreate = () => {
  router.push('/data-source/create')
}

const handleEdit = (row: any) => {
  router.push(`/data-source/edit?id=${row.id}`)
}

const handleDelete = async (id: number) => {
  try {
    await dataSourceApi.delete(id)
    await loadDataSourceList()
  } catch (error) {
    console.error('Failed to delete data source:', error)
  }
}

const handleTestConnection = async (id: number) => {
  try {
    const result = await dataSourceApi.testConnection(id)
    testResult.value = {
      success: result.data.success,
      message: result.data.message
    }
    testResultVisible.value = true
  } catch (error) {
    console.error('Failed to test connection:', error)
    testResult.value = {
      success: false,
      message: '连接失败: ' + (error as Error).message
    }
    testResultVisible.value = true
  }
}

const handleEnableChange = async (row: any) => {
  try {
    await dataSourceApi.enable(row.id, row.enabled)
  } catch (error) {
    console.error('Failed to change enable status:', error)
    row.enabled = !row.enabled
  }
}

// 健康状态相关方法
const getHealthStatusType = (status: string) => {
  switch (status) {
    case 'HEALTHY':
      return 'success'
    case 'UNSTABLE':
      return 'warning'
    case 'UNHEALTHY':
      return 'danger'
    default:
      return 'info'
  }
}

const getHealthStatusText = (status: string) => {
  switch (status) {
    case 'HEALTHY':
      return '正常'
    case 'UNSTABLE':
      return '连接不稳定'
    case 'UNHEALTHY':
      return '断开'
    default:
      return status
  }
}

const getHealthTooltip = (row: any) => {
  if (row.healthStatus === 'HEALTHY') {
    return '连接正常'
  } else if (row.healthStatus === 'UNSTABLE') {
    return '连接不稳定，可能存在网络波动'
  } else if (row.healthStatus === 'UNHEALTHY') {
    return `连接断开，最近一次失败原因: ${row.lastFailureReason || '未知'}`
  }
  return '未知状态'
}

// 类型相关方法
const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    'MYSQL': 'info',
    'POSTGRESQL': 'success',
    'ORACLE': 'warning',
    'SQL_SERVER': 'danger',
    'MONGODB': 'primary',
    'REDIS': 'success'
  }
  return types[type] || 'info'
}

const getTypeName = (type: string) => {
  const names: Record<string, string> = {
    'MYSQL': 'MySQL',
    'POSTGRESQL': 'PostgreSQL',
    'ORACLE': 'Oracle',
    'SQL_SERVER': 'SQL Server',
    'MONGODB': 'MongoDB',
    'REDIS': 'Redis'
  }
  return names[type] || type
}

// 诊断相关方法
const handleDiagnose = async (id: number) => {
  diagnoseDialogVisible.value = true
  diagnoseReport.value = null
  
  try {
    const report = await dataSourceApi.diagnose(id)
    diagnoseReport.value = report.data
  } catch (error) {
    console.error('Failed to diagnose data source:', error)
    diagnoseDialogVisible.value = false
  }
}

const getItemTitle = (type: string) => {
  const titles: Record<string, string> = {
    network: '网络连通性',
    authentication: '账号权限',
    logMonitor: '日志监听端口',
    connection: '数据库连接',
    networkStatus: '网络连通性',
    authenticationStatus: '账号权限',
    logMonitorStatus: '日志监听端口',
    connectionStatus: '数据库连接'
  }
  return titles[type] || type
}

const getDiagnoseStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'WARNING': '警告'
  }
  return texts[status] || status
}

// 日志监控状态相关方法
const getLogMonitorStatusClass = (status: string) => {
  switch (status) {
    case 'RUNNING':
      return 'healthy'
    case 'STARTING':
    case 'STOPPING':
      return 'unstable'
    case 'STOPPED':
    case 'FAILED':
      return 'unhealthy'
    default:
      return 'unhealthy'
  }
}

const getLogMonitorStatusText = (status: string) => {
  switch (status) {
    case 'RUNNING':
      return '运行中'
    case 'STARTING':
      return '启动中'
    case 'STOPPING':
      return '停止中'
    case 'STOPPED':
      return '已停止'
    case 'FAILED':
      return '失败'
    default:
      return '未配置'
  }
}

const getLogMonitorTooltip = (row: any) => {
  if (row.logMonitorStatus === 'RUNNING') {
    return '日志监控运行正常'
  } else if (row.logMonitorStatus === 'STARTING') {
    return '日志监控正在启动'
  } else if (row.logMonitorStatus === 'STOPPING') {
    return '日志监控正在停止'
  } else if (row.logMonitorStatus === 'STOPPED') {
    return '日志监控已停止'
  } else if (row.logMonitorStatus === 'FAILED') {
    return `日志监控失败: ${row.logMonitorError || '未知原因'}`
  }
  return '日志监控未配置'
}

// 统计数据计算
const totalDataSources = computed(() => dataSources.value.length)
const healthyDataSources = computed(() => dataSources.value.filter(item => item.healthStatus === 'HEALTHY').length)
const unstableDataSources = computed(() => dataSources.value.filter(item => item.healthStatus === 'UNSTABLE').length)
const unhealthyDataSources = computed(() => dataSources.value.filter(item => item.healthStatus === 'UNHEALTHY').length)
</script>

<style scoped>
/* 全局样式 */
.data-source-list {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  padding: 32px;
  position: relative;
  overflow: hidden;
}

/* 背景网格 */
.data-source-list::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 50px 50px;
  }
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 48px;
  animation: fadeInUp 0.8s ease-out;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(90deg, #4361ee, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.title-icon {
  font-size: 40px;
  color: #4361ee;
  animation: pulse 2s infinite;
}

.page-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 48px;
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.stat-card {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  backdrop-filter: blur(15px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #4361ee, #3a0ca3);
  transform: scaleY(0);
  transition: transform 0.3s ease;
  transform-origin: bottom;
}

.stat-card:hover::before {
  transform: scaleY(1);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 60px rgba(67, 97, 238, 0.3);
  border-color: rgba(67, 97, 238, 0.4);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  transition: all 0.3s ease;
}

.stat-icon.success {
  background: linear-gradient(135deg, #10b981, #059669);
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 4px 15px rgba(245, 158, 11, 0.4);
}

.stat-icon.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  box-shadow: 0 4px 15px rgba(239, 68, 68, 0.4);
}

.stat-icon.info {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin: 0;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 0 0 0;
}

/* 筛选和搜索区域 */
.filter-section {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  backdrop-filter: blur(15px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.filter-controls {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  width: 320px;
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.filter-select {
  width: 200px;
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.create-btn {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(67, 97, 238, 0.4);
}

/* 数据源网格 */
.data-source-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 32px;
}

.data-source-card {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 24px;
  padding: 32px;
  backdrop-filter: blur(15px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  animation: slideUp 0.6s ease-out;
}

.data-source-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  transform-origin: left;
}

.data-source-card:hover::before {
  transform: scaleX(1);
}

.data-source-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 60px rgba(67, 97, 238, 0.3);
  border-color: rgba(67, 97, 238, 0.4);
}

/* 状态颜色 */
.data-source-card.status-healthy::before {
  background: linear-gradient(90deg, #10b981, #059669);
}

.data-source-card.status-unstable::before {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.data-source-card.status-unhealthy::before {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.card-title h3 {
  font-size: 20px;
  font-weight: 700;
  color: white;
  margin: 0;
}

.type-tag {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
}

/* 卡片内容 */
.card-content {
  margin-bottom: 24px;
}

.connection-info {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 14px;
}

.info-icon {
  color: #409eff;
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.info-label {
  color: rgba(255, 255, 255, 0.7);
  min-width: 80px;
}

.info-value {
  color: white;
  font-weight: 500;
  flex: 1;
  word-break: break-all;
}

/* 健康状态 */
.health-status,
.log-monitor-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.1);
  margin-bottom: 16px;
}

.log-monitor-status:last-child {
  margin-bottom: 0;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-dot.healthy {
  background: #10b981;
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.4);
}

.status-dot.unstable {
  background: #f59e0b;
  box-shadow: 0 0 20px rgba(245, 158, 11, 0.4);
}

.status-dot.unhealthy {
  background: #ef4444;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4);
}

.status-text {
  color: white;
  font-weight: 500;
}

.status-details {
  color: #409eff;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.status-details:hover {
  background: rgba(64, 158, 255, 0.2);
}

/* 卡片底部操作 */
.card-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  min-width: 80px;
  border-radius: 10px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.action-btn.edit:hover {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border-color: transparent;
  color: white;
}

.action-btn.test:hover {
  background: linear-gradient(90deg, #10b981, #059669);
  border-color: transparent;
  color: white;
}

.action-btn.diagnose:hover {
  background: linear-gradient(90deg, #f59e0b, #d97706);
  border-color: transparent;
  color: white;
}

.action-btn.delete:hover {
  background: linear-gradient(90deg, #ef4444, #dc2626);
  border-color: transparent;
  color: white;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 40px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 24px;
  backdrop-filter: blur(15px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out;
}

.empty-icon {
  font-size: 80px;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 24px;
  animation: pulse 2s infinite;
}

.empty-state h3 {
  font-size: 24px;
  color: white;
  margin-bottom: 12px;
}

.empty-state p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 32px;
}

/* 加载状态 */
.loading-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 40px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 24px;
  backdrop-filter: blur(15px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out;
}

.loading-spinner {
  font-size: 60px;
  color: #4361ee;
  margin-bottom: 24px;
}

.loading-state p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

/* 诊断报告样式 */
.diagnose-report {
  padding: 24px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  animation: fadeInUp 0.6s ease-out;
}

.diagnose-header {
  margin-bottom: 24px;
}

.diagnose-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: white;
  margin-bottom: 8px;
}

.diagnose-time {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.diagnose-items {
  margin-top: 24px;
}

.diagnose-item {
  margin-bottom: 20px;
  padding: 20px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(64, 158, 255, 0.1);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(12px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 4px solid transparent;
}

.diagnose-item:hover {
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.3);
  transform: translateY(-2px);
  border-left-color: #4361ee;
  border-color: rgba(64, 158, 255, 0.3);
}

.diagnose-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.diagnose-item-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin: 0;
}

/* 测试连接结果样式 */
.test-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  text-align: center;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  margin: -24px;
  animation: fadeInUp 0.6s ease-out;
}

.test-result :deep(.el-icon) {
  font-size: 64px;
  margin-bottom: 24px;
  animation: zoomIn 0.6s ease-out;
}

.success-icon {
  color: #10b981;
}

.error-icon {
  color: #ef4444;
}

.test-result h4 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
  color: white;
}

.test-result p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  animation: fadeInUp 0.6s ease-out 0.4s both;
  max-width: 320px;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .data-source-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 24px;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-controls {
    justify-content: center;
  }

  .search-input {
    width: 280px;
  }

  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .data-source-list {
    padding: 16px;
  }

  .page-title {
    font-size: 28px;
  }

  .data-source-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .data-source-card {
    padding: 24px;
  }

  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }

  .filter-select {
    width: 100%;
  }

  .card-actions {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-number {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .filter-section {
    padding: 20px;
  }

  .data-source-card {
    padding: 20px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .card-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>