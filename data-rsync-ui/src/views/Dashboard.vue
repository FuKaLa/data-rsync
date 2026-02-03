<template>
  <div class="dashboard" v-animate="'fadeIn'">
    <!-- 页面标题 -->
    <div class="page-header" v-animate="'slideUp'">
      <h1 class="page-title">数据同步系统</h1>
      <p class="page-description">实时监控系统状态，高效管理数据同步任务</p>
      <div class="header-actions">
        <el-button @click="refreshData" plain class="refresh-btn">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 概览卡片 -->
    <div class="overview-section" v-animate="'fadeIn'" style="animation-delay: 0.2s">
      <div class="overview-grid">
        <div class="stat-card" v-animate="'slideUp'" style="animation-delay: 0.2s">
          <div class="stat-icon primary">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">数据源数量</div>
            <div class="stat-value">{{ dataSourcesCount }}</div>
            <div class="stat-trend positive">+2 本周</div>
          </div>
        </div>
        <div class="stat-card" v-animate="'slideUp'" style="animation-delay: 0.3s">
          <div class="stat-icon success">
            <el-icon><RefreshLeft /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">同步任务</div>
            <div class="stat-value">{{ tasksCount }}</div>
            <div class="stat-trend positive">+1 今日</div>
          </div>
        </div>
        <div class="stat-card" v-animate="'slideUp'" style="animation-delay: 0.4s">
          <div class="stat-icon warning">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">健康数据源</div>
            <div class="stat-value">{{ healthyDataSources }}</div>
            <div class="stat-trend neutral">{{ Math.round((healthyDataSources / dataSourcesCount) * 100) }}% 健康率</div>
          </div>
        </div>
        <div class="stat-card" v-animate="'slideUp'" style="animation-delay: 0.5s">
          <div class="stat-icon info">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">运行中任务</div>
            <div class="stat-value">{{ runningTasks }}</div>
            <div class="stat-trend positive">{{ Math.round((runningTasks / tasksCount) * 100) }}% 运行率</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section" v-animate="'fadeIn'" style="animation-delay: 0.6s">
      <el-row :gutter="24">
        <el-col :span="12">
          <div class="chart-card" v-animate="'slideRight'" style="animation-delay: 0.6s">
            <div class="chart-header">
              <h3 class="chart-title">同步状态</h3>
              <el-dropdown>
                <el-button size="small" plain>
                  <el-icon><Setting /></el-icon>
                  配置
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>刷新图表</el-dropdown-item>
                    <el-dropdown-item>导出数据</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div ref="statusChartRef" class="chart-container"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-card" v-animate="'slideLeft'" style="animation-delay: 0.7s">
            <div class="chart-header">
              <h3 class="chart-title">系统资源</h3>
              <el-dropdown>
                <el-button size="small" plain>
                  <el-icon><Setting /></el-icon>
                  配置
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>刷新图表</el-dropdown-item>
                    <el-dropdown-item>导出数据</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div ref="resourceChartRef" class="chart-container"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 最近任务 -->
    <div class="tasks-section" v-animate="'fadeIn'" style="animation-delay: 0.8s">
      <div class="section-header">
        <h3 class="section-title">最近任务</h3>
        <el-button type="primary" plain class="add-task-btn">
          <el-icon><Plus /></el-icon>
          创建任务
        </el-button>
      </div>
      <div class="tasks-table" v-animate="'slideUp'" style="animation-delay: 0.9s">
        <el-table :data="recentTasks" style="width: 100%" class="custom-table">
          <el-table-column prop="name" label="任务名称" width="200">
            <template #default="scope">
              <div class="task-name">{{ scope.row.name }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" class="status-tag">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="180" />
          <el-table-column prop="progress" label="进度">
            <template #default="scope">
              <div class="progress-info">
                <el-progress 
                  :percentage="parseInt(scope.row.progress)" 
                  :format="() => scope.row.progress" 
                  :color="getProgressColor(scope.row.progress)" 
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary" 
                class="view-btn"
                @click="viewTask(scope.row)"
              >
                <el-icon><View /></el-icon>
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 系统告警 -->
    <div class="alerts-section" v-animate="'fadeIn'" style="animation-delay: 1s">
      <div class="section-header">
        <h3 class="section-title">系统告警</h3>
        <el-badge :value="alerts.length" class="alerts-badge" />
      </div>
      <div class="alerts-list" v-animate="'slideUp'" style="animation-delay: 1.1s">
        <div v-for="(alert, index) in alerts" :key="index" class="alert-item" v-animate="'slideUp'" :style="{ animationDelay: (1.1 + index * 0.1) + 's' }">
          <div class="alert-icon" :class="alert.level">
            <el-icon v-if="alert.level === 'warning'" ><Warning /></el-icon>
            <el-icon v-else-if="alert.level === 'danger'" ><Close /></el-icon>
            <el-icon v-else ><InfoFilled /></el-icon>
          </div>
          <div class="alert-content">
            <div class="alert-title">{{ alert.title }}</div>
            <div class="alert-message">{{ alert.message }}</div>
            <div class="alert-time">{{ alert.time }}</div>
          </div>
          <el-button size="small" plain class="alert-action">处理</el-button>
        </div>
        <div v-if="alerts.length === 0" class="no-alerts">
          <el-icon class="no-alerts-icon"><CheckCircle /></el-icon>
          <p>暂无告警信息</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { 
  DataAnalysis, 
  RefreshLeft, 
  Check, 
  Timer, 
  Refresh, 
  Setting, 
  Plus, 
  View, 
  Warning, 
  Close, 
  InfoFilled 
} from '@element-plus/icons-vue'
import { monitorApi } from '@/api'
import { ElMessage } from 'element-plus'

const dataSourcesCount = ref(10)
const tasksCount = ref(5)
const healthyDataSources = ref(8)
const runningTasks = ref(3)

const recentTasks = ref([
  { name: '用户数据同步', status: '运行中', createdTime: '2026-02-02 10:00:00', progress: '60%' },
  { name: '产品数据同步', status: '已完成', createdTime: '2026-02-02 09:30:00', progress: '100%' },
  { name: '订单数据同步', status: '暂停', createdTime: '2026-02-02 09:00:00', progress: '30%' }
])

const alerts = ref([
  {
    level: 'warning',
    title: '数据源连接异常',
    message: 'MySQL数据源 "user_db" 连接超时，请检查网络连接',
    time: '2026-02-02 10:15:00'
  },
  {
    level: 'danger',
    title: '同步任务失败',
    message: '任务 "产品数据同步" 执行失败，错误码：5001',
    time: '2026-02-02 09:45:00'
  }
])

const statusChartRef = ref<HTMLElement>()
const resourceChartRef = ref<HTMLElement>()
let statusChart: echarts.ECharts | null = null
let resourceChart: echarts.ECharts | null = null

onMounted(async () => {
  await loadDashboardData()
  nextTick(() => {
    initStatusChart()
    initResourceChart()
    // 监听窗口大小变化，自适应图表
    window.addEventListener('resize', handleResize)
  })
})

const handleResize = () => {
  statusChart?.resize()
  resourceChart?.resize()
}

const refreshData = async () => {
  try {
    ElMessage({
      message: '正在刷新数据...',
      type: 'info',
      duration: 1000
    })
    await loadDashboardData()
    // 刷新图表
    initStatusChart()
    initResourceChart()
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('Failed to refresh data:', error)
    ElMessage.error('数据刷新失败')
  }
}

const loadDashboardData = async () => {
  try {
    const dashboardData = await monitorApi.getDashboard()
    console.log('Dashboard Data:', dashboardData)
    // 这里可以根据实际返回的数据更新页面
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case '运行中':
      return 'primary'
    case '已完成':
      return 'success'
    case '暂停':
      return 'warning'
    case '失败':
      return 'danger'
    default:
      return 'info'
  }
}

const getProgressColor = (progress: string) => {
  const value = parseInt(progress)
  if (value < 30) {
    return '#f56c6c'
  } else if (value < 70) {
    return '#e6a23c'
  } else {
    return '#67c23a'
  }
}

const viewTask = (task: any) => {
  console.log('View task:', task)
  // 这里可以跳转到任务详情页面
  ElMessage.info(`查看任务：${task.name}`)
}

const initStatusChart = () => {
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
    statusChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#409eff',
        borderWidth: 1,
        textStyle: {
          color: '#303133'
        },
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
      },
      legend: {
        top: '5%',
        left: 'center',
        textStyle: {
          color: '#606266'
        }
      },
      series: [
        {
          name: '同步状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 12,
            borderColor: '#fff',
            borderWidth: 3,
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.1)'
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 24,
              fontWeight: 'bold',
              color: '#303133'
            },
            itemStyle: {
              shadowBlur: 20,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.25)'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { 
              value: 3, 
              name: '运行中',
              itemStyle: { color: '#409eff' }
            },
            { 
              value: 1, 
              name: '已完成',
              itemStyle: { color: '#67c23a' }
            },
            { 
              value: 1, 
              name: '暂停',
              itemStyle: { color: '#e6a23c' }
            }
          ]
        }
      ]
    })
  }
}

const initResourceChart = () => {
  if (resourceChartRef.value) {
    resourceChart = echarts.init(resourceChartRef.value)
    resourceChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.1)'
        },
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#409eff',
        borderWidth: 1,
        textStyle: {
          color: '#303133'
        },
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['CPU', '内存', '磁盘', '网络'],
        axisLine: {
          lineStyle: {
            color: '#dcdfe6'
          }
        },
        axisLabel: {
          color: '#606266'
        }
      },
      yAxis: {
        type: 'value',
        max: 100,
        axisLabel: {
          formatter: '{value}%',
          color: '#606266'
        },
        axisLine: {
          lineStyle: {
            color: '#dcdfe6'
          }
        },
        splitLine: {
          lineStyle: {
            color: '#f0f0f0'
          }
        }
      },
      series: [
        {
          name: '使用率',
          type: 'bar',
          barWidth: '60%',
          data: [15, 45, 30, 20],
          itemStyle: {
            borderRadius: [8, 8, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#667eea' }
            ]),
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(64, 158, 255, 0.5)'
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#667eea' },
                { offset: 1, color: '#409eff' }
              ]),
              shadowBlur: 20,
              shadowOffsetX: 0,
              shadowColor: 'rgba(64, 158, 255, 0.7)'
            }
          }
        }
      ]
    })
  }
}
</script>

<style scoped>
/* 动画关键帧 */
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

@keyframes slideRight {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideLeft {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

@keyframes countUp {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 动画指令 */
[v-animate] {
  animation-duration: 0.8s;
  animation-fill-mode: both;
  animation-timing-function: cubic-bezier(0.16, 1, 0.3, 1);
}

[v-animate="fadeIn"] {
  animation-name: fadeIn;
}

[v-animate="slideUp"] {
  animation-name: slideUp;
}

[v-animate="slideRight"] {
  animation-name: slideRight;
}

[v-animate="slideLeft"] {
  animation-name: slideLeft;
}

/* 主容器样式 */
.dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: 100vh;
  background-size: 200% 200%;
  animation: gradientShift 15s ease infinite;
}

/* 页面标题样式 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shine 3s infinite;
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 100%; }
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(90deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
  margin-top: 0;
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0 0 24px 0;
  opacity: 0.9;
}

.header-actions {
  margin-top: 16px;
}

.refresh-btn {
  border-radius: 10px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.3);
  color: #409eff;
}

.refresh-btn:hover {
  background: rgba(64, 158, 255, 0.1);
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
}

/* 概览部分样式 */
.overview-section {
  margin-bottom: 32px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
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
  background: linear-gradient(135deg, #409eff, #667eea);
  border-radius: 16px 0 0 16px;
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  animation: pulse 2s infinite;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  font-size: 28px;
  color: #fff;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-icon.primary {
  background: linear-gradient(135deg, #409eff, #667eea);
}

.stat-icon.success {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stat-icon.info {
  background: linear-gradient(135deg, #909399, #c0c4cc);
}

.stat-content {
  position: relative;
  z-index: 1;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 8px;
  opacity: 0.9;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(90deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  animation: countUp 2s ease-out forwards;
}

.stat-trend {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
}

.stat-trend.positive {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.stat-trend.negative {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.stat-trend.neutral {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

/* 图表部分样式 */
.charts-section {
  margin-bottom: 32px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.chart-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.chart-container {
  width: 100%;
  height: 360px;
  border-radius: 12px;
  overflow: hidden;
}

/* 任务部分样式 */
.tasks-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(90deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.add-task-btn {
  border-radius: 10px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  background: linear-gradient(90deg, #409eff, #667eea);
  border: none;
  color: #fff;
}

.add-task-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
}

.tasks-table {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.custom-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

:deep(.el-table__header-wrapper) {
  background: linear-gradient(90deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px 12px 0 0;
}

:deep(.el-table__header th) {
  font-weight: 600;
  color: #303133;
  background: transparent;
  border-bottom: 3px solid #409eff;
  padding: 16px;
}

:deep(.el-table__row) {
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

:deep(.el-table__row:hover) {
  background: rgba(64, 158, 255, 0.05) !important;
  border-left-color: #409eff;
  transform: translateX(4px);
}

:deep(.el-table__cell) {
  padding: 18px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

:deep(.el-table__cell:hover) {
  color: #409eff;
}

.task-name {
  font-weight: 500;
  color: #303133;
}

.status-tag {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-tag:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.progress-info {
  margin-top: 4px;
}

:deep(.el-progress) {
  margin-top: 4px;
}

:deep(.el-progress__bar) {
  border-radius: 12px;
  overflow: hidden;
  height: 8px;
}

:deep(.el-progress__bar-inner) {
  border-radius: 12px;
  transition: width 1s ease;
  box-shadow: 0 0 10px currentColor;
}

.view-btn {
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  background: linear-gradient(90deg, #409eff, #667eea);
  border: none;
  color: #fff;
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 告警部分样式 */
.alerts-section {
  margin-bottom: 32px;
}

.alerts-badge {
  background: linear-gradient(90deg, #f56c6c, #f78989);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 0 8px;
  border-radius: 10px;
  line-height: 1.5;
}

.alerts-list {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: rgba(245, 108, 108, 0.05);
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
  border-left: 4px solid #f56c6c;
}

.alert-item:hover {
  transform: translateX(8px);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.15);
}

.alert-item:last-child {
  margin-bottom: 0;
}

.alert-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
  margin-top: 2px;
}

.alert-icon.warning {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.4);
}

.alert-icon.danger {
  background: linear-gradient(135deg, #f56c6c, #f78989);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
}

.alert-icon.info {
  background: linear-gradient(135deg, #409eff, #667eea);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.alert-message {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.4;
}

.alert-time {
  font-size: 12px;
  color: #909399;
}

.alert-action {
  border-radius: 8px;
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.3);
  color: #409eff;
  flex-shrink: 0;
  margin-top: 4px;
}

.alert-action:hover {
  background: rgba(64, 158, 255, 0.1);
  border-color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.no-alerts {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-alerts-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 16px;
  opacity: 0.8;
}

.no-alerts p {
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .page-title {
    font-size: 28px;
  }

  .overview-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-value {
    font-size: 28px;
  }

  .chart-container {
    height: 280px;
  }

  .chart-card {
    padding: 20px;
  }

  .tasks-table {
    padding: 20px;
  }

  :deep(.el-table__cell) {
    padding: 12px;
  }

  .alerts-list {
    padding: 20px;
  }

  .alert-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .alert-action {
    align-self: flex-end;
    margin-top: 0;
  }
}

@media (max-width: 480px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .chart-container {
    height: 240px;
  }

  .page-title {
    font-size: 24px;
  }

  .stat-value {
    font-size: 24px;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 5px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #409eff, #667eea);
  border-radius: 5px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #667eea, #409eff);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}
</style>