<template>
  <div class="milvus-indexes" v-animate="'fadeIn'">
    <!-- 页面标题 -->
    <div class="page-header" v-animate="'slideUp'">
      <h1 class="page-title">Milvus 索引管理</h1>
      <p class="page-description">高效管理和监控向量索引，提升查询性能</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards" v-animate="'fadeIn'" style="animation-delay: 0.2s">
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ indexes.filter(i => i.status === 'READY').length }}</div>
          <div class="stat-label">就绪索引</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">
          <el-icon><Loading /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ indexes.filter(i => i.status === 'BUILDING').length }}</div>
          <div class="stat-label">构建中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">
          <el-icon><Close /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ indexes.filter(i => i.status === 'FAILED').length }}</div>
          <div class="stat-label">失败索引</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon info">
          <el-icon><CollectionTag /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ collections.length }}</div>
          <div class="stat-label">可用集合</div>
        </div>
      </div>
    </div>

    <el-card shadow="hover" v-animate="'fadeIn'" style="animation-delay: 0.4s">
      <template #header>
        <div class="card-header">
          <span>索引列表</span>
          <div class="header-actions">
            <el-button size="small" @click="loadCollections" plain>
              <el-icon><Refresh /></el-icon>
              刷新集合
            </el-button>
          </div>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="margin-bottom: 20px">
        <el-form-item label="集合名称" prop="collectionName" v-animate="'slideRight'">
          <el-select v-model="form.collectionName" placeholder="请选择集合名称" @change="loadIndexes" class="w-100">
            <el-option v-for="collection in collections" :key="collection.name" :label="collection.name" :value="collection.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="indexes" style="width: 100%; animation-delay: 0.5s" v-animate="'fadeIn'">
        <el-table-column prop="name" label="索引名称" />
        <el-table-column prop="type" label="索引类型" />
        <el-table-column prop="metricType" label="度量类型" />
        <el-table-column prop="dimension" label="维度" />
        <el-table-column prop="nlist" label="nlist" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" class="status-tag">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="构建进度">
          <template #default="scope">
            <div v-if="scope.row.progress !== undefined" class="progress-container">
              <el-progress :percentage="scope.row.progress" :format="() => `${scope.row.progress}%`" />
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="resources" label="占用资源">
          <template #default="scope">
            <div v-if="scope.row.resources" class="resources-info">
              <div class="resource-item">
              <el-icon class="resource-icon"><Memo /></el-icon>
              {{ scope.row.resources.memory }} MB
            </div>
            <div class="resource-item">
              <el-icon class="resource-icon"><DataAnalysis /></el-icon>
              {{ scope.row.resources.disk }} MB
            </div>
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="handleViewIndex(scope.row)" class="action-btn view">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button size="small" type="warning" @click="handleRebuildIndex(scope.row.name)" v-if="scope.row.status === 'FAILED'" class="action-btn rebuild">
                <el-icon><Refresh /></el-icon>
                重建
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteIndex(scope.row.name)" class="action-btn delete">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-divider>创建索引</el-divider>
      <el-form :model="indexForm" :rules="indexRules" ref="indexFormRef" label-width="120px" v-animate="'fadeIn'" style="animation-delay: 0.6s">
        <el-form-item label="索引名称" prop="name" v-animate="'slideRight'">
          <el-input v-model="indexForm.name" placeholder="请输入索引名称" />
        </el-form-item>
        <el-form-item label="索引类型" prop="type" v-animate="'slideRight'" style="animation-delay: 0.1s">
          <el-select v-model="indexForm.type" placeholder="请选择索引类型" @change="handleIndexTypeChange">
            <el-option label="IVF_FLAT" value="IVF_FLAT" />
            <el-option label="IVF_SQ8" value="IVF_SQ8" />
            <el-option label="IVF_PQ" value="IVF_PQ" />
            <el-option label="HNSW" value="HNSW" />
            <el-option label="FLAT" value="FLAT" />
            <el-option label="BIN_FLAT" value="BIN_FLAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="度量类型" prop="metricType" v-animate="'slideRight'" style="animation-delay: 0.2s">
          <el-select v-model="indexForm.metricType" placeholder="请选择度量类型">
            <el-option label="L2" value="L2" />
            <el-option label="IP" value="IP" />
            <el-option label="COSINE" value="COSINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="维度" prop="dimension" v-animate="'slideRight'" style="animation-delay: 0.3s">
          <el-input v-model="indexForm.dimension" type="number" placeholder="请输入维度" />
        </el-form-item>
        <el-form-item label="nlist" prop="nlist" v-if="['IVF_FLAT', 'IVF_SQ8', 'IVF_PQ'].includes(indexForm.type)" v-animate="'slideRight'" style="animation-delay: 0.4s">
          <el-input v-model="indexForm.nlist" type="number" placeholder="请输入nlist" />
          <el-tooltip content="推荐值: 4 * sqrt(数据量)" placement="top">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="efConstruction" prop="efConstruction" v-if="indexForm.type === 'HNSW'" v-animate="'slideRight'" style="animation-delay: 0.4s">
          <el-input v-model="indexForm.efConstruction" type="number" placeholder="请输入efConstruction" />
          <el-tooltip content="推荐值: 128-256" placement="top">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="M" prop="M" v-if="indexForm.type === 'HNSW'" v-animate="'slideRight'" style="animation-delay: 0.5s">
          <el-input v-model="indexForm.M" type="number" placeholder="请输入M" />
          <el-tooltip content="推荐值: 16-64" placement="top">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item v-animate="'slideUp'" style="animation-delay: 0.6s">
          <el-button type="primary" @click="handleCreateIndex" class="create-btn">
            <el-icon><Plus /></el-icon>
            创建索引
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 索引详情对话框 -->
    <el-dialog
      v-model="indexDetailVisible"
      title="索引详情"
      width="800px"
      custom-class="index-detail-dialog"
    >
      <div v-if="currentIndex" class="index-detail" v-animate="'fadeIn'">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="索引名称" class="desc-item">{{ currentIndex.name }}</el-descriptions-item>
          <el-descriptions-item label="索引类型" class="desc-item">{{ currentIndex.type }}</el-descriptions-item>
          <el-descriptions-item label="度量类型" class="desc-item">{{ currentIndex.metricType }}</el-descriptions-item>
          <el-descriptions-item label="维度" class="desc-item">{{ currentIndex.dimension }}</el-descriptions-item>
          <el-descriptions-item label="nlist" v-if="currentIndex.nlist" class="desc-item">{{ currentIndex.nlist }}</el-descriptions-item>
          <el-descriptions-item label="efConstruction" v-if="currentIndex.efConstruction" class="desc-item">{{ currentIndex.efConstruction }}</el-descriptions-item>
          <el-descriptions-item label="M" v-if="currentIndex.M" class="desc-item">{{ currentIndex.M }}</el-descriptions-item>
          <el-descriptions-item label="状态" class="desc-item">
            <el-tag :type="getStatusType(currentIndex.status)">{{ currentIndex.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="构建进度" class="desc-item">
            <el-progress :percentage="currentIndex.progress || 0" :format="() => `${currentIndex.progress || 0}%`" />
          </el-descriptions-item>
          <el-descriptions-item label="占用资源" class="desc-item" :span="2">
            <div v-if="currentIndex.resources" class="resource-detail">
              <div class="resource-bar">
                <div class="resource-label">内存</div>
                <div class="resource-progress">
                  <div class="resource-progress-bar" :style="{ width: Math.min(currentIndex.resources.memory / 100, 100) + '%' }"></div>
                </div>
                <div class="resource-value">{{ currentIndex.resources.memory }} MB</div>
              </div>
              <div class="resource-bar">
                <div class="resource-label">磁盘</div>
                <div class="resource-progress">
                  <div class="resource-progress-bar disk" :style="{ width: Math.min(currentIndex.resources.disk / 200, 100) + '%' }"></div>
                </div>
                <div class="resource-value">{{ currentIndex.resources.disk }} MB</div>
              </div>
            </div>
            <span v-else>--</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" class="desc-item" :span="2">{{ currentIndex.createdTime }}</el-descriptions-item>
          <el-descriptions-item label="错误信息" v-if="currentIndex.errorMessage" class="desc-item" :span="2">
            <div class="error-message">
              <el-icon class="error-icon"><Warning /></el-icon>
              {{ currentIndex.errorMessage }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="indexDetailVisible = false" class="dialog-btn">关闭</el-button>
          <el-button type="warning" @click="handleRebuildIndex(currentIndex?.name)" v-if="currentIndex?.status === 'FAILED'" class="dialog-btn rebuild">重建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { 
  InfoFilled, 
  Check, 
  Loading, 
  Close, 
  CollectionTag, 
  Refresh, 
  View, 
  Delete, 
  Plus, 
  Memo, 
  DataAnalysis, 
  Warning 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const form = ref({
  collectionName: ''
})

const formRef = ref<any>(null)

const indexForm = ref({
  name: '',
  type: 'IVF_FLAT',
  metricType: 'L2',
  dimension: 128,
  nlist: 1024,
  efConstruction: 256,
  M: 16
})

const indexFormRef = ref<any>(null)

const collections = ref<any[]>([])
const indexes = ref<any[]>([])

// 索引详情对话框
const indexDetailVisible = ref(false)
const currentIndex = ref<any>(null)

const rules = ref({
  collectionName: [
    { required: true, message: '请选择集合名称', trigger: 'change' }
  ]
})

const indexRules = ref({
  name: [
    { required: true, message: '请输入索引名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择索引类型', trigger: 'change' }
  ],
  metricType: [
    { required: true, message: '请选择度量类型', trigger: 'change' }
  ],
  dimension: [
    { required: true, message: '请输入维度', trigger: 'blur' }
  ],
  nlist: [
    { required: true, message: '请输入nlist', trigger: 'blur' }
  ],
  efConstruction: [
    { required: true, message: '请输入efConstruction', trigger: 'blur' }
  ],
  M: [
    { required: true, message: '请输入M', trigger: 'blur' }
  ]
})

onMounted(() => {
  loadCollections()
})

const loadCollections = async () => {
  try {
    // 暂时使用模拟数据，后续可根据实际API返回的数据更新
    console.log('Loading collections...')
    // 模拟数据
    collections.value = [
      {
        name: 'user_embeddings',
        description: '用户向量数据',
        count: 100000,
        createdTime: '2024-01-01 10:00:00'
      },
      {
        name: 'product_embeddings',
        description: '产品向量数据',
        count: 50000,
        createdTime: '2024-01-02 14:30:00'
      }
    ]
  } catch (error) {
    console.error('Failed to load collections:', error)
    ElMessage.error('加载集合列表失败')
  }
}

const loadIndexes = async () => {
  if (!form.value.collectionName) return
  
  try {
    // 模拟索引数据，实际应调用API获取
    setTimeout(() => {
      indexes.value = [
        {
          name: 'idx_ivf_flat',
          type: 'IVF_FLAT',
          metricType: 'L2',
          dimension: 128,
          nlist: 1024,
          status: 'READY',
          progress: 100,
          resources: {
            memory: 512,
            disk: 1024
          },
          createdTime: '2026-02-01 10:00:00'
        },
        {
          name: 'idx_hnsw',
          type: 'HNSW',
          metricType: 'IP',
          dimension: 256,
          efConstruction: 256,
          M: 16,
          status: 'FAILED',
          progress: 75,
          resources: {
            memory: 2048,
            disk: 4096
          },
          errorMessage: '内存不足，无法完成索引构建',
          createdTime: '2026-02-01 11:00:00'
        },
        {
          name: 'idx_ivf_sq8',
          type: 'IVF_SQ8',
          metricType: 'COSINE',
          dimension: 128,
          nlist: 2048,
          status: 'BUILDING',
          progress: 45,
          resources: {
            memory: 1024,
            disk: 2048
          },
          createdTime: '2026-02-01 12:00:00'
        }
      ]
    }, 500)
  } catch (error) {
    console.error('Failed to load indexes:', error)
    ElMessage.error('加载索引列表失败')
  }
}

const handleCreateIndex = async () => {
  if (indexFormRef.value) {
    await indexFormRef.value.validate(async (valid: boolean) => {
      if (valid && form.value.collectionName) {
        try {
          // 模拟创建索引，实际应调用API
          setTimeout(() => {
            ElMessage.success('索引创建成功')
            loadIndexes()
          }, 1000)
        } catch (error) {
          console.error('Failed to create index:', error)
          ElMessage.error('创建索引失败')
        }
      }
    })
  }
}

const handleDeleteIndex = async (indexName: string) => {
  if (!form.value.collectionName) return
  
  try {
    // 模拟删除索引，实际应调用API
    setTimeout(() => {
      ElMessage.success('索引删除成功')
      loadIndexes()
    }, 500)
  } catch (error) {
    console.error('Failed to delete index:', error)
    ElMessage.error('删除索引失败')
  }
}

const handleViewIndex = (index: any) => {
  currentIndex.value = index
  indexDetailVisible.value = true
}

const handleRebuildIndex = (indexName: string) => {
  if (!form.value.collectionName) return
  
  try {
    // 模拟重建索引，实际应调用API
    setTimeout(() => {
      ElMessage.success('索引重建成功')
      loadIndexes()
    }, 1000)
  } catch (error) {
    console.error('Failed to rebuild index:', error)
    ElMessage.error('重建索引失败')
  }
}

const handleIndexTypeChange = () => {
  // 根据索引类型设置默认值
  if (indexForm.value.type === 'HNSW') {
    indexForm.value.efConstruction = 256
    indexForm.value.M = 16
  } else if (['IVF_FLAT', 'IVF_SQ8', 'IVF_PQ'].includes(indexForm.value.type)) {
    indexForm.value.nlist = 1024
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'READY':
      return 'success'
    case 'BUILDING':
      return 'warning'
    case 'FAILED':
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped>
/* 动画关键帧 */
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

@keyframes slideRight {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 动画指令 */
[v-animate] {
  animation-duration: 0.8s;
  animation-fill-mode: both;
  animation-timing-function: cubic-bezier(0.16, 1, 0.3, 1);
}

[v-animate="fadeIn"] {
  animation-name: fadeIn;
}

[v-animate="slideUp"] {
  animation-name: slideUp;
}

[v-animate="slideRight"] {
  animation-name: slideRight;
}

/* 主容器样式 */
.milvus-indexes {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: 100vh;
  background-size: 200% 200%;
  animation: gradientShift 15s ease infinite;
}

/* 页面标题样式 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(90deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
  opacity: 0.9;
}

/* 统计卡片样式 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  animation: pulse 2s infinite;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  font-size: 24px;
  color: #fff;
  transition: all 0.3s ease;
}

.stat-icon.success {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  box-shadow: 0 4px 20px rgba(103, 194, 58, 0.4);
}

.stat-icon.warning {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
  box-shadow: 0 4px 20px rgba(230, 162, 60, 0.4);
}

.stat-icon.danger {
  background: linear-gradient(135deg, #f56c6c, #f78989);
  box-shadow: 0 4px 20px rgba(245, 108, 108, 0.4);
}

.stat-icon.info {
  background: linear-gradient(135deg, #409eff, #667eea);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.4);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  opacity: 0.8;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  margin-bottom: 20px;
}

.card-header span {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(90deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 表单样式 */
:deep(.el-form) {
  margin-bottom: 30px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

:deep(.el-form-item:hover) {
  transform: translateX(4px);
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 10px;
  transition: all 0.3s ease;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  border-color: #409eff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
  border-color: #409eff;
}

/* 信息图标样式 */
.info-icon {
  margin-left: 10px;
  cursor: help;
  color: #409eff;
  transition: all 0.3s ease;
  font-size: 16px;
}

.info-icon:hover {
  transform: scale(1.2);
  color: #667eea;
  animation: pulse 1s infinite;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-table__header-wrapper) {
  background: linear-gradient(90deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px 16px 0 0;
}

:deep(.el-table__header th) {
  font-weight: 600;
  color: #303133;
  background: transparent;
  border-bottom: 3px solid #409eff;
  padding: 16px;
}

:deep(.el-table__row) {
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

:deep(.el-table__row:hover) {
  background: rgba(64, 158, 255, 0.05) !important;
  border-left-color: #409eff;
  transform: translateX(4px);
}

:deep(.el-table__row.el-table__row--striped) {
  background: rgba(0, 0, 0, 0.02);
}

:deep(.el-table__cell) {
  padding: 18px;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

:deep(.el-table__cell:hover) {
  color: #409eff;
}

/* 状态标签样式 */
.status-tag {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.status-tag:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* 进度条样式 */
.progress-container {
  margin-top: 4px;
}

:deep(.el-progress) {
  margin-top: 8px;
}

:deep(.el-progress__bar) {
  border-radius: 12px;
  overflow: hidden;
  height: 8px;
}

:deep(.el-progress__bar-inner) {
  border-radius: 12px;
  background: linear-gradient(90deg, #409eff, #667eea);
  transition: width 1s ease;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

:deep(.el-progress__text) {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-top: 4px;
}

/* 资源信息样式 */
.resources-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.resource-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  padding: 6px 12px;
  background: rgba(64, 158, 255, 0.05);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.resource-item:hover {
  background: rgba(64, 158, 255, 0.1);
  transform: scale(1.05);
}

.resource-icon {
  font-size: 14px;
  color: #409eff;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn.view {
  background: linear-gradient(90deg, #409eff, #667eea);
  border: none;
  color: #fff;
}

.action-btn.rebuild {
  background: linear-gradient(90deg, #e6a23c, #ebb563);
  border: none;
  color: #fff;
}

.action-btn.delete {
  background: linear-gradient(90deg, #f56c6c, #f78989);
  border: none;
  color: #fff;
}

/* 创建按钮样式 */
.create-btn {
  border-radius: 10px;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(90deg, #409eff, #667eea);
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.3);
}

.create-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
  animation: pulse 1.5s infinite;
}

/* 分割线样式 */
:deep(.el-divider) {
  margin: 40px 0;
}

:deep(.el-divider__text) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  background: #fff;
  padding: 0 24px;
}

/* 索引详情样式 */
.index-detail {
  max-height: 600px;
  overflow-y: auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border-radius: 16px;
  padding: 24px;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 描述列表样式 */
:deep(.el-descriptions) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.desc-item {
  transition: all 0.3s ease;
}

.desc-item:hover {
  background: rgba(64, 158, 255, 0.05);
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #303133;
  background: rgba(64, 158, 255, 0.08);
  padding: 16px;
  font-size: 14px;
}

:deep(.el-descriptions__cell) {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

/* 资源详情样式 */
.resource-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.resource-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resource-label {
  width: 60px;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.resource-progress {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.resource-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #667eea);
  border-radius: 4px;
  transition: width 1s ease;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.resource-progress-bar.disk {
  background: linear-gradient(90deg, #67c23a, #85ce61);
  box-shadow: 0 0 10px rgba(103, 194, 58, 0.5);
}

.resource-value {
  width: 100px;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  text-align: right;
}

/* 错误信息样式 */
.error-message {
  color: #f56c6c;
  background-color: #fef0f0;
  padding: 16px;
  border-radius: 12px;
  border-left: 4px solid #f56c6c;
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.15);
  transition: all 0.3s ease;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.error-message:hover {
  box-shadow: 0 8px 32px rgba(245, 108, 108, 0.25);
  transform: translateX(4px);
}

.error-icon {
  font-size: 20px;
  margin-top: 2px;
  flex-shrink: 0;
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border: none;
  background: #fff;
  backdrop-filter: blur(10px);
}

/* 对话框样式 */
.index-detail-dialog {
  border-radius: 20px !important;
  overflow: hidden !important;
  box-shadow: 0 16px 60px rgba(0, 0, 0, 0.25) !important;
  animation: fadeIn 0.5s ease;
}

:deep(.el-dialog__header) {
  background: linear-gradient(90deg, #409eff, #667eea);
  color: #fff;
  padding: 24px;
  border-radius: 20px 20px 0 0;
}

:deep(.el-dialog__title) {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

:deep(.el-dialog__close) {
  color: #fff;
  font-size: 20px;
  transition: all 0.3s ease;
}

:deep(.el-dialog__close:hover) {
  transform: scale(1.2);
  color: #fff;
}

:deep(.el-dialog__body) {
  padding: 28px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

:deep(.el-dialog__footer) {
  padding: 24px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  border-radius: 0 0 20px 20px;
}

.dialog-btn {
  border-radius: 8px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.dialog-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dialog-btn.rebuild {
  background: linear-gradient(90deg, #e6a23c, #ebb563);
  border: none;
  color: #fff;
}

/* 分割线样式 */
:deep(.el-divider) {
  margin: 40px 0;
}

:deep(.el-divider__text) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  background: #fff;
  padding: 0 24px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 5px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #409eff, #667eea);
  border-radius: 5px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #667eea, #409eff);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .milvus-indexes {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  :deep(.el-form) {
    padding: 20px;
  }

  :deep(.el-table__cell) {
    padding: 12px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>