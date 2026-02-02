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
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

.error-message {
  color: #f56c6c;
}

.source-data-preview {
  cursor: pointer;
  color: #409eff;
}

.error-detail pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 200px;
  overflow-y: auto;
}
</style>