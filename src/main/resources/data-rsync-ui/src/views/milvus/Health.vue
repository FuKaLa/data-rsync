<template>
  <div class="milvus-health">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>健康状态</span>
        </div>
      </template>
      <div class="health-grid">
        <el-statistic :value="healthData.status" title="状态">
          <template #suffix>
            <el-tag :type="healthData.status === 'healthy' ? 'success' : 'danger'">
              {{ healthData.status }}
            </el-tag>
          </template>
        </el-statistic>
        <el-statistic :value="healthData.version" title="版本" />
        <el-statistic :value="healthData.memoryUsage" title="内存使用" />
        <el-statistic :value="healthData.cpuUsage" title="CPU使用" />
        <el-statistic :value="healthData.connections" title="连接数" />
      </div>
      <el-button type="primary" @click="refreshHealth" style="margin-top: 20px">刷新状态</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const healthData = ref({
  status: 'healthy',
  version: '2.4.0',
  memoryUsage: '2GB',
  cpuUsage: '10%',
  connections: 5
})

onMounted(() => {
  loadHealthData()
})

const loadHealthData = async () => {
  try {
    // 暂时使用模拟数据，后续可根据实际API返回的数据更新
    console.log('Loading health data...')
    // 模拟数据
    healthData.value = {
      status: 'healthy',
      version: '2.4.0',
      memoryUsage: '2GB',
      cpuUsage: '10%',
      connections: 5
    }
  } catch (error) {
    console.error('Failed to load health data:', error)
  }
}

const refreshHealth = () => {
  loadHealthData()
}
</script>

<style scoped>
.milvus-health {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.health-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}
</style>