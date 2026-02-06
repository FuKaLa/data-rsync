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
  min-height: 100vh;
  animation: fadeIn 0.8s ease-out;
  position: relative;
}

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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  animation: slideInLeft 0.6s ease-out;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.card-header span {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  padding: 12px 0;
}

.card-header span::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border-radius: 2px;
  animation: expandWidth 1s ease-out 0.3s forwards;
  transform: scaleX(0);
  transform-origin: left;
}

@keyframes expandWidth {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

.card-header :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border: none;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  position: relative;
  overflow: hidden;
  color: #ffffff;
}

.card-header :deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.card-header :deep(.el-button:active::before) {
  width: 300px;
  height: 300px;
}

.card-header :deep(.el-button:hover) {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.5);
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  margin-top: 24px;
  animation: slideUp 0.8s ease-out 0.2s both;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
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

:deep(.el-table__header-wrapper) {
  background: rgba(15, 23, 42, 0.9) !important;
  border-bottom: 2px solid rgba(59, 130, 246, 0.5);
  z-index: 10;
}

:deep(.el-table__header th) {
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9) !important;
  background: transparent !important;
  border-bottom: none;
  padding: 18px 16px;
  font-size: 14px;
  position: relative;
  z-index: 11;
}

:deep(.el-table__header th::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 16px;
  right: 16px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #3b82f6, transparent);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  transform-origin: center;
}

:deep(.el-table__header th:hover::after) {
  transform: scaleX(1);
}

:deep(.el-table__row) {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.6s ease forwards;
  opacity: 0;
  position: relative;
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
}

:deep(.el-table__row:nth-child(1)) {
  animation-delay: 0.1s;
}

:deep(.el-table__row:nth-child(2)) {
  animation-delay: 0.2s;
}

:deep(.el-table__row:nth-child(3)) {
  animation-delay: 0.3s;
}

:deep(.el-table__row:nth-child(4)) {
  animation-delay: 0.4s;
}

:deep(.el-table__row:nth-child(5)) {
  animation-delay: 0.5s;
}

:deep(.el-table__row:nth-child(6)) {
  animation-delay: 0.6s;
}

:deep(.el-table__row:nth-child(7)) {
  animation-delay: 0.7s;
}

:deep(.el-table__row:nth-child(8)) {
  animation-delay: 0.8s;
}

:deep(.el-table__row:nth-child(9)) {
  animation-delay: 0.9s;
}

:deep(.el-table__row:nth-child(10)) {
  animation-delay: 1s;
}

:deep(.el-table__row:hover) {
  background: rgba(59, 130, 246, 0.1) !important;
  transform: translateX(8px) scale(1.01);
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.2);
  border-left: 3px solid #3b82f6;
}

:deep(.el-table__row.el-table__row--striped) {
  background: rgba(59, 130, 246, 0.05);
}

:deep(.el-table__cell) {
  padding: 18px 16px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-table__cell:hover) {
  background: rgba(59, 130, 246, 0.05);
  transform: scale(1.02);
}

/* 表格空状态样式 */
:deep(.el-table__empty-block) {
  background: rgba(15, 23, 42, 0.9) !important;
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-table__empty-text) {
  color: rgba(255, 255, 255, 0.7) !important;
}

/* 表格其他部分样式 */
:deep(.el-table__body-wrapper) {
  background: rgba(15, 23, 42, 0.8) !important;
}

:deep(.el-table__row) {
  background: rgba(15, 23, 42, 0.8) !important;
}

:deep(.el-table__row:hover) {
  background: rgba(59, 130, 246, 0.1) !important;
}

:deep(.el-table__row.el-table__row--striped) {
  background: rgba(59, 130, 246, 0.05) !important;
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
  border: none;
}

:deep(.el-tag::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

:deep(.el-tag:hover::before) {
  left: 100%;
}

:deep(.el-tag:hover) {
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

:deep(.el-tag--primary) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
}

:deep(.el-tag--success) {
  background: linear-gradient(90deg, #10b981, #059669);
}

:deep(.el-tag--warning) {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

:deep(.el-tag--danger) {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

:deep(.el-tag--info) {
  background: linear-gradient(90deg, #06b6d4, #0891b2);
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  margin-right: 8px;
  padding: 8px 16px;
  font-size: 12px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

:deep(.el-button:active::before) {
  width: 200px;
  height: 200px;
}

:deep(.el-button:hover) {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  color: #ffffff;
}

:deep(.el-button--primary) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border: none;
  color: #ffffff;
}

:deep(.el-button--danger) {
  background: linear-gradient(90deg, #ef4444, #dc2626);
  border: none;
  color: #ffffff;
}

:deep(.el-button--success) {
  background: linear-gradient(90deg, #10b981, #059669);
  border: none;
  color: #ffffff;
}

:deep(.el-button:disabled) {
  opacity: 0.5;
  transform: none;
  box-shadow: none;
  background: rgba(15, 23, 42, 0.6) !important;
  color: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
}

:deep(.el-button:disabled:hover) {
  transform: none;
  box-shadow: none;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 32px;
  padding: 24px;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(12px);
  animation: slideUp 0.8s ease-out 0.4s both;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-pagination__item) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 6px 12px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination__item:hover) {
  color: #3b82f6;
  border-color: #3b82f6;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  background: rgba(59, 130, 246, 0.1);
}

:deep(.el-pagination__item.is-active) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border-color: transparent;
  color: #ffffff;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

:deep(.el-pagination__prev),
:deep(.el-pagination__next) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination__prev:hover),
:deep(.el-pagination__next:hover) {
  border-color: #3b82f6;
  color: #3b82f6;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  background: rgba(59, 130, 246, 0.1);
}

:deep(.el-pagination__sizes) {
  margin-right: 16px;
}

:deep(.el-pagination__sizes .el-select .el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination__sizes .el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

:deep(.el-pagination__sizes .el-select .el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
  animation: zoomIn 0.6s ease-out;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(59, 130, 246, 0.3);
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

:deep(.el-dialog__header) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  color: #fff;
  padding: 24px;
}

:deep(.el-dialog__title) {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

:deep(.el-dialog__close) {
  color: #fff;
  font-size: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-dialog__close:hover) {
  transform: scale(1.2) rotate(90deg);
  color: #f1f5f9;
}

:deep(.el-dialog__body) {
  padding: 32px;
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-dialog__footer) {
  padding: 24px;
  background: rgba(15, 23, 42, 0.9);
  border-top: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-dialog__footer :deep(.el-button)) {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  margin-right: 12px;
}

:deep(.el-dialog__footer :deep(.el-button:hover)) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
}

/* 回滚对话框样式 */
.rollback-dialog {
  padding: 24px;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(12px);
  animation: fadeInUp 0.6s ease-out;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.rollback-dialog p {
  margin-bottom: 24px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  position: relative;
  padding-left: 16px;
}

.rollback-dialog p::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #3b82f6, #a855f7);
  border-radius: 50%;
  transform: translateY(-50%);
}

:deep(.el-select) {
  width: 100%;
  margin-bottom: 24px;
}

:deep(.el-select__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

:deep(.el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.3);
  border-color: #3b82f6;
}

:deep(.el-select__input) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select-dropdown) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.3);
  animation: fadeInScale 0.3s ease-out;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.version-option {
  padding: 16px;
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  margin: 4px 8px;
  border: 1px solid transparent;
  color: rgba(255, 255, 255, 0.8);
}

.version-option:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
  transform: translateX(8px);
}

.version-info {
  margin-bottom: 12px;
}

.version-info strong {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

.version-meta {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.version-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tip {
  margin-top: 24px !important;
  padding: 16px;
  background: rgba(245, 158, 11, 0.1);
  border-radius: 10px;
  border-left: 4px solid #f59e0b;
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: 14px !important;
  line-height: 1.6;
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.2);
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  animation: fadeInUp 0.8s ease-out;
}

:deep(.el-card__body) {
  padding: 32px;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 5px;
}

::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.5);
  border-radius: 5px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(15, 23, 42, 0.6);
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.8);
  transform: scale(1.1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  :deep(.el-button) {
    margin-right: 6px;
    padding: 6px 12px;
    font-size: 11px;
  }
  
  :deep(.el-table__cell) {
    padding: 14px 12px;
    font-size: 13px;
  }
  
  :deep(.el-table__header th) {
    padding: 14px 12px;
    font-size: 13px;
  }
  
  .card-header {
    padding: 0 20px;
  }
  
  .card-header span {
    font-size: 18px;
  }
  
  .card-header :deep(.el-button) {
    padding: 10px 20px;
    font-size: 14px;
  }
}

@media (max-width: 992px) {
  .task-list {
    padding: 16px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  :deep(.el-table__cell) {
    padding: 12px 8px;
  }
  
  :deep(.el-button) {
    margin-right: 4px;
    padding: 4px 8px;
    font-size: 10px;
  }
  
  .pagination {
    padding: 20px;
  }
  
  :deep(.el-pagination__item) {
    padding: 4px 8px;
    font-size: 12px;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 0 16px;
  }
  
  .card-header :deep(.el-button) {
    align-self: flex-start;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table__cell) {
    padding: 8px 4px;
  }
  
  :deep(.el-button) {
    margin-right: 2px;
    padding: 2px 6px;
    font-size: 9px;
  }
  
  .pagination {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>