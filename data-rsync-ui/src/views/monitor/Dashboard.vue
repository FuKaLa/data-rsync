<template>
  <div class="monitor-dashboard">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>监控面板</span>
        </div>
      </template>
      <div class="metrics-grid">
        <el-statistic :value="syncSuccessRate" title="同步成功率" suffix="%" />
        <el-statistic :value="dataDelay" title="数据延迟" suffix="s" />
        <el-statistic :value="milvusWriteQps" title="Milvus写入QPS" />
        <el-statistic :value="runningTasks" title="运行中任务" />
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>同步成功率趋势</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { monitorApi } from '@/api'

const syncSuccessRate = ref(99.5)
const dataDelay = ref(1.2)
const milvusWriteQps = ref(1200)
const runningTasks = ref(5)

const successRateChartRef = ref<HTMLElement>()
const delayChartRef = ref<HTMLElement>()
const qpsChartRef = ref<HTMLElement>()
const taskStatusChartRef = ref<HTMLElement>()

let successRateChart: echarts.ECharts | null = null
let delayChart: echarts.ECharts | null = null
let qpsChart: echarts.ECharts | null = null
let taskStatusChart: echarts.ECharts | null = null

onMounted(async () => {
  await loadMonitorData()
  nextTick(() => {
    initSuccessRateChart()
    initDelayChart()
    initQpsChart()
    initTaskStatusChart()
  })
})

const loadMonitorData = async () => {
  try {
    const response = await monitorApi.getMetrics()
    console.log('Business Metrics:', response)
    
    // 使用后端返回的实际数据更新页面
    if (response && typeof response === 'object') {
      const data = response as any
      // 更新监控指标
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
  } catch (error) {
    console.error('Failed to load monitor data:', error)
  }
}

const initSuccessRateChart = () => {
  if (successRateChartRef.value) {
    successRateChart = echarts.init(successRateChartRef.value)
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
          data: [99.2, 99.5, 99.8, 99.6, 99.7, 99.5]
        }
      ]
    })
  }
}

const initDelayChart = () => {
  if (delayChartRef.value) {
    delayChart = echarts.init(delayChartRef.value)
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
          data: [1.5, 1.2, 0.8, 1.0, 1.3, 1.2]
        }
      ]
    })
  }
}

const initQpsChart = () => {
  if (qpsChartRef.value) {
    qpsChart = echarts.init(qpsChartRef.value)
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
          data: [800, 1000, 1500, 1300, 1200, 1200]
        }
      ]
    })
  }
}

const initTaskStatusChart = () => {
  if (taskStatusChartRef.value) {
    taskStatusChart = echarts.init(taskStatusChartRef.value)
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
            { value: 5, name: '运行中' },
            { value: 3, name: '已完成' },
            { value: 2, name: '暂停' },
            { value: 1, name: '失败' }
          ]
        }
      ]
    })
  }
}
</script>

<style scoped>
.monitor-dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 300px;
}
</style>