<template>
  <div class="dashboard">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-grid"></div>
      <div class="bg-glow"></div>
      <div class="bg-orbs">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
      </div>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
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
    <div class="overview-section" style="animation-delay: 0.2s">
      <div class="overview-grid">
        <div class="stat-card" style="animation-delay: 0.2s" @mouseenter="cardHover = true" @mouseleave="cardHover = false">
          <div class="stat-icon primary">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">数据源数量</div>
            <div class="stat-value">{{ dataSourcesCount }}</div>
            <div class="stat-trend positive">+2 本周</div>
          </div>
          <div class="stat-card-glow"></div>
        </div>
        <div class="stat-card" style="animation-delay: 0.3s" @mouseenter="cardHover = true" @mouseleave="cardHover = false">
          <div class="stat-icon success">
            <el-icon><RefreshLeft /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">同步任务</div>
            <div class="stat-value">{{ tasksCount }}</div>
            <div class="stat-trend positive">+1 今日</div>
          </div>
          <div class="stat-card-glow"></div>
        </div>
        <div class="stat-card" style="animation-delay: 0.4s" @mouseenter="cardHover = true" @mouseleave="cardHover = false">
          <div class="stat-icon warning">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">健康数据源</div>
            <div class="stat-value">{{ healthyDataSources }}</div>
            <div class="stat-trend neutral">{{ Math.round((healthyDataSources / dataSourcesCount) * 100) }}% 健康率</div>
          </div>
          <div class="stat-card-glow"></div>
        </div>
        <div class="stat-card" style="animation-delay: 0.5s" @mouseenter="cardHover = true" @mouseleave="cardHover = false">
          <div class="stat-icon info">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">运行中任务</div>
            <div class="stat-value">{{ runningTasks }}</div>
            <div class="stat-trend positive">{{ Math.round((runningTasks / tasksCount) * 100) }}% 运行率</div>
          </div>
          <div class="stat-card-glow"></div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section" style="animation-delay: 0.6s">
      <el-row :gutter="24">
        <el-col :span="12">
          <div class="chart-card" style="animation-delay: 0.6s">
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
            <div class="chart-card-glow"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-card" style="animation-delay: 0.7s">
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
            <div class="chart-card-glow"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 最近任务 -->
    <div class="tasks-section" style="animation-delay: 0.8s">
      <div class="section-header">
        <h3 class="section-title">最近任务</h3>
        <el-button type="primary" plain class="add-task-btn">
          <el-icon><Plus /></el-icon>
          创建任务
        </el-button>
      </div>
      <div class="tasks-table" style="animation-delay: 0.9s">
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
    <div class="alerts-section" style="animation-delay: 1s">
      <div class="section-header">
        <h3 class="section-title">系统告警</h3>
        <el-badge :value="alerts.length" class="alerts-badge" />
      </div>
      <div class="alerts-list" style="animation-delay: 1.1s">
        <div v-for="(alert, index) in alerts" :key="index" class="alert-item" :style="{ animationDelay: (1.1 + index * 0.1) + 's' }">
          <div class="alert-icon" :class="alert.level">
            <el-icon v-if="alert.level === 'warning'">
              <Warning />
            </el-icon>
            <el-icon v-else-if="alert.level === 'danger'">
              <Close />
            </el-icon>
            <el-icon v-else>
              <InfoFilled />
            </el-icon>
          </div>
          <div class="alert-content">
            <div class="alert-title">{{ alert.title }}</div>
            <div class="alert-message">{{ alert.message }}</div>
            <div class="alert-time">{{ alert.time }}</div>
          </div>
          <el-button size="small" plain class="alert-action">处理</el-button>
        </div>
        <div v-if="alerts.length === 0" class="no-alerts">
          <el-icon class="no-alerts-icon"><CircleCheck /></el-icon>
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
  InfoFilled, 
  CircleCheck 
} from '@element-plus/icons-vue'
import { monitorApi } from '@/api'
import { ElMessage } from 'element-plus'

const dataSourcesCount = ref(10)
const tasksCount = ref(5)
const healthyDataSources = ref(8)
const runningTasks = ref(3)
const cardHover = ref(false)

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
    const response = await monitorApi.getMetrics()
    console.log('Dashboard Data:', response)
    
    // 使用后端返回的实际数据更新页面
    if (response && typeof response === 'object') {
      const data = response as any
      // 更新概览卡片数据
      if (data.dataSourcesCount !== undefined) {
        dataSourcesCount.value = data.dataSourcesCount
      }
      if (data.tasksCount !== undefined) {
        tasksCount.value = data.tasksCount
      }
      if (data.healthyDataSources !== undefined) {
        healthyDataSources.value = data.healthyDataSources
      }
      if (data.runningTasks !== undefined) {
        runningTasks.value = data.runningTasks
      }
      
      // 更新最近任务数据
      if (data.recentTasks !== undefined) {
        recentTasks.value = data.recentTasks
      }
      
      // 更新告警数据
      if (data.alerts !== undefined) {
        alerts.value = data.alerts
      }
    }
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
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  min-height: 100vh;
  background-size: 200% 200%;
  animation: gradientShift 15s ease infinite;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

/* 背景网格 */
.bg-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
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

/* 背景光晕 */
.bg-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  right: -50%;
  bottom: -50%;
  background: radial-gradient(circle at center, rgba(64, 158, 255, 0.1) 0%, transparent 70%);
  animation: glowPulse 8s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.1);
  }
}

/* 背景球体 */
.bg-orbs {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  animation: orbFloat 20s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: rgba(64, 158, 255, 0.2);
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  background: rgba(103, 194, 58, 0.2);
  top: 60%;
  right: 15%;
  animation-delay: -7s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: rgba(230, 162, 60, 0.2);
  bottom: 10%;
  left: 30%;
  animation-delay: -14s;
}

@keyframes orbFloat {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* 页面标题样式 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 20px;
  backdrop-filter: blur(15px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.2);
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.3), transparent);
  animation: shine 3s infinite;
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 100%; }
}

.page-title {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(90deg, #409eff, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 20px rgba(64, 158, 255, 0.5);
  margin-top: 0;
  position: relative;
  z-index: 1;
}

.page-description {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 24px 0;
  opacity: 0.9;
  position: relative;
  z-index: 1;
}

.header-actions {
  margin-top: 16px;
  position: relative;
  z-index: 1;
}

.refresh-btn {
  border-radius: 12px;
  padding: 12px 28px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.4);
  color: #409eff;
  background: rgba(64, 158, 255, 0.05);
}

.refresh-btn:hover {
  background: rgba(64, 158, 255, 0.15);
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.4);
}

/* 概览部分样式 */
.overview-section {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.stat-card {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 18px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(64, 158, 255, 0.1);
  backdrop-filter: blur(15px);
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.2);
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
  border-radius: 18px 0 0 18px;
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 48px rgba(64, 158, 255, 0.3), 0 0 0 1px rgba(64, 158, 255, 0.2);
  animation: pulse 2s infinite;
  border-color: rgba(64, 158, 255, 0.4);
}

.stat-card-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.6s ease;
  opacity: 0;
}

.stat-card:hover .stat-card-glow {
  width: 300px;
  height: 300px;
  opacity: 1;
}

.stat-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  font-size: 32px;
  color: #fff;
  transition: all 0.3s ease;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.stat-icon::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shine 2s infinite;
}

.stat-icon.primary {
  background: linear-gradient(135deg, #409eff, #667eea);
  box-shadow: 0 8px 32px rgba(64, 158, 255, 0.4);
}

.stat-icon.success {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  box-shadow: 0 8px 32px rgba(103, 194, 58, 0.4);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
  box-shadow: 0 8px 32px rgba(230, 162, 60, 0.4);
}

.stat-icon.info {
  background: linear-gradient(135deg, #909399, #c0c4cc);
  box-shadow: 0 8px 32px rgba(144, 147, 153, 0.4);
}

.stat-icon:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.3);
}

.stat-content {
  position: relative;
  z-index: 1;
}

.stat-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
  margin-bottom: 12px;
  opacity: 0.9;
}

.stat-value {
  font-size: 42px;
  font-weight: 700;
  background: linear-gradient(90deg, #409eff, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  animation: countUp 2s ease-out forwards;
  text-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
}

.stat-trend {
  font-size: 13px;
  font-weight: 500;
  padding: 6px 16px;
  border-radius: 16px;
  display: inline-block;
  transition: all 0.3s ease;
}

.stat-trend.positive {
  background: rgba(103, 194, 58, 0.15);
  color: #67c23a;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
}

.stat-trend.negative {
  background: rgba(245, 108, 108, 0.15);
  color: #f56c6c;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

.stat-trend.neutral {
  background: rgba(144, 147, 153, 0.15);
  color: #909399;
  box-shadow: 0 2px 8px rgba(144, 147, 153, 0.2);
}

.stat-trend:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

/* 图表部分样式 */
.charts-section {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.chart-card {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(64, 158, 255, 0.1);
  backdrop-filter: blur(15px);
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.chart-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(64, 158, 255, 0.3), 0 0 0 1px rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.4);
}

.chart-card-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.6s ease;
  opacity: 0;
}

.chart-card:hover .chart-card-glow {
  width: 400px;
  height: 400px;
  opacity: 1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(90deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  text-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
}

.chart-container {
  width: 100%;
  height: 360px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 任务部分样式 */
.tasks-section {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(90deg, #409eff, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  text-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
}

.add-task-btn {
  border-radius: 12px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  background: linear-gradient(90deg, #409eff, #667eea);
  border: none;
  color: #fff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
}

.add-task-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(64, 158, 255, 0.4);
  background: linear-gradient(90deg, #667eea, #409eff);
}

.tasks-table {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(64, 158, 255, 0.1);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.custom-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

:deep(.el-table__header-wrapper) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, rgba(102, 126, 234, 0.1) 100%);
  border-radius: 12px 12px 0 0;
}

:deep(.el-table__header th) {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  background: transparent;
  border-bottom: 3px solid #409eff;
  padding: 16px;
}

:deep(.el-table__row) {
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
  background: rgba(15, 23, 42, 0.6) !important;
}

:deep(.el-table__row:hover) {
  background: rgba(64, 158, 255, 0.1) !important;
  border-left-color: #409eff;
  transform: translateX(4px);
}

:deep(.el-table__cell) {
  padding: 18px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-table__cell:hover) {
  color: #409eff;
}

.task-name {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.status-tag {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.status-tag:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
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
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.view-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  background: linear-gradient(90deg, #667eea, #409eff);
}

/* 告警部分样式 */
.alerts-section {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.alerts-badge {
  background: linear-gradient(90deg, #f56c6c, #f78989);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 0 8px;
  border-radius: 10px;
  line-height: 1.5;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
  margin-left: 8px;
}

.alerts-list {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(64, 158, 255, 0.1);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.alert-item.warning {
  background: rgba(230, 162, 60, 0.1);
  border-left: 4px solid #e6a23c;
}

.alert-item.danger {
  background: rgba(245, 108, 108, 0.1);
  border-left: 4px solid #f56c6c;
}

.alert-item.info {
  background: rgba(64, 158, 255, 0.1);
  border-left: 4px solid #409eff;
}

.alert-item.warning:hover {
  transform: translateX(8px);
  box-shadow: 0 6px 24px rgba(230, 162, 60, 0.3);
  background: rgba(230, 162, 60, 0.15);
}

.alert-item.danger:hover {
  transform: translateX(8px);
  box-shadow: 0 6px 24px rgba(245, 108, 108, 0.3);
  background: rgba(245, 108, 108, 0.15);
}

.alert-item.info:hover {
  transform: translateX(8px);
  box-shadow: 0 6px 24px rgba(64, 158, 255, 0.3);
  background: rgba(64, 158, 255, 0.15);
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
  position: relative;
  overflow: hidden;
}

.alert-icon::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shine 2s infinite;
}

.alert-icon.warning {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
  box-shadow: 0 4px 16px rgba(230, 162, 60, 0.4);
}

.alert-icon.danger {
  background: linear-gradient(135deg, #f56c6c, #f78989);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.4);
}

.alert-icon.info {
  background: linear-gradient(135deg, #409eff, #667eea);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 4px;
}

.alert-message {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
  line-height: 1.4;
}

.alert-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.alert-action {
  border-radius: 8px;
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.4);
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
  flex-shrink: 0;
  margin-top: 4px;
}

.alert-action:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.no-alerts {
  text-align: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.5);
}

.no-alerts-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 16px;
  opacity: 0.8;
  filter: drop-shadow(0 4px 12px rgba(103, 194, 58, 0.3));
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