<template>
  <div class="sync-container">
    <el-card class="sync-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">数据同步</span>
          <el-button type="primary" @click="resetForm">重置</el-button>
        </div>
      </template>
      
      <el-form :model="syncForm" label-width="120px" class="sync-form">
        <!-- 同步类型选择 -->
        <el-form-item label="同步类型" required>
          <el-radio-group v-model="syncForm.syncType" @change="handleSyncTypeChange">
            <el-radio label="database">数据库同步</el-radio>
            <el-radio label="table">表同步</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 数据源选择 -->
        <el-form-item label="数据源" required>
          <el-select v-model="syncForm.dataSourceId" placeholder="请选择数据源" @change="handleDataSourceChange">
            <el-option 
              v-for="dataSource in dataSources" 
              :key="dataSource.id" 
              :label="dataSource.name" 
              :value="dataSource.id"
            />
          </el-select>
        </el-form-item>
        
        <!-- 数据库名称（表同步时显示） -->
        <el-form-item v-if="syncForm.syncType === 'table'" label="数据库名称" required>
          <el-input v-model="syncForm.databaseName" placeholder="请输入数据库名称" />
        </el-form-item>
        
        <!-- 表名称（表同步时显示） -->
        <el-form-item v-if="syncForm.syncType === 'table'" label="表名称" required>
          <el-input v-model="syncForm.tableName" placeholder="请输入表名称" />
        </el-form-item>
        
        <!-- SQL语句（数据库同步时显示） -->
        <el-form-item v-if="syncForm.syncType === 'database'" label="SQL语句" required>
          <el-input
            v-model="syncForm.sql"
            type="textarea"
            rows="4"
            placeholder="请输入SQL查询语句"
          />
        </el-form-item>
        
        <!-- 目标集合选择 -->
        <el-form-item label="目标集合" required>
          <el-select v-model="syncForm.collectionName" placeholder="请选择目标集合">
            <el-option 
              v-for="collection in collections" 
              :key="collection.name" 
              :label="collection.name" 
              :value="collection.name"
            />
          </el-select>
          <el-button type="primary" size="small" @click="showCreateCollectionDialog = true" style="margin-left: 10px">
            创建新集合
          </el-button>
        </el-form-item>
        
        <!-- 向量化字段 -->
        <el-form-item label="向量化字段" required>
          <el-select
            v-model="syncForm.vectorFields"
            multiple
            placeholder="请选择需要向量化的字段"
            style="width: 100%"
          >
            <el-option 
              v-for="field in availableFields" 
              :key="field" 
              :label="field" 
              :value="field"
            />
          </el-select>
        </el-form-item>
        
        <!-- 过滤条件（表同步时显示） -->
        <el-form-item v-if="syncForm.syncType === 'table'" label="过滤条件">
          <el-input v-model="syncForm.whereClause" placeholder="请输入WHERE条件（可选）" />
        </el-form-item>
        
        <!-- 同步按钮 -->
        <el-form-item>
          <el-button type="primary" @click="executeSync" :loading="syncLoading">
            执行同步
          </el-button>
          <el-button @click="resetForm">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 同步进度 -->
    <el-card v-if="syncProgress" class="progress-card" shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">同步进度</span>
        </div>
      </template>
      <div class="progress-content">
        <el-progress :percentage="syncProgress.progress" :status="syncProgress.status === 'FAILED' ? 'exception' : syncProgress.status === 'COMPLETED' ? 'success' : ''" />
        <div class="progress-info">
          <div class="progress-item">
            <span class="progress-label">当前操作:</span>
            <span class="progress-value">{{ syncProgress.currentOperation }}</span>
          </div>
          <div class="progress-item">
            <span class="progress-label">处理进度:</span>
            <span class="progress-value">{{ syncProgress.processedCount }}/{{ syncProgress.totalCount }}</span>
          </div>
          <div class="progress-item">
            <span class="progress-label">预计剩余时间:</span>
            <span class="progress-value">{{ syncProgress.estimatedTimeRemaining }}秒</span>
          </div>
          <div class="progress-item" v-if="syncProgress.errorMessage">
            <span class="progress-label">错误信息:</span>
            <span class="progress-value error-message">{{ syncProgress.errorMessage }}</span>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 同步结果 -->
    <el-card v-if="syncResult" class="result-card" shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">同步结果</span>
        </div>
      </template>
      <div class="result-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="同步状态">
            <el-tag :type="syncResult.status === 'success' ? 'success' : 'danger'">
              {{ syncResult.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="同步记录数">
            {{ syncResult.recordCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="同步耗时">
            {{ syncResult.duration || 0 }}ms
          </el-descriptions-item>
          <el-descriptions-item label="错误信息" v-if="syncResult.errorMessage">
            <span class="error-message">{{ syncResult.errorMessage }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
    
    <!-- 创建集合对话框 -->
    <el-dialog
      v-model="showCreateCollectionDialog"
      title="创建Milvus集合"
      width="500px"
    >
      <el-form :model="collectionForm" label-width="100px">
        <el-form-item label="集合名称" required>
          <el-input v-model="collectionForm.collectionName" placeholder="请输入集合名称" />
        </el-form-item>
        <el-form-item label="向量维度" required>
          <el-input-number v-model="collectionForm.dimension" :min="1" :max="4096" placeholder="请输入向量维度" />
        </el-form-item>
        <el-form-item label="距离度量" required>
          <el-select v-model="collectionForm.metricType" placeholder="请选择距离度量">
            <el-option label="欧氏距离 (L2)" value="L2" />
            <el-option label="余弦相似度 (COSINE)" value="COSINE" />
            <el-option label="内积 (IP)" value="IP" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateCollectionDialog = false">取消</el-button>
          <el-button type="primary" @click="createCollection" :loading="createCollectionLoading">
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { dataSourceApi, milvusApi } from '@/api'
import { ElMessage } from 'element-plus'

// 数据源列表
const dataSources = ref<any[]>([])
// Milvus集合列表
const collections = ref<any[]>([])
// 可用字段列表
const availableFields = ref<string[]>([])

// 同步表单
const syncForm = reactive({
  syncType: 'table',
  dataSourceId: null,
  databaseName: '',
  tableName: '',
  sql: '',
  collectionName: '',
  vectorFields: [] as string[],
  whereClause: ''
})

// 同步结果
const syncResult = ref<any>(null)
// 同步加载状态
const syncLoading = ref(false)
// 同步进度
const syncProgress = ref<any>(null)
// 进度轮询定时器
let progressPollingTimer: number | null = null

// 创建集合对话框
const showCreateCollectionDialog = ref(false)
// 创建集合表单
const collectionForm = reactive({
  collectionName: '',
  dimension: 128,
  metricType: 'L2'
})
// 创建集合加载状态
const createCollectionLoading = ref(false)

// 加载数据源列表
const loadDataSources = async () => {
  try {
    const response = await dataSourceApi.getList()
    if (response.data.code === 200) {
      dataSources.value = response.data.data
    }
  } catch (error) {
    console.error('加载数据源失败:', error)
  }
}

// 加载Milvus集合列表
const loadCollections = async () => {
  try {
    const response = await milvusApi.getCollections()
    if (response.data.code === 200) {
      collections.value = response.data.data
    }
  } catch (error) {
    console.error('加载集合列表失败:', error)
  }
}

// 处理同步类型变更
const handleSyncTypeChange = () => {
  // 重置表单
  syncForm.databaseName = ''
  syncForm.tableName = ''
  syncForm.sql = ''
  syncForm.whereClause = ''
  availableFields.value = []
}

// 处理数据源变更
const handleDataSourceChange = () => {
  // 这里可以根据数据源加载可用字段
  // 示例：假设从数据源中获取字段列表
  availableFields.value = ['id', 'name', 'description', 'content', 'title']
}

// 执行同步
const executeSync = async () => {
  // 表单验证
  if (!syncForm.dataSourceId) {
    ElMessage.error('请选择数据源')
    return
  }
  
  if (syncForm.syncType === 'table') {
    if (!syncForm.databaseName) {
      ElMessage.error('请输入数据库名称')
      return
    }
    if (!syncForm.tableName) {
      ElMessage.error('请输入表名称')
      return
    }
  } else {
    if (!syncForm.sql) {
      ElMessage.error('请输入SQL查询语句')
      return
    }
  }
  
  if (!syncForm.collectionName) {
    ElMessage.error('请选择目标集合')
    return
  }
  
  if (syncForm.vectorFields.length === 0) {
    ElMessage.error('请选择至少一个向量化字段')
    return
  }
  
  syncLoading.value = true
  syncResult.value = null
  
  try {
    let response
    if (syncForm.syncType === 'database') {
      // 数据库同步
      response = await milvusApi.syncDatabase({
        databaseConfig: {
          id: syncForm.dataSourceId
        },
        collectionName: syncForm.collectionName,
        sql: syncForm.sql,
        vectorFields: syncForm.vectorFields
      })
    } else {
      // 表同步
      response = await milvusApi.syncTable({
        databaseConfig: {
          id: syncForm.dataSourceId
        },
        collectionName: syncForm.collectionName,
        databaseName: syncForm.databaseName,
        tableName: syncForm.tableName,
        vectorFields: syncForm.vectorFields,
        whereClause: syncForm.whereClause
      })
    }
    
    if (response.data.code === 200) {
      syncResult.value = response.data.data
      ElMessage.success('同步任务已启动')
      // 开始轮询进度
      startProgressPolling(response.data.data.taskId)
    } else {
      const errorMessage = response.data.message || '同步失败'
      const errorCode = response.data.errorCode || ''
      const errorDetail = response.data.errorDetail || ''
      const suggestion = response.data.suggestion || ''
      
      syncResult.value = {
        status: 'failed',
        errorMessage: errorMessage
      }
      
      let message = errorMessage
      if (errorDetail) {
        message += `\n详细信息: ${errorDetail}`
      }
      if (suggestion) {
        message += `\n建议: ${suggestion}`
      }
      
      ElMessage.error(message)
    }
  } catch (error: any) {
    const errorMessage = error.message || '同步失败'
    const errorCode = error.errorCode || ''
    const errorDetail = error.errorDetail || ''
    const suggestion = error.suggestion || ''
    
    syncResult.value = {
      status: 'failed',
      errorMessage: errorMessage
    }
    
    let message = errorMessage
    if (errorDetail) {
      message += `\n详细信息: ${errorDetail}`
    }
    if (suggestion) {
      message += `\n建议: ${suggestion}`
    }
    
    ElMessage.error(message)
  } finally {
    syncLoading.value = false
  }
}

// 开始轮询进度
const startProgressPolling = (taskId: number) => {
  // 清除之前的定时器
  if (progressPollingTimer) {
    clearInterval(progressPollingTimer)
  }
  
  // 开始轮询
  progressPollingTimer = window.setInterval(async () => {
    try {
      const response = await milvusApi.getSyncProgress({ taskId })
      if (response.data.code === 200) {
        syncProgress.value = response.data.data
        
        // 如果任务完成，停止轮询
        if (syncProgress.value.status === 'COMPLETED' || syncProgress.value.status === 'FAILED') {
          if (progressPollingTimer) {
            clearInterval(progressPollingTimer)
            progressPollingTimer = null
          }
        }
      }
    } catch (error) {
      console.error('获取同步进度失败:', error)
    }
  }, 1000) // 每秒轮询一次
}

// 重置表单
const resetForm = () => {
  syncForm.syncType = 'table'
  syncForm.dataSourceId = null
  syncForm.databaseName = ''
  syncForm.tableName = ''
  syncForm.sql = ''
  syncForm.collectionName = ''
  syncForm.vectorFields = []
  syncForm.whereClause = ''
  syncResult.value = null
  syncProgress.value = null
  availableFields.value = []
  
  // 清除进度轮询定时器
  if (progressPollingTimer) {
    clearInterval(progressPollingTimer)
    progressPollingTimer = null
  }
}

// 创建集合
const createCollection = async () => {
  createCollectionLoading.value = true
  
  try {
    const response = await milvusApi.createCollection({
      collectionName: collectionForm.collectionName,
      dimension: collectionForm.dimension,
      metricType: collectionForm.metricType
    })
    
    if (response.data.code === 200) {
      // 重新加载集合列表
      await loadCollections()
      // 关闭对话框
      showCreateCollectionDialog.value = false
      // 重置表单
      collectionForm.collectionName = ''
      collectionForm.dimension = 128
      collectionForm.metricType = 'L2'
      // 提示成功
      ElMessage.success('集合创建成功')
    } else {
      ElMessage.error('集合创建失败: ' + response.data.message)
    }
  } catch (error: any) {
    ElMessage.error('集合创建失败: ' + (error.message || '未知错误'))
  } finally {
    createCollectionLoading.value = false
  }
}

// 初始化
onMounted(async () => {
  await loadDataSources()
  await loadCollections()
})
</script>

<style scoped>
.sync-container {
  padding: 20px;
}

.sync-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
}

.sync-form {
  margin-top: 20px;
}

.result-card {
  max-width: 800px;
  margin: 20px auto 0;
}

.result-content {
  margin-top: 20px;
}

.progress-content {
  margin-top: 20px;
}

.progress-info {
  margin-top: 15px;
}

.progress-item {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.progress-label {
  width: 120px;
  font-weight: 500;
  color: #606266;
}

.progress-value {
  flex: 1;
  color: #303133;
}

.error-message {
  color: #f56c6c;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>