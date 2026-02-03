<template>
  <div class="error-data">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>错误数据详情</span>
          <el-button type="primary" @click="handleRetrySelected">重推选中</el-button>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="错误类型">
            <el-select v-model="filterForm.errorType" placeholder="选择错误类型">
              <el-option label="向量维度不匹配" value="VECTOR_DIMENSION_MISMATCH"></el-option>
              <el-option label="Milvus写入超时" value="MILVUS_WRITE_TIMEOUT"></el-option>
              <el-option label="数据源连接失败" value="DATA_SOURCE_CONNECTION_FAILED"></el-option>
              <el-option label="数据格式错误" value="DATA_FORMAT_ERROR"></el-option>
              <el-option label="权限不足" value="PERMISSION_DENIED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="同步环节">
            <el-select v-model="filterForm.syncStage" placeholder="选择同步环节">
              <el-option label="数据源读取" value="DATA_SOURCE_READ"></el-option>
              <el-option label="数据处理" value="DATA_PROCESSING"></el-option>
              <el-option label="向量生成" value="VECTOR_GENERATION"></el-option>
              <el-option label="Milvus写入" value="MILVUS_WRITE"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">筛选</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 错误数据表格 -->
      <el-table 
        :data="errorDataList" 
        style="width: 100%"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="sourceData" label="源数据">
          <template #default="scope">
            <el-popover
              placement="top"
              :width="400"
              trigger="click"
            >
              <template #reference>
                <span class="source-data-preview">{{ truncateSourceData(scope.row.sourceData) }}</span>
              </template>
              <pre>{{ scope.row.sourceData }}</pre>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误原因">
          <template #default="scope">
            <el-tooltip :content="scope.row.errorMessage" placement="top">
              <span class="error-message">{{ truncateErrorMessage(scope.row.errorMessage) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="errorType" label="错误类型">
          <template #default="scope">
            <el-tag type="danger">{{ scope.row.errorType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="syncStage" label="同步环节">
          <template #default="scope">
            <el-tag :type="getStageType(scope.row.syncStage)">{{ scope.row.syncStage }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorTime" label="错误时间"></el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleRetrySingle(scope.row.id)">重推</el-button>
            <el-button size="small" @click="handleViewDetails(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
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
    
    <!-- 错误详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="错误详情"
      width="800px"
    >
      <div v-if="currentErrorData" class="error-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="错误ID">{{ currentErrorData.id }}</el-descriptions-item>
          <el-descriptions-item label="任务ID">{{ currentErrorData.taskId }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ currentErrorData.taskName }}</el-descriptions-item>
          <el-descriptions-item label="错误类型">{{ currentErrorData.errorType }}</el-descriptions-item>
          <el-descriptions-item label="同步环节">{{ currentErrorData.syncStage }}</el-descriptions-item>
          <el-descriptions-item label="错误时间">{{ currentErrorData.errorTime }}</el-descriptions-item>
          <el-descriptions-item label="错误原因">{{ currentErrorData.errorMessage }}</el-descriptions-item>
          <el-descriptions-item label="源数据">
            <pre>{{ currentErrorData.sourceData }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="处理建议">{{ getSuggestion(currentErrorData.errorType) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleRetrySingle(currentErrorData?.id)">重推</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const taskId = ref(Number(route.params.id))

// 筛选表单
const filterForm = ref({
  errorType: '',
  syncStage: ''
})

// 表格数据
const errorDataList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref<any[]>([])

// 对话框
const detailDialogVisible = ref(false)
const currentErrorData = ref<any>(null)

onMounted(() => {
  loadErrorData()
})

const loadErrorData = async () => {
  try {
    // 这里模拟数据，实际应调用API获取错误数据
    const mockData = [
      {
        id: 1,
        taskId: taskId.value,
        taskName: '测试任务',
        sourceData: '{"id": 1, "name": "测试数据", "content": "这是一条测试数据"}',
        errorMessage: '向量维度不匹配，期望128维，实际256维',
        errorType: 'VECTOR_DIMENSION_MISMATCH',
        syncStage: 'VECTOR_GENERATION',
        errorTime: '2026-02-02 10:00:00'
      },
      {
        id: 2,
        taskId: taskId.value,
        taskName: '测试任务',
        sourceData: '{"id": 2, "name": "测试数据2", "content": "这是另一条测试数据"}',
        errorMessage: 'Milvus写入超时，连接超时时间30秒',
        errorType: 'MILVUS_WRITE_TIMEOUT',
        syncStage: 'MILVUS_WRITE',
        errorTime: '2026-02-02 10:05:00'
      }
    ]
    errorDataList.value = mockData
    total.value = mockData.length
  } catch (error) {
    console.error('Failed to load error data:', error)
    ElMessage.error('加载错误数据失败')
  }
}

const handleFilter = () => {
  // 实际应根据筛选条件调用API
  console.log('Filter form:', filterForm.value)
  loadErrorData()
}

const resetFilter = () => {
  filterForm.value = {
    errorType: '',
    syncStage: ''
  }
  loadErrorData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadErrorData()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  loadErrorData()
}

const handleSelectionChange = (val: any[]) => {
  selectedRows.value = val
}

const handleRetrySelected = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要重推的数据')
    return
  }
  
  const ids = selectedRows.value.map(row => row.id)
  console.log('Retry selected ids:', ids)
  // 实际应调用API重推选中数据
  ElMessage.success(`已重推 ${selectedRows.value.length} 条数据`)
}

const handleRetrySingle = (id: number) => {
  console.log('Retry single id:', id)
  // 实际应调用API重推单条数据
  ElMessage.success('数据重推成功')
}

const handleViewDetails = (row: any) => {
  currentErrorData.value = row
  detailDialogVisible.value = true
}

const truncateSourceData = (data: string) => {
  return data.length > 50 ? data.substring(0, 50) + '...' : data
}

const truncateErrorMessage = (message: string) => {
  return message.length > 30 ? message.substring(0, 30) + '...' : message
}

const getStageType = (stage: string) => {
  switch (stage) {
    case 'DATA_SOURCE_READ':
      return 'info'
    case 'DATA_PROCESSING':
      return 'warning'
    case 'VECTOR_GENERATION':
      return 'primary'
    case 'MILVUS_WRITE':
      return 'success'
    default:
      return ''
  }
}

const getSuggestion = (errorType: string) => {
  switch (errorType) {
    case 'VECTOR_DIMENSION_MISMATCH':
      return '检查向量生成配置，确保维度与Milvus集合一致'
    case 'MILVUS_WRITE_TIMEOUT':
      return '增加Milvus写入超时时间，检查Milvus服务状态'
    case 'DATA_SOURCE_CONNECTION_FAILED':
      return '检查数据源连接配置，确保网络连通性'
    case 'DATA_FORMAT_ERROR':
      return '检查源数据格式，确保符合处理要求'
    case 'PERMISSION_DENIED':
      return '检查Milvus权限配置，确保有写入权限'
    default:
      return '请根据错误信息进行相应处理'
  }
}
</script>

<style scoped>
.error-data {
  padding: 20px;
  background: linear-gradient(135deg, #f0f4f8 0%, #e2e8f0 100%);
  min-height: 100vh;
  animation: fadeIn 0.8s ease-out;
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
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
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
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
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
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
  position: relative;
  overflow: hidden;
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
  box-shadow: 0 8px 30px rgba(67, 97, 238, 0.5);
}

/* 筛选区域样式 */
.filter-section {
  margin-bottom: 28px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(12px);
  animation: slideUp 0.8s ease-out 0.2s both;
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

:deep(.demo-form-inline) {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: end;
}

:deep(.el-form-item) {
  margin-bottom: 0;
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
}

:deep(.el-form-item:nth-child(1)) {
  --delay: 0.3s;
}

:deep(.el-form-item:nth-child(2)) {
  --delay: 0.4s;
}

:deep(.el-form-item:nth-child(3)) {
  --delay: 0.5s;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  padding: 0 12px 0 0;
}

:deep(.el-select) {
  width: 220px;
}

:deep(.el-select__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
}

:deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  border-color: #4361ee;
}

:deep(.el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.2);
  border-color: #4361ee;
}

:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  padding: 10px 20px;
  font-size: 14px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

:deep(.el-button--primary) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
}

:deep(.el-button--info) {
  background: linear-gradient(90deg, #3498db, #2980b9);
  border: none;
  color: #fff;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  margin-top: 24px;
  animation: slideUp 0.8s ease-out 0.4s both;
}

:deep(.el-table__header-wrapper) {
  background: linear-gradient(90deg, #f8fafc 0%, #e2e8f0 100%);
  border-bottom: 2px solid #4361ee;
}

:deep(.el-table__header th) {
  font-weight: 700;
  color: #1e293b;
  background: transparent;
  border-bottom: none;
  padding: 18px 16px;
  font-size: 14px;
  position: relative;
}

:deep(.el-table__header th::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 16px;
  right: 16px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #4361ee, transparent);
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

:deep(.el-table__row:hover) {
  background: rgba(67, 97, 238, 0.08) !important;
  transform: translateX(8px) scale(1.01);
  box-shadow: 0 4px 20px rgba(67, 97, 238, 0.2);
}

:deep(.el-table__cell) {
  padding: 18px 16px;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  color: #334155;
}

:deep(.el-table__cell:hover) {
  background: rgba(67, 97, 238, 0.05);
  transform: scale(1.02);
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
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
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

:deep(.el-tag--danger) {
  background: linear-gradient(90deg, #e74c3c, #c0392b);
  border: none;
}

:deep(.el-tag--info) {
  background: linear-gradient(90deg, #3498db, #2980b9);
  border: none;
}

:deep(.el-tag--warning) {
  background: linear-gradient(90deg, #f39c12, #e67e22);
  border: none;
}

:deep(.el-tag--primary) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
}

:deep(.el-tag--success) {
  background: linear-gradient(90deg, #2ecc71, #27ae60);
  border: none;
}

/* 错误信息样式 */
.error-message {
  color: #dc2626;
  background: rgba(220, 38, 38, 0.1);
  padding: 8px 16px;
  border-radius: 10px;
  border-left: 4px solid #dc2626;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.1);
  font-size: 13px;
  font-weight: 500;
}

.error-message:hover {
  box-shadow: 0 4px 16px rgba(220, 38, 38, 0.2);
  transform: translateX(4px);
  background: rgba(220, 38, 38, 0.15);
}

/* 源数据预览样式 */
.source-data-preview {
  cursor: pointer;
  color: #4361ee;
  background: rgba(67, 97, 238, 0.1);
  padding: 8px 16px;
  border-radius: 10px;
  border-left: 4px solid #4361ee;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(67, 97, 238, 0.1);
  font-size: 13px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.source-data-preview::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(67, 97, 238, 0.2), transparent);
  transition: left 0.5s ease;
}

.source-data-preview:hover::before {
  left: 100%;
}

.source-data-preview:hover {
  box-shadow: 0 4px 16px rgba(67, 97, 238, 0.2);
  transform: translateX(4px);
  color: #3a0ca3;
  background: rgba(67, 97, 238, 0.15);
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 32px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(12px);
  animation: slideUp 0.8s ease-out 0.6s both;
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
  border: 1px solid #e2e8f0;
}

:deep(.el-pagination__item:hover) {
  color: #4361ee;
  border-color: #4361ee;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.2);
}

:deep(.el-pagination__item.is-active) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border-color: transparent;
  color: #fff;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.4);
}

:deep(.el-pagination__prev),
:deep(.el-pagination__next) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
}

:deep(.el-pagination__prev:hover),
:deep(.el-pagination__next:hover) {
  border-color: #4361ee;
  color: #4361ee;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.2);
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: zoomIn 0.6s ease-out;
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
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
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
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

:deep(.el-dialog__footer) {
  padding: 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
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
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* 错误详情样式 */
.error-detail {
  max-height: 600px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(12px);
  animation: fadeInUp 0.6s ease-out;
}

.error-detail pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 250px;
  overflow-y: auto;
  background: #f8fafc;
  padding: 20px;
  border-radius: 10px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #334155;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 描述列表样式 */
:deep(.el-descriptions) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(12px);
  animation: fadeInUp 0.6s ease-out;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #1e293b;
  background: rgba(67, 97, 238, 0.1);
  border-right: 1px solid #e2e8f0;
  padding: 16px 20px;
}

:deep(.el-descriptions__cell) {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #334155;
}

:deep(.el-descriptions__cell:hover) {
  background: rgba(67, 97, 238, 0.05);
}

:deep(.el-descriptions__row:last-child :deep(.el-descriptions__cell)) {
  border-bottom: none;
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  border: none;
  background: #fff;
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
  background: #f1f5f9;
  border-radius: 5px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 5px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid #f1f5f9;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
  transform: scale(1.1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  :deep(.demo-form-inline) {
    gap: 16px;
  }
  
  :deep(.el-select) {
    width: 200px;
  }
  
  :deep(.el-table__cell) {
    padding: 14px 12px;
    font-size: 13px;
  }
  
  :deep(.el-table__header th) {
    padding: 14px 12px;
    font-size: 13px;
  }
  
  .error-message,
  .source-data-preview {
    padding: 6px 12px;
    font-size: 12px;
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
  .error-data {
    padding: 16px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  :deep(.demo-form-inline) {
    flex-direction: column;
    align-items: stretch;
  }
  
  :deep(.el-select) {
    width: 100%;
  }
  
  :deep(.el-table__cell) {
    padding: 12px 8px;
  }
  
  .filter-section {
    padding: 20px;
  }
  
  .pagination {
    padding: 20px;
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
  
  .error-message,
  .source-data-preview {
    padding: 4px 8px;
    font-size: 10px;
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
  
  :deep(.el-dialog) {
    width: 95% !important;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  .error-detail {
    padding: 16px;
  }
  
  .error-detail pre {
    padding: 12px;
    font-size: 12px;
  }
}
</style>