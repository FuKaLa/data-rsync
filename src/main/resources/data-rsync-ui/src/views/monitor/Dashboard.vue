<template>
  <div class="monitor-dashboard">
    <!-- 告警状态卡片 -->
    <el-card shadow="hover" class="alert-card" :class="{ 'alert-active': hasActiveAlerts }">
      <template #header>
        <div class="card-header">
          <span>告警状态</span>
          <el-badge v-if="alertCount > 0" :value="alertCount" type="danger" />
        </div>
      </template>
      <div class="alert-content">
        <el-empty v-if="alertCount === 0" description="暂无告警" />
        <el-list v-else>
          <el-list-item v-for="alert in recentAlerts" :key="alert.id" class="alert-item">
            <el-tag :type="getAlertType(alert.severity)" size="small" class="alert-tag">{{ alert.severity }}</el-tag>
            <span class="alert-message">{{ alert.message }}</span>
            <span class="alert-time">{{ alert.time }}</span>
          </el-list-item>
        </el-list>
      </div>
    </el-card>

    <!-- 核心指标卡片 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>核心指标</span>
          <el-button type="primary" size="small" @click="refreshMetrics">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <div class="metrics-grid">
        <el-statistic :value="syncSuccessRate" title="同步成功率" suffix="%" :precision="2" />
        <el-statistic :value="dataDelay" title="数据延迟" suffix="s" :precision="2" />
        <el-statistic :value="milvusWriteQps" title="Milvus写入QPS" :precision="0" />
        <el-statistic :value="runningTasks" title="运行中任务" />
        <el-statistic :value="systemCpuUsage" title="系统CPU使用率" suffix="%" :precision="1" />
        <el-statistic :value="systemMemoryUsage" title="系统内存使用率" suffix="%" :precision="1" />
        <el-statistic :value="apiResponseTime" title="API响应时间" suffix="ms" :precision="0" />
        <el-statistic :value="redisHitRate" title="Redis命中率" suffix="%" :precision="1" />
      </div>
    </el-card>

    <!-- 趋势图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>同步成功率趋势</span>
              <el-select v-model="timeRange" size="small" class="time-range-select">
                <el-option label="6小时" value="6h" />
                <el-option label="12小时" value="12h" />
                <el-option label="24小时" value="24h" />
                <el-option label="7天" value="7d" />
              </el-select>
            </div>
          </template>
          <div ref="successRateChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>数据延迟趋势</span>
            </div>
          </template>
          <div ref="delayChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Milvus写入QPS</span>
            </div>
          </template>
          <div ref="qpsChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>任务状态分布</span>
            </div>
          </template>
          <div ref="taskStatusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统资源使用</span>
            </div>
          </template>
          <div ref="systemResourceChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>API性能</span>
            </div>
          </template>
          <div ref="apiPerformanceChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { monitorApi } from '@/api'
import { Refresh } from '@element-plus/icons-vue'

// 核心指标
const syncSuccessRate = ref(99.5)
const dataDelay = ref(1.2)
const milvusWriteQps = ref(1200)
const runningTasks = ref(5)
const systemCpuUsage = ref(45.5)
const systemMemoryUsage = ref(60.2)
const apiResponseTime = ref(150)
const redisHitRate = ref(95.0)

// 告警相关
const alertCount = ref(0)
const hasActiveAlerts = ref(false)
const recentAlerts = ref([
  { id: 1, severity: 'ERROR', message: '任务执行失败：数据库连接超时', time: '2026-02-11 09:30:00' },
  { id: 2, severity: 'WARNING', message: '系统内存使用率超过70%', time: '2026-02-11 09:25:00' }
])

// 时间范围选择
const timeRange = ref('6h')

// 图表引用
const successRateChartRef = ref<HTMLElement>()
const delayChartRef = ref<HTMLElement>()
const qpsChartRef = ref<HTMLElement>()
const taskStatusChartRef = ref<HTMLElement>()
const systemResourceChartRef = ref<HTMLElement>()
const apiPerformanceChartRef = ref<HTMLElement>()

// 图表实例
let successRateChart: echarts.ECharts | null = null
let delayChart: echarts.ECharts | null = null
let qpsChart: echarts.ECharts | null = null
let taskStatusChart: echarts.ECharts | null = null
let systemResourceChart: echarts.ECharts | null = null
let apiPerformanceChart: echarts.ECharts | null = null

// 定时刷新计时器
let refreshTimer: number | null = null

onMounted(async () => {
  await loadMonitorData()
  nextTick(() => {
    initSuccessRateChart()
    initDelayChart()
    initQpsChart()
    initTaskStatusChart()
    initSystemResourceChart()
    initApiPerformanceChart()
  })
  
  // 启动定时刷新
  startAutoRefresh()
})

onUnmounted(() => {
  // 清理计时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  
  // 销毁图表实例
  destroyCharts()
})

// 启动自动刷新
const startAutoRefresh = () => {
  refreshTimer = window.setInterval(async () => {
    await loadMonitorData()
    updateCharts()
  }, 30000) // 每30秒刷新一次
}

// 销毁图表
const destroyCharts = () => {
  successRateChart?.dispose()
  delayChart?.dispose()
  qpsChart?.dispose()
  taskStatusChart?.dispose()
  systemResourceChart?.dispose()
  apiPerformanceChart?.dispose()
}

// 刷新指标数据
const refreshMetrics = async () => {
  await loadMonitorData()
  updateCharts()
}

// 加载监控数据
const loadMonitorData = async () => {
  try {
    // 加载核心指标
    const metricsResponse = await monitorApi.getAllMetrics()
    console.log('Core Metrics:', metricsResponse)
    
    // 加载系统指标
    const systemResponse = await monitorApi.getSystemMetrics()
    console.log('System Metrics:', systemResponse)
    
    // 加载API指标
    const apiResponse = await monitorApi.getApiMetrics()
    console.log('API Metrics:', apiResponse)
    
    // 加载Redis指标
    const redisResponse = await monitorApi.getRedisMetrics()
    console.log('Redis Metrics:', redisResponse)
    
    // 模拟告警数据
    const alertResponse = []
    console.log('Alerts:', alertResponse)
    
    // 更新核心指标
    if (metricsResponse && typeof metricsResponse === 'object') {
      const data = metricsResponse as any
      if (data.syncSuccessRate !== undefined) {
        syncSuccessRate.value = data.syncSuccessRate
      }
      if (data.dataDelay !== undefined) {
        dataDelay.value = data.dataDelay
      }
      if (data.milvusWriteQps !== undefined) {
        milvusWriteQps.value = data.milvusWriteQps
      }
      if (data.runningTasks !== undefined) {
        runningTasks.value = data.runningTasks
      }
    }
    
    // 更新系统指标
    if (systemResponse && typeof systemResponse === 'object') {
      const data = systemResponse as any
      if (data.cpu && data.cpu.usage !== undefined) {
        systemCpuUsage.value = data.cpu.usage
      }
      if (data.memory && data.memory.usage !== undefined) {
        systemMemoryUsage.value = data.memory.usage
      }
    }
    
    // 更新API指标
    if (apiResponse && typeof apiResponse === 'object') {
      const data = apiResponse as any
      if (data.response && data.response.time !== undefined) {
        apiResponseTime.value = data.response.time
      }
    }
    
    // 更新Redis指标
    if (redisResponse && typeof redisResponse === 'object') {
      const data = redisResponse as any
      if (data.hit && data.hit.rate !== undefined) {
        redisHitRate.value = data.hit.rate
      }
    }
    
    // 更新告警数据
    if (alertResponse && Array.isArray(alertResponse)) {
      recentAlerts.value = alertResponse.slice(0, 5)
      alertCount.value = alertResponse.length
      hasActiveAlerts.value = alertResponse.length > 0
    } else {
      // 模拟告警数据
      simulateAlertData()
    }
  } catch (error) {
    console.error('Failed to load monitor data:', error)
    // 加载失败时使用模拟数据
    useMockData()
  }
}

// 使用模拟数据
const useMockData = () => {
  // 模拟核心指标数据
  syncSuccessRate.value = Math.random() * 5 + 95 // 95-100%
  dataDelay.value = Math.random() * 2 + 0.5 // 0.5-2.5s
  milvusWriteQps.value = Math.floor(Math.random() * 800 + 800) // 800-1600
  runningTasks.value = Math.floor(Math.random() * 5 + 3) // 3-8
  
  // 模拟系统指标数据
  systemCpuUsage.value = Math.random() * 30 + 30 // 30-60%
  systemMemoryUsage.value = Math.random() * 20 + 50 // 50-70%
  
  // 模拟API指标数据
  apiResponseTime.value = Math.floor(Math.random() * 100 + 100) // 100-200ms
  redisHitRate.value = Math.random() * 5 + 93 // 93-98%
  
  // 模拟告警数据
  simulateAlertData()
}

// 模拟告警数据
const simulateAlertData = () => {
  // 随机生成告警数据
  const randomAlert = Math.random() > 0.95 // 5%的概率生成告警
  if (randomAlert && alertCount.value < 5) {
    alertCount.value++
    hasActiveAlerts.value = true
    recentAlerts.value.unshift({
      id: Date.now(),
      severity: Math.random() > 0.5 ? 'ERROR' : 'WARNING',
      message: Math.random() > 0.5 ? '任务执行失败：网络连接超时' : '系统CPU使用率超过80%',
      time: new Date().toLocaleString()
    })
    
    // 限制告警数量
    if (recentAlerts.value.length > 5) {
      recentAlerts.value = recentAlerts.value.slice(0, 5)
    }
  }
}

// 获取告警类型对应的标签类型
const getAlertType = (severity: string) => {
  switch (severity) {
    case 'ERROR':
      return 'danger'
    case 'WARNING':
      return 'warning'
    case 'INFO':
      return 'info'
    default:
      return 'default'
  }
}

// 更新图表数据
const updateCharts = () => {
  updateSuccessRateChart()
  updateDelayChart()
  updateQpsChart()
  updateTaskStatusChart()
  updateSystemResourceChart()
  updateApiPerformanceChart()
}

// 初始化成功率图表
const initSuccessRateChart = () => {
  if (successRateChartRef.value) {
    successRateChart = echarts.init(successRateChartRef.value)
    updateSuccessRateChart()
  }
}

// 更新成功率图表
const updateSuccessRateChart = () => {
  if (successRateChart) {
    successRateChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
      },
      yAxis: {
        type: 'value',
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: '同步成功率',
          type: 'line',
          stack: 'Total',
          data: [99.2, 99.5, 99.8, 99.6, 99.7, syncSuccessRate.value]
        }
      ]
    })
  }
}

// 初始化延迟图表
const initDelayChart = () => {
  if (delayChartRef.value) {
    delayChart = echarts.init(delayChartRef.value)
    updateDelayChart()
  }
}

// 更新延迟图表
const updateDelayChart = () => {
  if (delayChart) {
    delayChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}s'
        }
      },
      series: [
        {
          name: '数据延迟',
          type: 'line',
          stack: 'Total',
          data: [1.5, 1.2, 0.8, 1.0, 1.3, dataDelay.value]
        }
      ]
    })
  }
}

// 初始化QPS图表
const initQpsChart = () => {
  if (qpsChartRef.value) {
    qpsChart = echarts.init(qpsChartRef.value)
    updateQpsChart()
  }
}

// 更新QPS图表
const updateQpsChart = () => {
  if (qpsChart) {
    qpsChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: 'Milvus写入QPS',
          type: 'bar',
          data: [800, 1000, 1500, 1300, 1200, milvusWriteQps.value]
        }
      ]
    })
  }
}

// 初始化任务状态图表
const initTaskStatusChart = () => {
  if (taskStatusChartRef.value) {
    taskStatusChart = echarts.init(taskStatusChartRef.value)
    updateTaskStatusChart()
  }
}

// 更新任务状态图表
const updateTaskStatusChart = () => {
  if (taskStatusChart) {
    taskStatusChart.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '任务状态',
          type: 'pie',
          radius: '50%',
          data: [
            { value: runningTasks.value, name: '运行中' },
            { value: 3, name: '已完成' },
            { value: 2, name: '暂停' },
            { value: 1, name: '失败' }
          ]
        }
      ]
    })
  }
}

// 初始化系统资源图表
const initSystemResourceChart = () => {
  if (systemResourceChartRef.value) {
    systemResourceChart = echarts.init(systemResourceChartRef.value)
    updateSystemResourceChart()
  }
}

// 更新系统资源图表
const updateSystemResourceChart = () => {
  if (systemResourceChart) {
    systemResourceChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['CPU使用率', '内存使用率']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
      },
      yAxis: {
        type: 'value',
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          name: 'CPU使用率',
          type: 'bar',
          data: [30, 35, 40, 45, 50, systemCpuUsage.value]
        },
        {
          name: '内存使用率',
          type: 'bar',
          data: [50, 55, 60, 65, 70, systemMemoryUsage.value]
        }
      ]
    })
  }
}

// 初始化API性能图表
const initApiPerformanceChart = () => {
  if (apiPerformanceChartRef.value) {
    apiPerformanceChart = echarts.init(apiPerformanceChartRef.value)
    updateApiPerformanceChart()
  }
}

// 更新API性能图表
const updateApiPerformanceChart = () => {
  if (apiPerformanceChart) {
    apiPerformanceChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}ms'
        }
      },
      series: [
        {
          name: 'API响应时间',
          type: 'line',
          stack: 'Total',
          data: [100, 120, 140, 160, 180, apiResponseTime.value]
        },
        {
          name: 'Redis命中率',
          type: 'line',
          stack: 'Total',
          data: [90, 92, 94, 96, 98, redisHitRate.value]
        }
      ]
    })
  }
}
</script>

<style scoped>
.monitor-dashboard {
  padding: 0;
  min-height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.chart-container {
  width: 100%;
  height: 280px;
  min-height: 250px;
}

/* 告警卡片样式 */
.alert-card {
  margin-bottom: 24px;
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
}

.alert-card.alert-active {
  border-color: #ef4444;
  box-shadow: 0 12px 40px rgba(239, 68, 68, 0.3);
  background: rgba(39, 39, 42, 0.9);
}

.alert-content {
  padding: 20px;
  min-height: 120px;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.alert-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(4px);
}

.alert-item:nth-child(1) {
  border-left-color: #ef4444;
}

.alert-item:nth-child(2) {
  border-left-color: #f59e0b;
}

.alert-tag {
  margin-right: 12px;
  flex-shrink: 0;
}

.alert-message {
  flex: 1;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin-right: 12px;
}

.alert-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

/* 时间范围选择器样式 */
.time-range-select {
  width: 120px;
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(59, 130, 246, 0.2);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 24px;
}

:deep(.el-card:hover) {
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.3);
  transform: translateY(-4px);
  border-color: rgba(59, 130, 246, 0.4);
}

:deep(.el-card__header) {
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  background: rgba(15, 23, 42, 0.6);
  border-radius: 12px 12px 0 0;
  padding: 16px;
}

/* 统计组件样式 */
:deep(.el-statistic) {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 8px;
  padding: 16px;
  border: 1px solid rgba(59, 130, 246, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

:deep(.el-statistic:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.3);
  border-color: rgba(59, 130, 246, 0.4);
  background: rgba(15, 23, 42, 0.8);
}

:deep(.el-statistic__title) {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  margin-bottom: 8px;
}

:deep(.el-statistic__value) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

:deep(.el-statistic__suffix) {
  color: rgba(59, 130, 246, 0.8);
  font-size: 14px;
  font-weight: 600;
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(67, 97, 238, 0.4);
}

:deep(.el-button:active) {
  transform: translateY(0);
}

/* 选择器样式 */
:deep(.el-select) {
  border-radius: 8px;
}

:deep(.el-select__wrapper) {
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select__wrapper:hover) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

:deep(.el-select__placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

:deep(.el-select__caret) {
  color: rgba(255, 255, 255, 0.6);
}

/* 空状态样式 */
:deep(.el-empty) {
  color: rgba(255, 255, 255, 0.4);
}

:deep(.el-empty__description) {
  color: rgba(255, 255, 255, 0.4);
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 16px;
  }
  
  :deep(.el-statistic) {
    padding: 16px;
  }
  
  :deep(.el-statistic__value) {
    font-size: 18px;
  }
  
  .chart-container {
    height: 250px;
  }
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 12px;
  }
  
  :deep(.el-statistic) {
    padding: 12px;
  }
  
  :deep(.el-statistic__title) {
    font-size: 11px;
    margin-bottom: 6px;
  }
  
  :deep(.el-statistic__value) {
    font-size: 16px;
  }
  
  .chart-container {
    height: 200px;
    min-height: 180px;
  }
  
  :deep(.el-card) {
    margin-bottom: 16px;
  }
  
  .alert-card {
    margin-bottom: 16px;
  }
}
</style>