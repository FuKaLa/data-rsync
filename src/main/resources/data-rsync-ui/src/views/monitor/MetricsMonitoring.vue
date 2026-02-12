<template>
  <div class="metrics-monitoring">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>监控功能</span>
          <div class="header-actions">
            <el-button type="primary" @click="loadAllMetrics">刷新所有指标</el-button>
            <el-button @click="activeTab = 'metrics'">监控指标</el-button>
            <el-button @click="activeTab = 'alerts'">告警管理</el-button>
            <el-button @click="activeTab = 'dashboard'">仪表盘</el-button>
          </div>
        </div>
      </template>
      
      <!-- 监控指标 -->
      <div v-if="activeTab === 'metrics'" class="tab-content">
        <div class="metrics-tabs">
          <el-tabs v-model="activeMetricTab" @tab-click="handleMetricTabClick">
            <el-tab-pane label="所有指标" name="all">
              <el-button type="primary" @click="loadAllMetrics" class="mb-4">刷新指标</el-button>
              <el-card v-if="metricsData" class="metrics-card">
                <template #header>
                  <div class="metrics-header">
                    <span>所有监控指标</span>
                    <span class="timestamp">{{ formatTimestamp(metricsData.timestamp) }}</span>
                  </div>
                </template>
                <div class="metrics-grid">
                  <div v-for="(value, key) in metricsData.metrics" :key="key" class="metric-item">
                    <div class="metric-label">{{ key }}</div>
                    <div class="metric-value">{{ formatValue(value) }}</div>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="JVM指标" name="jvm">
              <el-button type="primary" @click="loadJvmMetrics" class="mb-4">刷新JVM指标</el-button>
              <el-card v-if="jvmMetrics" class="metrics-card">
                <template #header>
                  <div class="metrics-header">
                    <span>JVM监控指标</span>
                    <span class="timestamp">{{ formatTimestamp(jvmMetrics.timestamp) }}</span>
                  </div>
                </template>
                <div class="metrics-grid">
                  <div v-for="(value, key) in jvmMetrics.metrics" :key="key" class="metric-item">
                    <div class="metric-label">{{ key }}</div>
                    <div class="metric-value">{{ formatValue(value) }}</div>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="系统指标" name="system">
              <el-button type="primary" @click="loadSystemMetrics" class="mb-4">刷新系统指标</el-button>
              <el-card v-if="systemMetrics" class="metrics-card">
                <template #header>
                  <div class="metrics-header">
                    <span>系统监控指标</span>
                    <span class="timestamp">{{ formatTimestamp(systemMetrics.timestamp) }}</span>
                  </div>
                </template>
                <div class="metrics-grid">
                  <div v-for="(value, key) in systemMetrics.metrics" :key="key" class="metric-item">
                    <div class="metric-label">{{ key }}</div>
                    <div class="metric-value">{{ formatValue(value) }}</div>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="数据源指标" name="datasource">
              <el-button type="primary" @click="loadDatasourceMetrics" class="mb-4">刷新数据源指标</el-button>
              <el-card v-if="datasourceMetrics" class="metrics-card">
                <template #header>
                  <div class="metrics-header">
                    <span>数据源监控指标</span>
                    <span class="timestamp">{{ formatTimestamp(datasourceMetrics.timestamp) }}</span>
                  </div>
                </template>
                <div class="metrics-grid">
                  <div v-for="(value, key) in datasourceMetrics.metrics" :key="key" class="metric-item">
                    <div class="metric-label">{{ key }}</div>
                    <div class="metric-value">{{ formatValue(value) }}</div>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="任务指标" name="task">
              <el-button type="primary" @click="loadTaskMetrics" class="mb-4">刷新任务指标</el-button>
              <el-card v-if="taskMetrics" class="metrics-card">
                <template #header>
                  <div class="metrics-header">
                    <span>任务监控指标</span>
                    <span class="timestamp">{{ formatTimestamp(taskMetrics.timestamp) }}</span>
                  </div>
                </template>
                <div class="metrics-grid">
                  <div v-for="(value, key) in taskMetrics.metrics" :key="key" class="metric-item">
                    <div class="metric-label">{{ key }}</div>
                    <div class="metric-value">{{ formatValue(value) }}</div>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="Milvus指标" name="milvus">
              <el-button type="primary" @click="loadMilvusMetrics" class="mb-4">刷新Milvus指标</el-button>
              <el-card v-if="milvusMetrics" class="metrics-card">
                <template #header>
                  <div class="metrics-header">
                    <span>Milvus监控指标</span>
                    <span class="timestamp">{{ formatTimestamp(milvusMetrics.timestamp) }}</span>
                  </div>
                </template>
                <div class="metrics-grid">
                  <div v-for="(value, key) in milvusMetrics.metrics" :key="key" class="metric-item">
                    <div class="metric-label">{{ key }}</div>
                    <div class="metric-value">{{ formatValue(value) }}</div>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="API性能" name="api">
              <el-button type="primary" @click="loadApiMetrics" class="mb-4">刷新API指标</el-button>
              <el-card v-if="apiMetrics" class="metrics-card">
                <template #header>
                  <div class="metrics-header">
                    <span>API性能指标</span>
                    <span class="timestamp">{{ formatTimestamp(apiMetrics.timestamp) }}</span>
                  </div>
                </template>
                <div class="metrics-grid">
                  <div v-for="(value, key) in apiMetrics.metrics" :key="key" class="metric-item">
                    <div class="metric-label">{{ key }}</div>
                    <div class="metric-value">{{ formatValue(value) }}</div>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
            <el-tab-pane label="错误统计" name="error">
              <el-button type="primary" @click="loadErrorMetrics" class="mb-4">刷新错误指标</el-button>
              <el-card v-if="errorMetrics" class="metrics-card">
                <template #header>
                  <div class="metrics-header">
                    <span>错误统计指标</span>
                    <span class="timestamp">{{ formatTimestamp(errorMetrics.timestamp) }}</span>
                  </div>
                </template>
                <div class="metrics-grid">
                  <div v-for="(value, key) in errorMetrics.metrics" :key="key" class="metric-item">
                    <div class="metric-label">{{ key }}</div>
                    <div class="metric-value">{{ formatValue(value) }}</div>
                  </div>
                </div>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
      
      <!-- 告警管理 -->
      <div v-if="activeTab === 'alerts'" class="tab-content">
        <div class="alerts-content">
          <el-form :model="alertForm" label-width="120px" class="mb-4">
            <el-form-item label="告警级别">
              <el-select v-model="alertForm.severity" placeholder="请选择告警级别">
                <el-option label="INFO" value="INFO" />
                <el-option label="WARNING" value="WARNING" />
                <el-option label="ERROR" value="ERROR" />
                <el-option label="CRITICAL" value="CRITICAL" />
              </el-select>
            </el-form-item>
            <el-form-item label="告警消息">
              <el-input type="textarea" v-model="alertForm.message" placeholder="请输入告警消息" :rows="4" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="sendAlert">发送告警</el-button>
              <el-button @click="resetAlertForm">重置</el-button>
            </el-form-item>
          </el-form>
          
          <el-card class="alerts-history-card">
            <template #header>
              <div class="alerts-header">
                <span>告警历史</span>
              </div>
            </template>
            <el-table :data="alertHistory" style="width: 100%">
              <el-table-column prop="timestamp" label="时间" width="180">
                <template #default="scope">
                  {{ formatTimestamp(scope.row.timestamp) }}
                </template>
              </el-table-column>
              <el-table-column prop="severity" label="级别" width="100">
                <template #default="scope">
                  <el-tag :type="getAlertType(scope.row.severity)">
                    {{ scope.row.severity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="消息" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'resolved' ? 'success' : 'danger'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button size="small" @click="resolveAlert(scope.row.id)" :disabled="scope.row.status === 'resolved'">
                    解决
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
      
      <!-- 仪表盘 -->
      <div v-if="activeTab === 'dashboard'" class="tab-content">
        <div class="dashboard-content">
          <div class="dashboard-grid">
            <!-- 系统健康状态 -->
            <el-card class="dashboard-card">
              <template #header>
                <div class="dashboard-card-header">
                  <span>系统健康状态</span>
                </div>
              </template>
              <div class="health-status">
                <div class="health-item">
                  <div class="health-label">JVM状态</div>
                  <div class="health-value" :class="getHealthClass(jvmHealth)">{{ jvmHealth }}</div>
                </div>
                <div class="health-item">
                  <div class="health-label">系统状态</div>
                  <div class="health-value" :class="getHealthClass(systemHealth)">{{ systemHealth }}</div>
                </div>
                <div class="health-item">
                  <div class="health-label">数据源状态</div>
                  <div class="health-value" :class="getHealthClass(datasourceHealth)">{{ datasourceHealth }}</div>
                </div>
                <div class="health-item">
                  <div class="health-label">Milvus状态</div>
                  <div class="health-value" :class="getHealthClass(milvusHealth)">{{ milvusHealth }}</div>
                </div>
              </div>
            </el-card>
            
            <!-- 关键指标 -->
            <el-card class="dashboard-card">
              <template #header>
                <div class="dashboard-card-header">
                  <span>关键指标</span>
                </div>
              </template>
              <div class="key-metrics">
                <div class="key-metric-item">
                  <div class="key-metric-label">CPU使用率</div>
                  <div class="key-metric-value">{{ cpuUsage }}%</div>
                </div>
                <div class="key-metric-item">
                  <div class="key-metric-label">内存使用率</div>
                  <div class="key-metric-value">{{ memoryUsage }}%</div>
                </div>
                <div class="key-metric-item">
                  <div class="key-metric-label">活跃线程</div>
                  <div class="key-metric-value">{{ activeThreads }}</div>
                </div>
                <div class="key-metric-item">
                  <div class="key-metric-label">任务成功率</div>
                  <div class="key-metric-value">{{ taskSuccessRate }}%</div>
                </div>
              </div>
            </el-card>
            
            <!-- 告警统计 -->
            <el-card class="dashboard-card">
              <template #header>
                <div class="dashboard-card-header">
                  <span>告警统计</span>
                </div>
              </template>
              <div class="alert-stats">
                <div class="alert-stat-item">
                  <div class="alert-stat-label">INFO</div>
                  <div class="alert-stat-value">{{ infoAlerts }}</div>
                </div>
                <div class="alert-stat-item">
                  <div class="alert-stat-label">WARNING</div>
                  <div class="alert-stat-value">{{ warningAlerts }}</div>
                </div>
                <div class="alert-stat-item">
                  <div class="alert-stat-label">ERROR</div>
                  <div class="alert-stat-value">{{ errorAlerts }}</div>
                </div>
                <div class="alert-stat-item">
                  <div class="alert-stat-label">CRITICAL</div>
                  <div class="alert-stat-value">{{ criticalAlerts }}</div>
                </div>
              </div>
            </el-card>
            
            <!-- 系统负载 -->
            <el-card class="dashboard-card">
              <template #header>
                <div class="dashboard-card-header">
                  <span>系统负载</span>
                </div>
              </template>
              <div class="system-load">
                <div class="load-item">
                  <div class="load-label">1分钟负载</div>
                  <div class="load-value">{{ load1m }}</div>
                </div>
                <div class="load-item">
                  <div class="load-label">5分钟负载</div>
                  <div class="load-value">{{ load5m }}</div>
                </div>
                <div class="load-item">
                  <div class="load-label">15分钟负载</div>
                  <div class="load-value">{{ load15m }}</div>
                </div>
                <div class="load-item">
                  <div class="load-label">磁盘使用率</div>
                  <div class="load-value">{{ diskUsage }}%</div>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { monitorApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 标签页
const activeTab = ref('metrics')

// 监控指标标签页
const activeMetricTab = ref('all')

// 指标数据
const metricsData = ref<any>(null)
const jvmMetrics = ref<any>(null)
const systemMetrics = ref<any>(null)
const datasourceMetrics = ref<any>(null)
const taskMetrics = ref<any>(null)
const milvusMetrics = ref<any>(null)
const apiMetrics = ref<any>(null)
const errorMetrics = ref<any>(null)

// 告警表单
const alertForm = reactive({
  severity: 'INFO',
  message: ''
})

// 告警历史
const alertHistory = ref([
  {
    id: 1,
    timestamp: Date.now(),
    severity: 'INFO',
    message: '系统启动正常',
    status: 'resolved'
  },
  {
    id: 2,
    timestamp: Date.now() - 3600000,
    severity: 'WARNING',
    message: 'CPU使用率超过阈值',
    status: 'resolved'
  },
  {
    id: 3,
    timestamp: Date.now() - 7200000,
    severity: 'ERROR',
    message: '数据源连接失败',
    status: 'resolved'
  }
])

// 仪表盘数据
const jvmHealth = ref('HEALTHY')
const systemHealth = ref('HEALTHY')
const datasourceHealth = ref('HEALTHY')
const milvusHealth = ref('HEALTHY')
const cpuUsage = ref(45)
const memoryUsage = ref(60)
const activeThreads = ref(25)
const taskSuccessRate = ref(98)
const infoAlerts = ref(10)
const warningAlerts = ref(5)
const errorAlerts = ref(2)
const criticalAlerts = ref(0)
const load1m = ref(1.2)
const load5m = ref(1.0)
const load15m = ref(0.8)
const diskUsage = ref(75)

onMounted(() => {
  loadAllMetrics()
  loadJvmMetrics()
  loadSystemMetrics()
  loadDatasourceMetrics()
  loadTaskMetrics()
  loadMilvusMetrics()
  loadApiMetrics()
  loadErrorMetrics()
})

// 加载所有指标
const loadAllMetrics = async () => {
  try {
    const response = await monitorApi.getAllMetrics()
    metricsData.value = response.data
    ElMessage.success('所有指标加载成功')
  } catch (error) {
    console.error('Failed to load all metrics:', error)
    ElMessage.error('加载所有指标失败')
  }
}

// 加载JVM指标
const loadJvmMetrics = async () => {
  try {
    const response = await monitorApi.getJvmMetrics()
    jvmMetrics.value = response.data
    ElMessage.success('JVM指标加载成功')
  } catch (error) {
    console.error('Failed to load JVM metrics:', error)
    ElMessage.error('加载JVM指标失败')
  }
}

// 加载系统指标
const loadSystemMetrics = async () => {
  try {
    const response = await monitorApi.getSystemMetrics()
    systemMetrics.value = response.data
    ElMessage.success('系统指标加载成功')
  } catch (error) {
    console.error('Failed to load system metrics:', error)
    ElMessage.error('加载系统指标失败')
  }
}

// 加载数据源指标
const loadDatasourceMetrics = async () => {
  try {
    const response = await monitorApi.getDatasourceMetrics()
    datasourceMetrics.value = response.data
    ElMessage.success('数据源指标加载成功')
  } catch (error) {
    console.error('Failed to load datasource metrics:', error)
    ElMessage.error('加载数据源指标失败')
  }
}

// 加载任务指标
const loadTaskMetrics = async () => {
  try {
    const response = await monitorApi.getTaskMetrics()
    taskMetrics.value = response.data
    ElMessage.success('任务指标加载成功')
  } catch (error) {
    console.error('Failed to load task metrics:', error)
    ElMessage.error('加载任务指标失败')
  }
}

// 加载Milvus指标
const loadMilvusMetrics = async () => {
  try {
    const response = await monitorApi.getMilvusMetrics()
    milvusMetrics.value = response.data
    ElMessage.success('Milvus指标加载成功')
  } catch (error) {
    console.error('Failed to load Milvus metrics:', error)
    ElMessage.error('加载Milvus指标失败')
  }
}

// 加载API指标
const loadApiMetrics = async () => {
  try {
    const response = await monitorApi.getApiMetrics()
    apiMetrics.value = response.data
    ElMessage.success('API指标加载成功')
  } catch (error) {
    console.error('Failed to load API metrics:', error)
    ElMessage.error('加载API指标失败')
  }
}

// 加载错误指标
const loadErrorMetrics = async () => {
  try {
    const response = await monitorApi.getErrorMetrics()
    errorMetrics.value = response.data
    ElMessage.success('错误指标加载成功')
  } catch (error) {
    console.error('Failed to load error metrics:', error)
    ElMessage.error('加载错误指标失败')
  }
}

// 处理指标标签页点击
const handleMetricTabClick = (tab: any) => {
  const tabName = tab.props.name
  switch (tabName) {
    case 'all':
      loadAllMetrics()
      break
    case 'jvm':
      loadJvmMetrics()
      break
    case 'system':
      loadSystemMetrics()
      break
    case 'datasource':
      loadDatasourceMetrics()
      break
    case 'task':
      loadTaskMetrics()
      break
    case 'milvus':
      loadMilvusMetrics()
      break
    case 'api':
      loadApiMetrics()
      break
    case 'error':
      loadErrorMetrics()
      break
  }
}

// 发送告警
const sendAlert = async () => {
  try {
    if (!alertForm.message) {
      ElMessage.warning('请输入告警消息')
      return
    }
    
    const response = await monitorApi.sendAlert(alertForm.severity, alertForm.message, {})
    
    // 添加到告警历史
    alertHistory.value.unshift({
      id: alertHistory.value.length + 1,
      timestamp: Date.now(),
      severity: alertForm.severity,
      message: alertForm.message,
      status: 'active'
    })
    
    ElMessage.success('告警发送成功')
    resetAlertForm()
  } catch (error) {
    console.error('Failed to send alert:', error)
    ElMessage.error('发送告警失败')
  }
}

// 重置告警表单
const resetAlertForm = () => {
  Object.assign(alertForm, {
    severity: 'INFO',
    message: ''
  })
}

// 解决告警
const resolveAlert = async (id: number) => {
  try {
    const alert = alertHistory.value.find(item => item.id === id)
    if (alert) {
      alert.status = 'resolved'
      ElMessage.success('告警已解决')
    }
  } catch (error) {
    console.error('Failed to resolve alert:', error)
    ElMessage.error('解决告警失败')
  }
}

// 格式化时间戳
const formatTimestamp = (timestamp: number) => {
  return new Date(timestamp).toLocaleString()
}

// 格式化值
const formatValue = (value: any) => {
  if (typeof value === 'number') {
    return value.toLocaleString()
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return value
}

// 获取告警类型
const getAlertType = (severity: string) => {
  switch (severity) {
    case 'INFO':
      return 'info'
    case 'WARNING':
      return 'warning'
    case 'ERROR':
      return 'danger'
    case 'CRITICAL':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取健康状态类
const getHealthClass = (health: string) => {
  switch (health) {
    case 'HEALTHY':
      return 'healthy'
    case 'WARNING':
      return 'warning'
    case 'UNHEALTHY':
      return 'unhealthy'
    default:
      return 'healthy'
  }
}
</script>

<style scoped>
.metrics-monitoring {
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

.tab-content {
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

/* 指标标签页 */
.metrics-tabs {
  margin-top: 24px;
}

:deep(.el-tabs) {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-tabs__header) {
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

:deep(.el-tabs__nav) {
  padding: 0 24px;
}

:deep(.el-tabs__item) {
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
  padding: 16px 24px;
  margin-right: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-tabs__item:hover) {
  color: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
}

:deep(.el-tabs__item.is-active) {
  color: #3b82f6;
  border-bottom: 3px solid #3b82f6;
}

:deep(.el-tabs__content) {
  padding: 24px;
}

/* 指标卡片 */
.metrics-card {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  margin-top: 24px;
}

.metrics-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

.metrics-header span {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.timestamp {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
  padding: 24px;
}

.metric-item {
  background: rgba(15, 23, 42, 0.7);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(59, 130, 246, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.metric-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.3);
  border-color: #3b82f6;
}

.metric-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
  font-weight: 600;
}

.metric-value {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 700;
}

/* 告警管理 */
.alerts-content {
  margin-top: 24px;
}

:deep(.el-form) {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  margin-bottom: 24px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover) {
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

.alerts-history-card {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

.alerts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

.alerts-header span {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-table) {
  background: rgba(15, 23, 42, 0.7);
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-table__header-wrapper) {
  background: rgba(15, 23, 42, 0.9) !important;
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

:deep(.el-table__header th) {
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9) !important;
  background: transparent !important;
  border-bottom: none;
  padding: 16px 16px;
}

:deep(.el-table__row) {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
}

:deep(.el-table__row:hover) {
  background: rgba(59, 130, 246, 0.1) !important;
  transform: translateX(8px);
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.2);
  border-left: 3px solid #3b82f6;
}

:deep(.el-table__cell) {
  padding: 16px 16px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

/* 仪表盘 */
.dashboard-content {
  margin-top: 24px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.dashboard-card {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.dashboard-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.4);
}

.dashboard-card-header {
  padding: 16px 24px;
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

.dashboard-card-header span {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.health-status {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.health-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.7);
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.health-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
}

.health-value {
  font-size: 16px;
  font-weight: 700;
}

.health-value.healthy {
  color: #10b981;
}

.health-value.warning {
  color: #f59e0b;
}

.health-value.unhealthy {
  color: #ef4444;
}

.key-metrics {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.key-metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.7);
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.key-metric-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
}

.key-metric-value {
  font-size: 18px;
  font-weight: 700;
  color: #3b82f6;
}

.alert-stats {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.alert-stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.7);
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.alert-stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
}

.alert-stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #f59e0b;
}

.system-load {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.load-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(15, 23, 42, 0.7);
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.load-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 600;
}

.load-value {
  font-size: 16px;
  font-weight: 700;
  color: #a855f7;
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
  padding: 24px;
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
    padding: 20px;
  }
  
  .metrics-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
  }
  
  .dashboard-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 992px) {
  .metrics-monitoring {
    padding: 16px;
  }
  
  :deep(.el-card__body) {
    padding: 16px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .header-actions :deep(.el-button) {
    padding: 8px 16px;
    font-size: 12px;
  }
  
  :deep(.el-tabs__nav) {
    padding: 0 16px;
  }
  
  :deep(.el-tabs__item) {
    padding: 12px 16px;
    margin-right: 16px;
  }
  
  :deep(.el-tabs__content) {
    padding: 16px;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .dashboard-grid {
    grid-template-columns: 1fr;
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
    padding: 12px;
  }
  
  :deep(.el-form) {
    padding: 16px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
  
  :deep(.el-table__cell) {
    padding: 12px 8px;
  }
  
  :deep(.el-button) {
    margin-right: 4px;
    padding: 6px 12px;
    font-size: 11px;
  }
}
</style>