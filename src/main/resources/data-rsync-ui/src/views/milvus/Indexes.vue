<template>
  <div class="milvus-indexes">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>Milvus索引管理</span>
          <div class="header-actions">
            <el-select v-model="selectedCollection" placeholder="选择集合" style="width: 200px; margin-right: 16px">
              <el-option
                v-for="collection in collections"
                :key="collection.name"
                :label="collection.name"
                :value="collection.name"
              />
            </el-select>
            <el-button type="primary" @click="handleCreateIndex" :disabled="!selectedCollection">
              创建索引
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 索引列表 -->
      <div v-if="selectedCollection" class="index-list">
        <el-table :data="indexes" style="width: 100%" border>
          <el-table-column prop="fieldName" label="字段名称" />
          <el-table-column prop="indexName" label="索引名称" />
          <el-table-column prop="indexType" label="索引类型" />
          <el-table-column prop="metricType" label="度量类型" />
          <el-table-column prop="indexParams" label="索引参数">
            <template #default="scope">
              <el-popover
                placement="top"
                :width="300"
                trigger="click"
              >
                <template #reference>
                  <el-button size="small" type="text">查看</el-button>
                </template>
                <div class="index-params">
                  <pre>{{ JSON.stringify(scope.row.indexParams, null, 2) }}</pre>
                </div>
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="handleRebuildIndex(scope.row)">
                重建
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteIndex(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="indexes.length === 0" class="empty-state">
          <el-empty
            description="
              <span>当前集合没有索引</span>
            "
          />
        </div>
      </div>
      
      <div v-else class="empty-state">
        <el-empty
          description="
            <span>请选择一个集合查看索引</span>
          "
        />
      </div>
    </el-card>
    
    <!-- 创建索引对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建索引"
      width="600px"
    >
      <el-form :model="indexForm" label-width="100px">
        <el-form-item label="集合名称" prop="collectionName">
          <el-input v-model="indexForm.collectionName" readonly />
        </el-form-item>
        <el-form-item label="字段名称" prop="fieldName">
          <el-select v-model="indexForm.fieldName" placeholder="请选择字段">
            <el-option
              v-for="field in collectionFields"
              :key="field.name"
              :label="field.name"
              :value="field.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="索引类型" prop="indexType">
          <el-select v-model="indexForm.indexType" placeholder="请选择索引类型">
            <el-option label="FLAT" value="FLAT" />
            <el-option label="IVF_FLAT" value="IVF_FLAT" />
            <el-option label="IVF_SQ8" value="IVF_SQ8" />
            <el-option label="IVF_PQ" value="IVF_PQ" />
            <el-option label="HNSW" value="HNSW" />
            <el-option label="ANNOY" value="ANNOY" />
          </el-select>
        </el-form-item>
        <el-form-item label="度量类型" prop="metricType">
          <el-select v-model="indexForm.metricType" placeholder="请选择度量类型">
            <el-option label="L2" value="L2" />
            <el-option label="IP" value="IP" />
            <el-option label="COSINE" value="COSINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="索引参数" prop="indexParams">
          <el-input type="textarea" v-model="indexForm.indexParams" placeholder="请输入索引参数（JSON格式）" :rows="4" />
          <div class="form-tip">
            例如：{"nlist": 1024}（IVF_FLAT）或 {"M": 16, "efConstruction": 200}（HNSW）
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCreateIndex">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { milvusApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 集合列表
const collections = ref<any[]>([])
// 选中的集合
const selectedCollection = ref('')
// 索引列表
const indexes = ref<any[]>([])
// 集合字段
const collectionFields = ref<any[]>([])

// 创建索引对话框
const createDialogVisible = ref(false)
const indexForm = ref({
  collectionName: '',
  fieldName: '',
  indexType: '',
  metricType: '',
  indexParams: ''
})

// 加载集合列表
const loadCollections = async () => {
  try {
    const response = await milvusApi.getCollections()
    if (response.data.code === 200) {
      collections.value = response.data.data || []
    }
  } catch (error) {
    console.error('Failed to load collections:', error)
    ElMessage.error('加载集合列表失败')
  }
}

// 加载索引列表
const loadIndexes = async (collectionName: string) => {
  if (!collectionName) return
  
  try {
    const response = await milvusApi.getIndexes(collectionName)
    if (response.data.code === 200) {
      indexes.value = response.data.data || []
    }
  } catch (error) {
    console.error('Failed to load indexes:', error)
    ElMessage.error('加载索引列表失败')
  }
}

// 加载集合字段
const loadCollectionFields = async (collectionName: string) => {
  if (!collectionName) return
  
  try {
    // 这里应该调用获取集合字段的API
    // 暂时使用模拟数据
    collectionFields.value = [
      { name: 'vector', type: 'FLOAT_VECTOR' },
      { name: 'data', type: 'VARCHAR' },
      { name: 'metadata', type: 'JSON' },
      { name: 'id', type: 'INT64' }
    ]
  } catch (error) {
    console.error('Failed to load collection fields:', error)
    ElMessage.error('加载集合字段失败')
  }
}

// 监听选中集合变化
watch(selectedCollection, (newValue) => {
  if (newValue) {
    loadIndexes(newValue)
    loadCollectionFields(newValue)
  } else {
    indexes.value = []
    collectionFields.value = []
  }
})

// 状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'CREATED':
      return 'success'
    case 'CREATING':
      return 'warning'
    case 'FAILED':
      return 'danger'
    default:
      return ''
  }
}

// 创建索引
const handleCreateIndex = () => {
  indexForm.value = {
    collectionName: selectedCollection.value,
    fieldName: '',
    indexType: '',
    metricType: '',
    indexParams: ''
  }
  createDialogVisible.value = true
}

// 确认创建索引
const confirmCreateIndex = async () => {
  try {
    let indexParams
    try {
      indexParams = indexForm.value.indexParams ? JSON.parse(indexForm.value.indexParams) : {}
    } catch (error) {
      ElMessage.error('索引参数格式错误，请输入有效的JSON')
      return
    }
    
    const requestData = {
      fieldName: indexForm.value.fieldName,
      indexType: indexForm.value.indexType,
      metricType: indexForm.value.metricType,
      indexParams
    }
    
    const response = await milvusApi.createIndex(selectedCollection.value, requestData)
    if (response.data.code === 200) {
      ElMessage.success('索引创建成功')
      createDialogVisible.value = false
      loadIndexes(selectedCollection.value)
    }
  } catch (error) {
    console.error('Failed to create index:', error)
    ElMessage.error('创建索引失败')
  }
}

// 删除索引
const handleDeleteIndex = async (index: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该索引吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await milvusApi.deleteIndex(selectedCollection.value, index.indexName)
    if (response.data.code === 200) {
      ElMessage.success('索引删除成功')
      loadIndexes(selectedCollection.value)
    }
  } catch (error) {
    // 取消删除
  }
}

// 重建索引
const handleRebuildIndex = async (index: any) => {
  try {
    await ElMessageBox.confirm('确定要重建该索引吗？这可能需要一些时间。', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const response = await milvusApi.rebuildIndex(selectedCollection.value, index.indexName)
    if (response.data.code === 200) {
      ElMessage.success('索引重建成功')
      loadIndexes(selectedCollection.value)
    }
  } catch (error) {
    // 取消重建
  }
}

// 初始化
onMounted(() => {
  loadCollections()
})
</script>

<style scoped>
.milvus-indexes {
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

.header-actions {
  display: flex;
  align-items: center;
  animation: slideInRight 0.6s ease-out;
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

.header-actions :deep(.el-select) {
  margin-right: 16px;
}

.header-actions :deep(.el-button) {
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

.header-actions :deep(.el-button::before) {
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

.header-actions :deep(.el-button:active::before) {
  width: 300px;
  height: 300px;
}

.header-actions :deep(.el-button:hover) {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.5);
}

/* 表格样式 */
.index-list {
  margin-top: 24px;
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

:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
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

/* 空状态 */
.empty-state {
  padding: 60px 0;
  text-align: center;
  animation: fadeIn 0.8s ease-out;
}

/* 索引参数弹出框 */
.index-params {
  max-height: 300px;
  overflow-y: auto;
  background: rgba(15, 23, 42, 0.9);
  padding: 16px;
  border-radius: 8px;
  font-family: 'Courier New', Courier, monospace;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.index-params pre {
  margin: 0;
  font-size: 12px;
  line-height: 1.4;
  color: rgba(255, 255, 255, 0.8);
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

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 8px;
  line-height: 1.4;
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
  .header-actions {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .header-actions :deep(.el-select) {
    margin-right: 0;
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
  
  .header-actions :deep(.el-button) {
    padding: 10px 20px;
    font-size: 14px;
  }
}

@media (max-width: 992px) {
  .milvus-indexes {
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
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 0 16px;
  }
  
  .header-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions :deep(.el-select) {
    width: 100% !important;
    margin-bottom: 12px;
  }
  
  .header-actions :deep(.el-button) {
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
}
</style>