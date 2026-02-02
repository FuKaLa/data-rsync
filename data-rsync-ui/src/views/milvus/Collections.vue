<template>
  <div class="milvus-collections">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>集合管理</span>
        </div>
      </template>
      <el-table :data="collections" style="width: 100%">
        <el-table-column prop="name" label="集合名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="count" label="数据量" />
        <el-table-column prop="createdTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="handleOptimize(scope.row.name)">优化</el-button>
            <el-button size="small" @click="handleViewIndexes(scope.row.name)">查看索引</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { milvusApi } from '@/api'

const collections = ref<any[]>([])

onMounted(() => {
  loadCollections()
})

const loadCollections = async () => {
  try {
    const response = await milvusApi.getCollections()
    collections.value = response.data || []
  } catch (error) {
    console.error('Failed to load collections:', error)
  }
}

const handleOptimize = async (collectionName: string) => {
  try {
    const result = await milvusApi.optimizeCollection(collectionName)
    console.log('Optimize result:', result)
    // 这里可以显示优化结果
  } catch (error) {
    console.error('Failed to optimize collection:', error)
  }
}

const handleViewIndexes = (collectionName: string) => {
  // 这里可以跳转到索引管理页面，带上集合名称参数
  console.log('View indexes for collection:', collectionName)
}
</script>

<style scoped>
.milvus-collections {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>