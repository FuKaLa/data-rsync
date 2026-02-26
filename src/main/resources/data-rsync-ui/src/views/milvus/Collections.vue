<template>
  <div class="milvus-collections">
    <el-card :body-style="{ position: 'relative' }" class="tech-card">
      <template #header>
        <div class="card-header">
          <h2 class="card-title">集合管理</h2>
          <el-button 
            type="primary" 
            @click="dialogVisible = true"
            :loading="loading"
            class="tech-button"
          >
            <el-icon><Plus /></el-icon>
            新建集合
          </el-button>
        </div>
      </template>
      <div v-if="loading" class="table-loading-overlay">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <el-table 
        v-loading="loading"
        :data="collections" 
        style="width: 100%"
        class="tech-table"
        :empty-text="loading ? '加载中...' : '暂无集合数据'"
      >
        <el-table-column prop="collectionName" label="集合名称" />
        <el-table-column prop="dimension" label="向量维度" />
        <el-table-column prop="metricType" label="距离度量" />
        <el-table-column prop="rowCount" label="数据量" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button 
              size="small" 
              @click="handleOptimize(scope.row.collectionName)"
              :loading="loading"
              class="tech-button-secondary"
            >
              优化
            </el-button>
            <el-button 
              size="small" 
              @click="handleViewIndexes(scope.row.collectionName)"
              :loading="loading"
              class="tech-button-secondary"
            >
              查看索引
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row.collectionName)"
              :loading="loading"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建集合对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="新建向量集合"
      width="400px"
      class="tech-dialog"
    >
      <el-form 
        :model="collectionForm" 
        label-width="80px"
        class="tech-form"
      >
        <el-form-item 
          label="集合名称"
          :rules="[{ required: true, message: '请输入集合名称', trigger: 'blur' }]"
        >
          <el-input 
            v-model="collectionForm.collectionName" 
            placeholder="请输入集合名称" 
            class="tech-input"
          />
        </el-form-item>
        <el-form-item 
          label="向量维度"
          :rules="[{ required: true, message: '请输入向量维度', trigger: 'blur' }]"
        >
          <el-input-number 
            v-model="collectionForm.dimension" 
            :min="1" 
            :max="4096" 
            placeholder="请输入向量维度" 
            class="tech-input"
          />
        </el-form-item>
        <el-form-item 
          label="距离度量"
          :rules="[{ required: true, message: '请选择距离度量', trigger: 'change' }]"
        >
          <el-select 
            v-model="collectionForm.metricType" 
            placeholder="请选择距离度量"
            class="tech-select"
          >
            <el-option label="L2" value="L2" />
            <el-option label="IP" value="IP" />
            <el-option label="COSINE" value="COSINE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleCreateCollection"
            :loading="loading"
          >
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { milvusApi } from '@/api'
import { useRouter } from 'vue-router'

const router = useRouter()
const collections = ref<any[]>([])
const dialogVisible = ref(false)
const loading = ref(false)
const collectionForm = ref({
  collectionName: '',
  dimension: 128,
  metricType: 'L2'
})

onMounted(() => {
  loadCollections()
})

const loadCollections = async () => {
  loading.value = true
  try {
    console.log('Loading collections...')
    // 使用统一的API配置获取集合列表
    const response = await milvusApi.getCollections()
    if (response.data.code === 200) {
      collections.value = response.data.data
    } else {
      ElMessage.error('获取集合列表失败: ' + response.data.message)
    }
  } catch (error: any) {
    console.error('Failed to load collections:', error)
    ElMessage.error('获取集合列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const handleCreateCollection = async () => {
  loading.value = true
  try {
    console.log('Creating collection:', collectionForm.value)
    
    // 使用统一的API配置创建集合
    const response = await milvusApi.createCollection(collectionForm.value)
    if (response.data.code === 200) {
      ElMessage.success('集合创建成功')
      dialogVisible.value = false
      // 重置表单
      collectionForm.value = {
        collectionName: '',
        dimension: 128,
        metricType: 'L2'
      }
      // 重新加载集合列表
      loadCollections()
    } else {
      ElMessage.error('创建集合失败: ' + response.data.message)
    }
  } catch (error: any) {
    console.error('Failed to create collection:', error)
    ElMessage.error('创建集合失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const handleDelete = async (collectionName: string) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除集合 ${collectionName} 吗？此操作不可恢复。`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    console.log('Deleting collection:', collectionName)
    
    // 使用统一的API配置删除集合
    const response = await milvusApi.deleteCollection(collectionName)
    if (response.data.code === 200) {
      ElMessage.success('集合删除成功')
      // 重新加载集合列表
      loadCollections()
    } else {
      ElMessage.error('删除集合失败: ' + response.data.message)
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete collection:', error)
      ElMessage.error('删除集合失败: ' + (error.message || '网络错误'))
    }
  } finally {
    loading.value = false
  }
}

const handleOptimize = async (collectionName: string) => {
  loading.value = true
  try {
    console.log('Optimizing collection:', collectionName)
    
    // 使用统一的API配置优化集合
    const response = await milvusApi.optimizeCollection(collectionName)
    if (response.data.code === 200) {
      ElMessage.success('集合优化成功')
    } else {
      ElMessage.error('优化集合失败: ' + response.data.message)
    }
  } catch (error: any) {
    console.error('Failed to optimize collection:', error)
    ElMessage.error('优化集合失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

const handleViewIndexes = (collectionName: string) => {
  // 跳转到索引管理页面，带上集合名称参数
  console.log('View indexes for collection:', collectionName)
  router.push(`/milvus/indexes?collection=${collectionName}`)
}
</script>

<style scoped>
.milvus-collections {
  padding: 20px;
  min-height: 100%;
  position: relative;
}

/* 科技感卡片样式 */
.tech-card {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  transition: all 0.3s ease;
}

.tech-card:hover {
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.4);
  transform: translateY(-2px);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 科技感按钮 */
.tech-button {
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.tech-button:hover {
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px);
}

.tech-button-secondary {
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: #3b82f6;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.tech-button-secondary:hover {
  background: rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

/* 表格样式 */
.tech-table {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 8px;
  overflow: hidden;
}

.tech-table :deep(.el-table__header-wrapper) th {
  background: rgba(59, 130, 246, 0.1);
  color: #94a3b8;
  font-weight: 500;
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
}

.tech-table :deep(.el-table__body-wrapper) tr {
  transition: all 0.3s ease;
}

.tech-table :deep(.el-table__body-wrapper) tr:hover {
  background: rgba(59, 130, 246, 0.05);
}

.tech-table :deep(.el-table__body-wrapper) td {
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  color: #f8fafc;
}

/* 加载状态 */
.table-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(8px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  animation: fadeIn 0.3s ease;
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 16px;
  animation: spin 1.5s linear infinite;
  color: #3b82f6;
}

/* 对话框样式 */
.tech-dialog {
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.tech-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
  padding: 20px;
}

.tech-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #f8fafc;
}

.tech-dialog :deep(.el-dialog__body) {
  padding: 24px 20px;
  color: #f8fafc;
}

.tech-dialog :deep(.el-dialog__footer) {
  border-top: 1px solid rgba(59, 130, 246, 0.2);
  padding: 16px 20px;
  background: rgba(15, 23, 42, 0.6);
  border-radius: 0 0 12px 12px;
}

/* 表单样式 */
.tech-form :deep(.el-form-item__label) {
  color: #94a3b8;
  font-weight: 500;
}

.tech-input,
.tech-select {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 6px;
  color: #f8fafc;
  transition: all 0.3s ease;
}

.tech-input:focus,
.tech-select:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.tech-input :deep(.el-input__inner),
.tech-select :deep(.el-select__input) {
  color: #f8fafc;
  background: transparent;
}

.tech-select :deep(.el-select__caret) {
  color: #94a3b8;
}

/* 对话框底部按钮 */
.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .milvus-collections {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .tech-button {
    align-self: flex-end;
  }
  
  .tech-dialog {
    width: 90% !important;
  }
}
</style>