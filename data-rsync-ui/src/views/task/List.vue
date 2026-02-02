<template>
  <div class="task-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
          <el-button type="primary" @click="handleCreate">创建任务</el-button>
        </div>
      </template>
      <el-table :data="tasks" style="width: 100%">
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="dataSourceType" label="数据源类型" />
        <el-table-column prop="databaseName" label="数据库" />
        <el-table-column prop="tableName" label="表名" />
        <el-table-column prop="type" label="任务类型">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'FULL' ? 'primary' : 'success'">
              {{ scope.row.type === 'FULL' ? '全量同步' : '增量同步' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            <el-button size="small" type="primary" @click="handleStart(scope.row.id)">启动</el-button>
            <el-button size="small" @click="handlePause(scope.row.id)">暂停</el-button>
            <el-button size="small" @click="handleResume(scope.row.id)">继续</el-button>
            <el-button size="small" @click="handleRollback(scope.row.id)">回滚</el-button>
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
import { taskApi } from '@/api'

const router = useRouter()
const tasks = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => {
  loadTaskList()
})

const loadTaskList = async () => {
  try {
    const response = await taskApi.getList()
    tasks.value = response.data || []
    total.value = tasks.value.length
  } catch (error) {
    console.error('Failed to load tasks:', error)
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'RUNNING':
      return 'success'
    case 'PAUSED':
      return 'warning'
    case 'FAILED':
      return 'danger'
    case 'COMPLETED':
      return 'info'
    default:
      return ''
  }
}

const handleCreate = () => {
  router.push('/task/create')
}

const handleEdit = (row: any) => {
  // 这里可以跳转到编辑页面，带上id参数
  console.log('Edit task:', row)
}

const handleDelete = async (id: number) => {
  try {
    await taskApi.delete(id)
    await loadTaskList()
  } catch (error) {
    console.error('Failed to delete task:', error)
  }
}

const handleStart = async (id: number) => {
  try {
    const result = await taskApi.start(id)
    console.log('Start task result:', result)
    await loadTaskList()
  } catch (error) {
    console.error('Failed to start task:', error)
  }
}

const handlePause = async (id: number) => {
  try {
    const result = await taskApi.pause(id)
    console.log('Pause task result:', result)
    await loadTaskList()
  } catch (error) {
    console.error('Failed to pause task:', error)
  }
}

const handleResume = async (id: number) => {
  try {
    const result = await taskApi.resume(id)
    console.log('Resume task result:', result)
    await loadTaskList()
  } catch (error) {
    console.error('Failed to resume task:', error)
  }
}

const handleRollback = async (id: number) => {
  try {
    // 这里可以弹出一个对话框让用户输入回滚点
    const rollbackPoint = '2026-02-01'
    const result = await taskApi.rollback(id, rollbackPoint)
    console.log('Rollback task result:', result)
    await loadTaskList()
  } catch (error) {
    console.error('Failed to rollback task:', error)
  }
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadTaskList()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  loadTaskList()
}
</script>

<style scoped>
.task-list {
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