<template>
  <div class="data-source-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据源列表</span>
          <el-button type="primary" @click="handleCreate">创建数据源</el-button>
        </div>
      </template>
      <el-table :data="dataSources" style="width: 100%">
        <el-table-column prop="name" label="数据源名称" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="url" label="连接地址" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="enabled" label="启用状态">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" @change="handleEnableChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="healthStatus" label="健康状态">
          <template #default="scope">
            <el-tag :type="scope.row.healthStatus === 'HEALTHY' ? 'success' : 'danger'">
              {{ scope.row.healthStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            <el-button size="small" @click="handleTestConnection(scope.row.id)">测试连接</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination" style="margin-top: 20px">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dataSourceApi } from '@/api'

const router = useRouter()
const dataSources = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadDataSourceList()
})

const loadDataSourceList = async () => {
  try {
    const response = await dataSourceApi.getList()
    dataSources.value = response.data || []
    total.value = dataSources.value.length
  } catch (error) {
    console.error('Failed to load data sources:', error)
  }
}

const handleCreate = () => {
  router.push('/data-source/create')
}

const handleEdit = (row: any) => {
  // 这里可以跳转到编辑页面，带上id参数
  console.log('Edit data source:', row)
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
    console.log('Test connection result:', result)
    // 这里可以显示测试结果
  } catch (error) {
    console.error('Failed to test connection:', error)
  }
}

const handleEnableChange = async (row: any) => {
  try {
    // 这里可以调用启用/禁用接口
    console.log('Enable change:', row)
  } catch (error) {
    console.error('Failed to change enable status:', error)
  }
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadDataSourceList()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  loadDataSourceList()
}
</script>

<style scoped>
.data-source-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}
</style>