<template>
  <div class="milvus-advanced">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>Milvus高级功能</span>
          <div class="header-actions">
            <el-button type="primary" @click="activeTab = 'batch'">批量操作</el-button>
            <el-button @click="activeTab = 'consistency'">一致性检查</el-button>
            <el-button @click="activeTab = 'collections'">集合管理</el-button>
            <el-button @click="activeTab = 'health'">健康状态</el-button>
          </div>
        </div>
      </template>
      
      <!-- 批量操作 -->
      <div v-if="activeTab === 'batch'" class="tab-content">
        <el-form :model="batchForm" label-width="120px" class="mb-4">
          <el-form-item label="集合名称" required>
            <el-select v-model="batchForm.collectionName" placeholder="请选择集合">
              <el-option v-for="collection in collections" :key="collection.name" :label="collection.name" :value="collection.name" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作类型" required>
            <el-select v-model="batchForm.operation" placeholder="请选择操作类型">
              <el-option label="插入数据" value="insert" />
              <el-option label="删除数据" value="delete" />
            </el-select>
          </el-form-item>
          <el-form-item label="批量数据" v-if="batchForm.operation === 'insert'">
            <el-input type="textarea" v-model="batchForm.data" placeholder="请输入JSON格式的数据列表" :rows="6" />
            <el-button size="small" type="info" @click="loadSampleData" class="mt-2">加载示例数据</el-button>
          </el-form-item>
          <el-form-item label="数据ID列表" v-if="batchForm.operation === 'delete'">
            <el-input type="textarea" v-model="batchForm.ids" placeholder="请输入ID列表，用逗号分隔" :rows="4" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="executeBatchOperation">执行批量操作</el-button>
            <el-button @click="resetBatchForm">重置</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 批量操作结果 -->
        <el-card v-if="batchResult" class="result-card">
          <template #header>
            <div class="result-header">
              <span>操作结果</span>
            </div>
          </template>
          <pre class="result-content">{{ JSON.stringify(batchResult, null, 2) }}</pre>
        </el-card>
      </div>
      
      <!-- 一致性检查 -->
      <div v-if="activeTab === 'consistency'" class="tab-content">
        <el-form :model="consistencyForm" label-width="120px" class="mb-4">
          <el-form-item label="集合名称" required>
            <el-select v-model="consistencyForm.collectionName" placeholder="请选择集合">
              <el-option v-for="collection in collections" :key="collection.name" :label="collection.name" :value="collection.name" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="checkConsistency">执行一致性检查</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 一致性检查结果 -->
        <el-card v-if="consistencyResult" class="result-card">
          <template #header>
            <div class="result-header">
              <span>一致性检查结果</span>
              <el-tag :type="consistencyResult.consistent ? 'success' : 'danger'">
                {{ consistencyResult.consistent ? '一致' : '不一致' }}
              </el-tag>
            </div>
          </template>
          <div class="consistency-result">
            <el-descriptions :column="2">
              <el-descriptions-item label="源数据数量">{{ consistencyResult.sourceCount }}</el-descriptions-item>
              <el-descriptions-item label="目标数据数量">{{ consistencyResult.targetCount }}</el-descriptions-item>
              <el-descriptions-item label="样本检查通过数">{{ consistencyResult.sampleCheckPassed }}</el-descriptions-item>
              <el-descriptions-item label="样本检查总数">{{ consistencyResult.sampleCheckTotal }}</el-descriptions-item>
              <el-descriptions-item label="检查时间" :span="2">{{ consistencyResult.checkTime }}</el-descriptions-item>
            </el-descriptions>
            
            <el-alert
              v-if="!consistencyResult.consistent && consistencyResult.discrepancies && consistencyResult.discrepancies.length > 0"
              :title="'发现' + consistencyResult.discrepancies.length + '个不一致项'"
              type="error"
              show-icon
              class="mt-4"
            >
              <el-collapse>
                <el-collapse-item title="查看详细差异">
                  <el-list>
                    <el-list-item v-for="(item, index) in consistencyResult.discrepancies" :key="index">
                      {{ item }}
                    </el-list-item>
                  </el-list>
                </el-collapse-item>
              </el-collapse>
            </el-alert>
          </div>
        </el-card>
      </div>
      
      <!-- 集合管理 -->
      <div v-if="activeTab === 'collections'" class="tab-content">
        <div class="header-actions mb-4">
          <el-button type="primary" @click="showCreateCollectionDialog = true">创建集合</el-button>
          <el-button @click="loadCollections">刷新</el-button>
        </div>
        
        <el-table :data="collections" style="width: 100%">
          <el-table-column prop="name" label="集合名称" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'healthy' ? 'success' : 'danger'">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="数据量" width="100" />
          <el-table-column label="操作" width="250">
            <template #default="scope">
              <el-button size="small" @click="showCollectionStats(scope.row.name)">统计信息</el-button>
              <el-button size="small" @click="optimizeCollection(scope.row.name)">优化</el-button>
              <el-button size="small" type="danger" @click="deleteCollection(scope.row.name)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 健康状态 -->
      <div v-if="activeTab === 'health'" class="tab-content">
        <el-button type="primary" @click="checkHealth" class="mb-4">检查健康状态</el-button>
        
        <el-card v-if="healthStatus" class="health-card">
          <template #header>
            <div class="result-header">
              <span>Milvus健康状态</span>
              <el-tag :type="healthStatus.status === 'healthy' ? 'success' : 'danger'">
                {{ healthStatus.status }}
              </el-tag>
            </div>
          </template>
          <div class="health-status">
            <el-descriptions :column="2">
              <el-descriptions-item label="服务状态">{{ healthStatus.serviceStatus }}</el-descriptions-item>
              <el-descriptions-item label="连接状态">{{ healthStatus.connectionStatus }}</el-descriptions-item>
              <el-descriptions-item label="集合数量">{{ healthStatus.collectionCount }}</el-descriptions-item>
              <el-descriptions-item label="索引数量">{{ healthStatus.indexCount }}</el-descriptions-item>
              <el-descriptions-item label="内存使用" :span="2">{{ healthStatus.memoryUsage }}</el-descriptions-item>
              <el-descriptions-item label="CPU使用率" :span="2">{{ healthStatus.cpuUsage }}</el-descriptions-item>
              <el-descriptions-item label="检查时间" :span="2">{{ healthStatus.checkTime }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>
    </el-card>
    
    <!-- 创建集合对话框 -->
    <el-dialog
      v-model="showCreateCollectionDialog"
      title="创建Milvus集合"
      width="600px"
    >
      <el-form :model="createCollectionForm" label-width="120px">
        <el-form-item label="集合名称" required>
          <el-input v-model="createCollectionForm.collectionName" placeholder="请输入集合名称" />
        </el-form-item>
        <el-form-item label="维度" required>
          <el-input-number v-model="createCollectionForm.dimension" :min="1" :max="10000" placeholder="请输入向量维度" />
        </el-form-item>
        <el-form-item label="度量类型" required>
          <el-select v-model="createCollectionForm.metricType" placeholder="请选择度量类型">
            <el-option label="L2" value="L2" />
            <el-option label="IP" value="IP" />
            <el-option label="COSINE" value="COSINE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateCollectionDialog = false">取消</el-button>
          <el-button type="primary" @click="createCollection">创建</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 集合统计信息对话框 -->
    <el-dialog
      v-model="showStatsDialog"
      :title="'集合统计信息 - ' + statsCollectionName"
      width="800px"
    >
      <pre v-if="collectionStats">{{ JSON.stringify(collectionStats, null, 2) }}</pre>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showStatsDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { milvusApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 标签页
const activeTab = ref('batch')

// 集合列表
const collections = ref<any[]>([])

// 批量操作表单
const batchForm = reactive({
  collectionName: '',
  operation: 'insert',
  data: '',
  ids: ''
})

// 批量操作结果
const batchResult = ref<any>(null)

// 一致性检查表单
const consistencyForm = reactive({
  collectionName: ''
})

// 一致性检查结果
const consistencyResult = ref<any>(null)

// 健康状态
const healthStatus = ref<any>(null)

// 集合统计信息
const collectionStats = ref<any>(null)
const statsCollectionName = ref('')
const showStatsDialog = ref(false)

// 创建集合
const showCreateCollectionDialog = ref(false)
const createCollectionForm = reactive({
  collectionName: '',
  dimension: 128,
  metricType: 'L2'
})

onMounted(() => {
  loadCollections()
})

// 加载集合列表
const loadCollections = async () => {
  try {
    const response = await milvusApi.getCollections()
    collections.value = response.data || []
  } catch (error) {
    console.error('Failed to load collections:', error)
    ElMessage.error('加载集合列表失败')
  }
}

// 加载示例数据
const loadSampleData = () => {
  const sampleData = [
    {
      id: 1,
      vector: Array.from({ length: 128 }, () => Math.random()),
      title: '示例文档1',
      content: '这是一个示例文档内容'
    },
    {
      id: 2,
      vector: Array.from({ length: 128 }, () => Math.random()),
      title: '示例文档2',
      content: '这是另一个示例文档内容'
    }
  ]
  batchForm.data = JSON.stringify(sampleData, null, 2)
}

// 执行批量操作
const executeBatchOperation = async () => {
  try {
    if (!batchForm.collectionName) {
      ElMessage.warning('请选择集合名称')
      return
    }
    
    if (batchForm.operation === 'insert') {
      if (!batchForm.data) {
        ElMessage.warning('请输入批量数据')
        return
      }
      
      const data = JSON.parse(batchForm.data)
      const response = await milvusApi.insertData(batchForm.collectionName, data)
      batchResult.value = response
      ElMessage.success('批量插入成功')
    } else if (batchForm.operation === 'delete') {
      if (!batchForm.ids) {
        ElMessage.warning('请输入数据ID列表')
        return
      }
      
      const ids = batchForm.ids.split(',').map(id => parseInt(id.trim()))
      const response = await milvusApi.deleteData(batchForm.collectionName, ids)
      batchResult.value = response
      ElMessage.success('批量删除成功')
    }
  } catch (error) {
    console.error('Failed to execute batch operation:', error)
    ElMessage.error('执行批量操作失败')
  }
}

// 重置批量操作表单
const resetBatchForm = () => {
  Object.assign(batchForm, {
    collectionName: '',
    operation: 'insert',
    data: '',
    ids: ''
  })
  batchResult.value = null
}

// 执行一致性检查
const checkConsistency = async () => {
  try {
    if (!consistencyForm.collectionName) {
      ElMessage.warning('请选择集合名称')
      return
    }
    
    const response = await milvusApi.checkConsistency(consistencyForm.collectionName)
    consistencyResult.value = {
      ...response.data,
      checkTime: new Date().toLocaleString()
    }
    ElMessage.success('一致性检查完成')
  } catch (error) {
    console.error('Failed to check consistency:', error)
    ElMessage.error('执行一致性检查失败')
  }
}

// 检查健康状态
const checkHealth = async () => {
  try {
    const response = await milvusApi.getHealth()
    healthStatus.value = {
      ...response.data,
      checkTime: new Date().toLocaleString()
    }
    ElMessage.success('健康状态检查完成')
  } catch (error) {
    console.error('Failed to check health:', error)
    ElMessage.error('检查健康状态失败')
  }
}

// 查看集合统计信息
const showCollectionStats = async (collectionName: string) => {
  try {
    statsCollectionName.value = collectionName
    const response = await milvusApi.getCollectionStats(collectionName)
    collectionStats.value = response.data
    showStatsDialog.value = true
  } catch (error) {
    console.error('Failed to get collection stats:', error)
    ElMessage.error('获取集合统计信息失败')
  }
}

// 优化集合
const optimizeCollection = async (collectionName: string) => {
  try {
    await ElMessageBox.confirm(`确定要优化集合 ${collectionName} 吗？`, '优化确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await milvusApi.optimizeCollection(collectionName)
    ElMessage.success('集合优化成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to optimize collection:', error)
      ElMessage.error('优化集合失败')
    }
  }
}

// 删除集合
const deleteCollection = async (collectionName: string) => {
  try {
    await ElMessageBox.confirm(`确定要删除集合 ${collectionName} 吗？此操作不可恢复！`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    })
    
    const response = await milvusApi.deleteCollection(collectionName)
    ElMessage.success('集合删除成功')
    loadCollections()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete collection:', error)
      ElMessage.error('删除集合失败')
    }
  }
}

// 创建集合
const createCollection = async () => {
  try {
    if (!createCollectionForm.collectionName) {
      ElMessage.warning('请输入集合名称')
      return
    }
    
    const response = await milvusApi.createCollection({
      collectionName: createCollectionForm.collectionName,
      dimension: createCollectionForm.dimension,
      metricType: createCollectionForm.metricType
    })
    
    ElMessage.success('集合创建成功')
    showCreateCollectionDialog.value = false
    loadCollections()
  } catch (error) {
    console.error('Failed to create collection:', error)
    ElMessage.error('创建集合失败')
  }
}
</script>

<style scoped>
.milvus-advanced {
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
  gap: 12px;
}

.header-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.header-actions :deep(.el-button--primary) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border: none;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  color: #ffffff;
}

.header-actions :deep(.el-button--primary::before) {
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

.header-actions :deep(.el-button--primary:active::before) {
  width: 300px;
  height: 300px;
}

.header-actions :deep(.el-button--primary:hover) {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.5);
}

.tab-content {
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

:deep(.el-form) {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper),
:deep(.el-input-number .el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover),
:deep(.el-input-number .el-input__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

:deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select-dropdown) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
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

/* 结果卡片 */
.result-card {
  margin-top: 24px;
  animation: fadeInUp 0.8s ease-out 0.6s both;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 1px solid rgba(59, 130, 246, 0.3);
}

.result-header span {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.result-content {
  padding: 24px;
  background: rgba(15, 23, 42, 0.7);
  border-radius: 12px;
  overflow-x: auto;
  color: rgba(255, 255, 255, 0.8);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
}

/* 健康状态卡片 */
.health-card {
  margin-top: 24px;
  animation: fadeInUp 0.8s ease-out 0.6s both;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

.health-status {
  padding: 24px;
}

.consistency-result {
  padding: 24px;
}

:deep(.el-descriptions) {
  background: rgba(15, 23, 42, 0.7);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

:deep(.el-descriptions__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

:deep(.el-descriptions__content) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-alert) {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-alert__title) {
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-collapse) {
  margin-top: 16px;
  background: rgba(239, 68, 68, 0.05);
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-collapse-item__header) {
  background: rgba(239, 68, 68, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(239, 68, 68, 0.2);
}

:deep(.el-collapse-item__content) {
  background: rgba(239, 68, 68, 0.05);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-list) {
  padding: 16px;
}

:deep(.el-list-item) {
  background: rgba(239, 68, 68, 0.05);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
  margin-bottom: 8px;
  padding: 12px;
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

:deep(.el-dialog__body .el-form) {
  background: transparent;
  padding: 0;
  box-shadow: none;
  border: none;
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
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 0 20px;
  }
  
  .header-actions {
    align-self: flex-start;
  }
  
  .header-actions :deep(.el-button) {
    padding: 10px 20px;
    font-size: 14px;
  }
  
  .card-header span {
    font-size: 18px;
  }
  
  :deep(.el-form) {
    padding: 20px;
  }
  
  :deep(.el-table__cell) {
    padding: 14px 12px;
    font-size: 13px;
  }
  
  :deep(.el-table__header th) {
    padding: 14px 12px;
    font-size: 13px;
  }
}

@media (max-width: 992px) {
  .milvus-advanced {
    padding: 16px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  :deep(.el-form) {
    padding: 16px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
  
  :deep(.el-table__cell) {
    padding: 12px 8px;
  }
  
  :deep(.el-button) {
    margin-right: 4px;
    padding: 6px 12px;
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .card-header {
    padding: 0 16px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .header-actions :deep(.el-button) {
    padding: 8px 16px;
    font-size: 12px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table__cell) {
    padding: 8px 4px;
  }
  
  :deep(.el-button) {
    margin-right: 2px;
    padding: 4px 8px;
    font-size: 10px;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  .result-content {
    padding: 16px;
    font-size: 12px;
  }
}
</style>