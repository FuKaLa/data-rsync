<template>
  <div class="vectorization-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>向量化配置管理</span>
          <el-button type="primary" @click="handleAddConfig">添加配置</el-button>
        </div>
      </template>
      
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-input
          v-model="searchQuery"
          placeholder="搜索配置名称或算法"
          prefix-icon="el-icon-search"
          style="width: 300px; margin-right: 16px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filterAlgorithm" placeholder="按算法筛选" style="width: 150px; margin-right: 16px">
          <el-option label="全部" value="" />
          <el-option label="BERT" value="BERT" />
          <el-option label="FastText" value="FASTTEXT" />
          <el-option label="OpenAI" value="OPENAI" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </div>
      
      <!-- 配置列表 -->
      <el-table :data="filteredConfigs" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="配置名称" />
        <el-table-column prop="algorithm" label="算法" />
        <el-table-column prop="dimension" label="向量维度" width="120" />
        <el-table-column prop="modelName" label="模型名称" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEditConfig(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteConfig(scope.row.id)">删除</el-button>
            <el-button size="small" @click="handlePreview(scope.row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
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
    
    <!-- 配置编辑对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      :title="configDialogTitle"
      width="600px"
    >
      <el-form :model="configForm" label-width="100px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="configForm.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="算法" prop="algorithm">
          <el-select v-model="configForm.algorithm" placeholder="请选择算法">
            <el-option label="BERT" value="BERT" />
            <el-option label="FastText" value="FASTTEXT" />
            <el-option label="OpenAI" value="OPENAI" />
            <el-option label="Sentence-BERT" value="SENTENCE_BERT" />
          </el-select>
        </el-form-item>
        <el-form-item label="向量维度" prop="dimension">
          <el-input-number v-model="configForm.dimension" :min="1" :max="10000" style="width: 100%" placeholder="请输入向量维度" />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="configForm.modelName" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型参数">
          <el-input type="textarea" v-model="configForm.modelParams" placeholder="请输入模型参数（JSON格式）" :rows="4" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="configForm.description" placeholder="请输入配置描述" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="configDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmConfig">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="向量化预览"
      width="800px"
    >
      <div v-if="previewData" class="preview-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="配置名称">{{ previewData.name }}</el-descriptions-item>
          <el-descriptions-item label="算法">{{ previewData.algorithm }}</el-descriptions-item>
          <el-descriptions-item label="向量维度">{{ previewData.dimension }}</el-descriptions-item>
          <el-descriptions-item label="模型名称">{{ previewData.modelName }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ previewData.processTime }}ms</el-descriptions-item>
          <el-descriptions-item label="源数据">{{ previewData.sourceData }}</el-descriptions-item>
          <el-descriptions-item label="向量结果">
            <div class="vector-result">
              <pre>{{ JSON.stringify(previewData.vectorData, null, 2) }}</pre>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>生成预览中...</span>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { taskApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

// 配置列表
const configs = ref<any[]>([])
// 搜索和筛选
const searchQuery = ref('')
const filterAlgorithm = ref('')
// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框
const configDialogVisible = ref(false)
const configDialogTitle = ref('添加向量化配置')
const configForm = ref({
  id: 0,
  name: '',
  algorithm: '',
  dimension: 0,
  modelName: '',
  modelParams: '',
  description: ''
})

// 预览
const previewVisible = ref(false)
const previewData = ref<any>(null)

// 计算筛选后的配置列表
const filteredConfigs = computed(() => {
  let result = [...configs.value]
  
  // 搜索
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(config => 
      config.name?.toLowerCase().includes(query) || 
      config.algorithm?.toLowerCase().includes(query)
    )
  }
  
  // 算法筛选
  if (filterAlgorithm.value) {
    result = result.filter(config => config.algorithm === filterAlgorithm.value)
  }
  
  // 分页
  total.value = result.length
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

// 加载配置列表
const loadConfigs = async () => {
  try {
    // 调用实际的API获取配置列表
    const response = await taskApi.getVectorizationConfigByTaskId(0) // 使用0获取所有配置
    if (response.data.code === 200) {
      configs.value = response.data.data
    } else {
      configs.value = []
    }
  } catch (error) {
    console.error('Failed to load vectorization configs:', error)
    ElMessage.error('加载向量化配置失败')
    configs.value = []
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  // 这里可以添加实际的搜索逻辑
}

// 重置筛选
const resetFilter = () => {
  searchQuery.value = ''
  filterAlgorithm.value = ''
  currentPage.value = 1
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
}

// 状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'warning'
    case 'ERROR':
      return 'danger'
    default:
      return ''
  }
}

// 添加配置
const handleAddConfig = () => {
  configForm.value = {
    id: 0,
    name: '',
    algorithm: '',
    dimension: 0,
    modelName: '',
    modelParams: '',
    description: ''
  }
  configDialogTitle.value = '添加向量化配置'
  configDialogVisible.value = true
}

// 编辑配置
const handleEditConfig = (row: any) => {
  configForm.value = { ...row }
  configDialogTitle.value = '编辑向量化配置'
  configDialogVisible.value = true
}

// 删除配置
const handleDeleteConfig = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该向量化配置吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用实际的API删除配置
    const response = await taskApi.deleteVectorizationConfig(id)
    if (response.data.code === 200) {
      ElMessage.success('向量化配置删除成功')
      // 重新加载配置列表
      loadConfigs()
    } else {
      ElMessage.error('删除向量化配置失败: ' + response.data.message)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete vectorization config:', error)
      ElMessage.error('删除向量化配置失败: ' + (error.message || '未知错误'))
    }
  }
}

// 预览配置
const handlePreview = async (config: any) => {
  previewVisible.value = true
  previewData.value = null
  
  try {
    // 调用实际的API生成预览
    const response = await taskApi.generateVectorizationPreview({
      configId: config.id,
      testData: '这是一条测试数据，用于生成向量'
    })
    if (response.data.code === 200) {
      previewData.value = {
        ...config,
        ...response.data.data
      }
    } else {
      ElMessage.error('生成预览失败: ' + response.data.message)
      previewVisible.value = false
    }
  } catch (error: any) {
    console.error('Failed to generate vectorization preview:', error)
    ElMessage.error('生成预览失败: ' + (error.message || '未知错误'))
    previewVisible.value = false
  }
}

// 确认保存配置
const confirmConfig = async () => {
  try {
    // 调用实际的API保存配置
    const response = await taskApi.saveVectorizationConfig(configForm.value)
    if (response.data.code === 200) {
      ElMessage.success(configForm.value.id === 0 ? '向量化配置添加成功' : '向量化配置更新成功')
      configDialogVisible.value = false
      // 重新加载配置列表
      loadConfigs()
    } else {
      ElMessage.error('保存向量化配置失败: ' + response.data.message)
    }
  } catch (error: any) {
    console.error('Failed to save vectorization config:', error)
    ElMessage.error('保存向量化配置失败: ' + (error.message || '未知错误'))
  }
}

// 初始化
onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.vectorization-list {
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

/* 搜索筛选区域 */
.search-filter {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.8s ease-out 0.4s both;
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

/* 分页样式 */
.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
  animation: fadeIn 0.8s ease-out 0.6s both;
}

:deep(.el-pagination) {
  background: rgba(15, 23, 42, 0.8);
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-pagination__item) {
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-pagination__item:hover) {
  border-color: #3b82f6;
  color: #ffffff;
  transform: scale(1.1);
}

:deep(.el-pagination__item.is-current) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border-color: #3b82f6;
  color: #ffffff;
}

:deep(.el-pagination__button) {
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-pagination__button:hover) {
  border-color: #3b82f6;
  color: #ffffff;
  transform: scale(1.1);
}

/* 表单样式 */
:deep(.el-form) {
  padding: 24px;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(12px);
  animation: fadeInUp 0.6s ease-out;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.3);
  border-color: #3b82f6;
}

:deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
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

:deep(.el-select-dropdown__item) {
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  margin: 4px 8px;
}

:deep(.el-select-dropdown__item:hover) {
  background: rgba(59, 130, 246, 0.1);
  transform: translateX(8px);
}

:deep(.el-select-dropdown__item.selected) {
  background: rgba(59, 130, 246, 0.2);
  color: #ffffff;
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

/* 预览内容样式 */
.preview-content {
  max-height: 600px;
  overflow-y: auto;
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  padding: 24px;
  box-shadow: inset 0 4px 20px rgba(0, 0, 0, 0.3);
  animation: fadeIn 0.8s ease-out;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.vector-result {
  max-height: 350px;
  overflow-y: auto;
  background: rgba(15, 23, 42, 0.8);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(12px);
  font-family: 'Courier New', Courier, monospace;
  border-left: 4px solid #3b82f6;
  animation: slideInRight 0.6s ease-out;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.vector-result pre {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.8);
  animation: fadeIn 1s ease-out 0.3s both;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  animation: fadeIn 0.8s ease-out;
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
  .search-filter {
    flex-wrap: wrap;
    gap: 16px;
  }
  
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
  .vectorization-list {
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
  
  .search-filter {
    padding: 16px;
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
  
  :deep(.el-dialog) {
    width: 90% !important;
  }
  
  :deep(.el-dialog__body) {
    padding: 24px;
  }
  
  .search-filter {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-filter :deep(.el-input),
  .search-filter :deep(.el-select) {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 12px;
  }
  
  .search-filter :deep(.el-button) {
    margin-right: 8px;
  }
}
</style>