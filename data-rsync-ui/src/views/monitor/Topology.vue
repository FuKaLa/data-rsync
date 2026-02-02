<template>
  <div class="monitor-topology">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>同步链路拓扑</span>
        </div>
      </template>
      <div ref="topologyChartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { monitorApi } from '@/api'

const topologyChartRef = ref<HTMLElement>()
let topologyChart: echarts.ECharts | null = null

onMounted(async () => {
  await loadTopologyData()
  nextTick(() => {
    initTopologyChart()
  })
})

const loadTopologyData = async () => {
  try {
    const topologyData = await monitorApi.getTopology()
    console.log('Topology Data:', topologyData)
    // 这里可以根据实际返回的数据更新图表
  } catch (error) {
    console.error('Failed to load topology data:', error)
  }
}

const initTopologyChart = () => {
  if (topologyChartRef.value) {
    topologyChart = echarts.init(topologyChartRef.value)
    topologyChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}'
      },
      animationDurationUpdate: 1500,
      animationEasingUpdate: 'quinticInOut',
      series: [
        {
          type: 'graph',
          layout: 'force',
          force: {
            repulsion: 100,
            edgeLength: [80, 150]
          },
          roam: true,
          label: {
            show: true,
            position: 'right',
            formatter: '{b}'
          },
          data: [
            {
              name: '数据源',
              symbolSize: 60,
              itemStyle: {
                color: '#409EFF'
              }
            },
            {
              name: '日志监听',
              symbolSize: 50,
              itemStyle: {
                color: '#67C23A'
              }
            },
            {
              name: '数据处理',
              symbolSize: 50,
              itemStyle: {
                color: '#E6A23C'
              }
            },
            {
              name: '向量化',
              symbolSize: 50,
              itemStyle: {
                color: '#F56C6C'
              }
            },
            {
              name: 'Milvus同步',
              symbolSize: 50,
              itemStyle: {
                color: '#909399'
              }
            },
            {
              name: 'Milvus存储',
              symbolSize: 60,
              itemStyle: {
                color: '#C0C4CC'
              }
            }
          ],
          links: [
            {
              source: '数据源',
              target: '日志监听',
              label: {
                show: true,
                formatter: '日志流'
              },
              lineStyle: {
                width: 2,
                color: '#409EFF'
              }
            },
            {
              source: '日志监听',
              target: '数据处理',
              label: {
                show: true,
                formatter: '原始数据'
              },
              lineStyle: {
                width: 2,
                color: '#67C23A'
              }
            },
            {
              source: '数据处理',
              target: '向量化',
              label: {
                show: true,
                formatter: '结构化数据'
              },
              lineStyle: {
                width: 2,
                color: '#E6A23C'
              }
            },
            {
              source: '向量化',
              target: 'Milvus同步',
              label: {
                show: true,
                formatter: '向量数据'
              },
              lineStyle: {
                width: 2,
                color: '#F56C6C'
              }
            },
            {
              source: 'Milvus同步',
              target: 'Milvus存储',
              label: {
                show: true,
                formatter: '写入'
              },
              lineStyle: {
                width: 2,
                color: '#909399'
              }
            }
          ]
        }
      ]
    })
  }
}
</script>

<style scoped>
.monitor-topology {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 600px;
}
</style>