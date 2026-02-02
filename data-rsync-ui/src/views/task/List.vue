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
            <el-button size="small" @click="handlePause(scope.row.id)" :disabled="scope.row.status !== 'RUNNING'">暂停</el-button>
            <el-button size="small" @click="handleResume(scope.row.id)" :disabled="scope.row.status !== 'PAUSED'">继续</el-button>
            <el-button size="small" @click="handleRollback(scope.row.id)">回滚</el-button>
            <el-button size="small" @click="handleErrorData(scope.row.id)" v-if="scope.row.status === 'FAILED'">错误数据</el-button>
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

    <!-- 回滚对话框 -->
    <el-dialog
      v-model="rollbackDialogVisible"
      title="任务回滚"
      width="500px"
    >
      <div class="rollback-dialog">
        <p>请选择回滚点：</p>
        <el-select v-model="rollbackPoint" placeholder="选择回滚版本">
          <el-option
            v-for="version in taskVersions"
            :key="version.version"
            :label="`版本 ${version.version} (${version.timestamp}) - ${version.description}`"
            :value="version.version"
          >
            <div class="version-option">
              <div class="version-info">
                <strong>{{ version.description }}</strong>
              </div>
              <div class="version-meta">
                <span>操作人：{{ version.operator }}</span>
                <span>时间：{{ version.timestamp }}</span>
              </div>
            </div>
          </el-option>
        </el-select>
        <div class="tip" style="margin-top: 10px; color: #999; font-size: 12px;">
          回滚后，任务将恢复到所选版本的状态，请注意数据安全。
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rollbackDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRollback">确认回滚</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const tasks = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rollbackDialogVisible = ref(false)
const currentTaskId = ref(0)
const rollbackPoint = ref('')
const taskVersions = ref<any[]>([])

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
    ElMessage.error('加载任务列表失败')
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
    case 'ROLLED_BACK':
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
    ElMessage.success('任务删除成功')
  } catch (error) {
    console.error('Failed to delete task:', error)
    ElMessage.error('删除任务失败')
  }
}

const handleStart = async (id: number) => {
  try {
    const result = await taskApi.start(id)
    console.log('Start task result:', result)
    await loadTaskList()
    ElMessage.success('任务启动成功')
  } catch (error) {
    console.error('Failed to start task:', error)
    ElMessage.error('启动任务失败')
  }
}

const handlePause = async (id: number) => {
  try {
    const result = await taskApi.pause(id)
    console.log('Pause task result:', result)
    await loadTaskList()
    ElMessage.success('任务暂停成功')
  } catch (error) {
    console.error('Failed to pause task:', error)
    ElMessage.error('暂停任务失败')
  }
}

const handleResume = async (id: number) => {
  try {
    const result = await taskApi.resume(id)
    console.log('Resume task result:', result)
    await loadTaskList()
    ElMessage.success('任务继续成功')
  } catch (error) {
    console.error('Failed to resume task:', error)
    ElMessage.error('继续任务失败')
  }
}

const handleRollback = async (id: number) => {
  currentTaskId.value = id
  // 加载任务版本
  try {
    const versions = await taskApi.getVersions(id)
    taskVersions.value = versions.data || []
    rollbackDialogVisible.value = true
  } catch (error) {
    console.error('Failed to get task versions:', error)
    ElMessage.error('加载任务版本失败')
  }
}

const confirmRollback = async () => {
  if (!rollbackPoint.value) {
    ElMessage.warning('请选择回滚点')
    return
  }
  
  try {
    const result = await taskApi.rollback(currentTaskId.value, rollbackPoint.value)
    console.log('Rollback task result:', result)
    rollbackDialogVisible.value = false
    await loadTaskList()
    ElMessage.success('任务回滚成功')
  } catch (error) {
    console.error('Failed to rollback task:', error)
    ElMessage.error('回滚任务失败')
  }
}

const handleErrorData = (id: number) => {
  // 跳转到错误数据详情页
  router.push(`/task/error-data/${id}`)
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