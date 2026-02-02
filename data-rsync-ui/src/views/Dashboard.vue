<template>
  <div class="dashboard">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统概览</span>
        </div>
      </template>
      <div class="overview-grid">
        <el-statistic :value="dataSourcesCount" title="数据源数量" />
        <el-statistic :value="tasksCount" title="同步任务" />
        <el-statistic :value="healthyDataSources" title="健康数据源" />
        <el-statistic :value="runningTasks" title="运行中任务" />
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>同步状态</span>
            </div>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统资源</span>
            </div>
          </template>
          <div ref="resourceChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>最近任务</span>
        </div>
      </template>
      <el-table :data="recentTasks" style="width: 100%">
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="createdTime" label="创建时间" />
        <el-table-column prop="progress" label="进度" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" type="primary">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { monitorApi } from '@/api'

const dataSourcesCount = ref(10)
const tasksCount = ref(5)
const healthyDataSources = ref(8)
const runningTasks = ref(3)

const recentTasks = ref([
  { name: '用户数据同步', status: '运行中', createdTime: '2026-02-02 10:00:00', progress: '60%' },
  { name: '产品数据同步', status: '已完成', createdTime: '2026-02-02 09:30:00', progress: '100%' },
  { name: '订单数据同步', status: '暂停', createdTime: '2026-02-02 09:00:00', progress: '30%' }
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
  })
})

const loadDashboardData = async () => {
  try {
    const dashboardData = await monitorApi.getDashboard()
    console.log('Dashboard Data:', dashboardData)
    // 这里可以根据实际返回的数据更新页面
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  }
}

const initStatusChart = () => {
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
    statusChart.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        top: '5%',
        left: 'center'
      },
      series: [
        {
          name: '同步状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: 3, name: '运行中' },
            { value: 1, name: '已完成' },
            { value: 1, name: '暂停' }
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
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['CPU', '内存', '磁盘', '网络']
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
          name: '使用率',
          type: 'bar',
          barWidth: '60%',
          data: [15, 45, 30, 20]
        }
      ]
    })
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.chart-container {
  width: 100%;
  height: 300px;
}
</style>