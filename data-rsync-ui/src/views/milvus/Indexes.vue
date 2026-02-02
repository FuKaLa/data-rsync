<template>
  <div class="milvus-indexes">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>索引管理</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="margin-bottom: 20px">
        <el-form-item label="集合名称" prop="collectionName">
          <el-select v-model="form.collectionName" placeholder="请选择集合名称" @change="loadIndexes">
            <el-option v-for="collection in collections" :key="collection.name" :label="collection.name" :value="collection.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="indexes" style="width: 100%">
        <el-table-column prop="name" label="索引名称" />
        <el-table-column prop="type" label="索引类型" />
        <el-table-column prop="metricType" label="度量类型" />
        <el-table-column prop="dimension" label="维度" />
        <el-table-column prop="nlist" label="nlist" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="构建进度">
          <template #default="scope">
            <div v-if="scope.row.progress !== undefined">
              <el-progress :percentage="scope.row.progress" :format="() => `${scope.row.progress}%`" />
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="resources" label="占用资源">
          <template #default="scope">
            <div v-if="scope.row.resources">
              <div>内存: {{ scope.row.resources.memory }} MB</div>
              <div>磁盘: {{ scope.row.resources.disk }} MB</div>
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="handleViewIndex(scope.row)">详情</el-button>
            <el-button size="small" type="warning" @click="handleRebuildIndex(scope.row.name)" v-if="scope.row.status === 'FAILED'">重建</el-button>
            <el-button size="small" type="danger" @click="handleDeleteIndex(scope.row.name)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-divider>创建索引</el-divider>
      <el-form :model="indexForm" :rules="indexRules" ref="indexFormRef" label-width="120px">
        <el-form-item label="索引名称" prop="name">
          <el-input v-model="indexForm.name" placeholder="请输入索引名称" />
        </el-form-item>
        <el-form-item label="索引类型" prop="type">
          <el-select v-model="indexForm.type" placeholder="请选择索引类型" @change="handleIndexTypeChange">
            <el-option label="IVF_FLAT" value="IVF_FLAT" />
            <el-option label="IVF_SQ8" value="IVF_SQ8" />
            <el-option label="IVF_PQ" value="IVF_PQ" />
            <el-option label="HNSW" value="HNSW" />
            <el-option label="FLAT" value="FLAT" />
            <el-option label="BIN_FLAT" value="BIN_FLAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="度量类型" prop="metricType">
          <el-select v-model="indexForm.metricType" placeholder="请选择度量类型">
            <el-option label="L2" value="L2" />
            <el-option label="IP" value="IP" />
            <el-option label="COSINE" value="COSINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="维度" prop="dimension">
          <el-input v-model="indexForm.dimension" type="number" placeholder="请输入维度" />
        </el-form-item>
        <el-form-item label="nlist" prop="nlist" v-if="['IVF_FLAT', 'IVF_SQ8', 'IVF_PQ'].includes(indexForm.type)">
          <el-input v-model="indexForm.nlist" type="number" placeholder="请输入nlist" />
          <el-tooltip content="推荐值: 4 * sqrt(数据量)" placement="top">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="efConstruction" prop="efConstruction" v-if="indexForm.type === 'HNSW'">
          <el-input v-model="indexForm.efConstruction" type="number" placeholder="请输入efConstruction" />
          <el-tooltip content="推荐值: 128-256" placement="top">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="M" prop="M" v-if="indexForm.type === 'HNSW'">
          <el-input v-model="indexForm.M" type="number" placeholder="请输入M" />
          <el-tooltip content="推荐值: 16-64" placement="top">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCreateIndex">创建索引</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 索引详情对话框 -->
    <el-dialog
      v-model="indexDetailVisible"
      title="索引详情"
      width="800px"
    >
      <div v-if="currentIndex" class="index-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="索引名称">{{ currentIndex.name }}</el-descriptions-item>
          <el-descriptions-item label="索引类型">{{ currentIndex.type }}</el-descriptions-item>
          <el-descriptions-item label="度量类型">{{ currentIndex.metricType }}</el-descriptions-item>
          <el-descriptions-item label="维度">{{ currentIndex.dimension }}</el-descriptions-item>
          <el-descriptions-item label="nlist" v-if="currentIndex.nlist">{{ currentIndex.nlist }}</el-descriptions-item>
          <el-descriptions-item label="efConstruction" v-if="currentIndex.efConstruction">{{ currentIndex.efConstruction }}</el-descriptions-item>
          <el-descriptions-item label="M" v-if="currentIndex.M">{{ currentIndex.M }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentIndex.status }}</el-descriptions-item>
          <el-descriptions-item label="构建进度">{{ currentIndex.progress || 0 }}%</el-descriptions-item>
          <el-descriptions-item label="占用资源">
            <div v-if="currentIndex.resources">
              <div>内存: {{ currentIndex.resources.memory }} MB</div>
              <div>磁盘: {{ currentIndex.resources.disk }} MB</div>
            </div>
            <span v-else>--</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentIndex.createdTime }}</el-descriptions-item>
          <el-descriptions-item label="错误信息" v-if="currentIndex.errorMessage">
            <div class="error-message">{{ currentIndex.errorMessage }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="indexDetailVisible = false">关闭</el-button>
          <el-button type="warning" @click="handleRebuildIndex(currentIndex?.name)" v-if="currentIndex?.status === 'FAILED'">重建</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { milvusApi } from '@/api'
import { InfoFilled } from '@element-plus/icons-vue'
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
    const response = await milvusApi.getCollections()
    collections.value = response.data || []
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
.milvus-indexes {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-icon {
  margin-left: 8px;
  cursor: help;
  color: #409eff;
}

.index-detail {
  max-height: 600px;
  overflow-y: auto;
}

.error-message {
  color: #f56c6c;
  background-color: #fef0f0;
  padding: 10px;
  border-radius: 4px;
  border-left: 4px solid #f56c6c;
}

.el-form-item {
  margin-bottom: 16px;
}
</style>