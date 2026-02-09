<template>
  <div class="task-create">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>创建任务</span>
        </div>
      </template>
      
      <!-- 基础信息表单 -->
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择任务类型">
            <el-option label="全量同步" value="FULL" />
            <el-option label="增量同步" value="INCREMENTAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      
      <el-divider content-position="left">任务流程配置</el-divider>
      
      <!-- 任务流程配置 -->
      <div class="flow-config">
        <div class="flow-steps">
          <!-- 步骤1：数据源选择 -->
          <div class="flow-step" :class="{ active: currentStep === 1 }" @click="currentStep = 1">
            <div class="step-header">
              <div class="step-number">1</div>
              <div class="step-info">
                <h4>数据源选择</h4>
                <p>配置数据源连接信息</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('dataSource')" /></el-icon>
            </div>
            <div class="step-content" v-if="currentStep === 1">
              <el-form :model="flowConfig.dataSource" label-width="120px">
                <el-form-item label="数据源类型">
                  <el-select v-model="flowConfig.dataSource.type" placeholder="选择数据源类型" style="width: 100%">
                    <el-option label="MySQL" value="MYSQL" />
                    <el-option label="PostgreSQL" value="POSTGRESQL" />
                    <el-option label="Oracle" value="ORACLE" />
                    <el-option label="SQL Server" value="SQL_SERVER" />
                    <el-option label="MongoDB" value="MONGODB" />
                    <el-option label="Redis" value="REDIS" />
                  </el-select>
                </el-form-item>
                <el-form-item label="数据源名称">
                  <el-input v-model="flowConfig.dataSource.name" placeholder="输入数据源名称" />
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 步骤2：同步类型 -->
          <div class="flow-step" :class="{ active: currentStep === 2 }" @click="currentStep = 2">
            <div class="step-header">
              <div class="step-number">2</div>
              <div class="step-info">
                <h4>同步类型</h4>
                <p>选择同步策略</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('syncType')" /></el-icon>
            </div>
            <div class="step-content" v-if="currentStep === 2">
              <el-form :model="flowConfig.syncType" label-width="120px">
                <el-form-item label="同步类型">
                  <el-select v-model="flowConfig.syncType.type" placeholder="选择同步类型" style="width: 100%">
                    <el-option label="全量同步" value="FULL" />
                    <el-option label="增量同步" value="INCREMENTAL" />
                  </el-select>
                </el-form-item>
                <el-form-item label="同步频率">
                  <el-select v-model="flowConfig.syncType.frequency" placeholder="选择同步频率" style="width: 100%">
                    <el-option label="实时" value="REALTIME" />
                    <el-option label="每小时" value="HOURLY" />
                    <el-option label="每天" value="DAILY" />
                    <el-option label="每周" value="WEEKLY" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 步骤3：数据处理 -->
          <div class="flow-step" :class="{ active: currentStep === 3 }" @click="currentStep = 3">
            <div class="step-header">
              <div class="step-number">3</div>
              <div class="step-info">
                <h4>数据处理</h4>
                <p>配置数据转换规则</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('dataProcess')" /></el-icon>
            </div>
            <div class="step-content" v-if="currentStep === 3">
              <el-form :model="flowConfig.dataProcess" label-width="120px">
                <el-form-item label="处理规则">
                  <el-input v-model="flowConfig.dataProcess.rule" type="textarea" placeholder="输入数据处理规则" :rows="4" />
                </el-form-item>
                <el-form-item label="字段映射">
                  <el-button type="primary" size="small">添加字段映射</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 步骤4：向量化处理 -->
          <div class="flow-step" :class="{ active: currentStep === 4 }" @click="currentStep = 4">
            <div class="step-header">
              <div class="step-number">4</div>
              <div class="step-info">
                <h4>向量化处理</h4>
                <p>配置向量生成参数</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('vectorization')" /></el-icon>
            </div>
            <div class="step-content" v-if="currentStep === 4">
              <el-form :model="flowConfig.vectorization" label-width="120px">
                <el-form-item label="算法">
                  <el-select v-model="flowConfig.vectorization.algorithm" placeholder="选择算法" style="width: 100%">
                    <el-option label="FastText" value="FASTTEXT" />
                    <el-option label="OpenAI" value="OPENAI" />
                    <el-option label="BERT" value="BERT" />
                  </el-select>
                </el-form-item>
                <el-form-item label="向量维度">
                  <el-input-number v-model="flowConfig.vectorization.dimension" :min="1" :max="10000" style="width: 100%" />
                </el-form-item>
                <el-form-item label="预览">
                  <el-button size="small" @click="handlePreviewVectorization()" type="primary">生成预览</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 步骤5：Milvus写入 -->
          <div class="flow-step" :class="{ active: currentStep === 5 }" @click="currentStep = 5">
            <div class="step-header">
              <div class="step-number">5</div>
              <div class="step-info">
                <h4>Milvus写入</h4>
                <p>配置向量存储参数</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('milvusWrite')" /></el-icon>
            </div>
            <div class="step-content" v-if="currentStep === 5">
              <el-form :model="flowConfig.milvusWrite" label-width="120px">
                <el-form-item label="集合名称">
                  <el-input v-model="flowConfig.milvusWrite.collectionName" placeholder="输入集合名称" />
                </el-form-item>
                <el-form-item label="索引类型">
                  <el-select v-model="flowConfig.milvusWrite.indexType" placeholder="选择索引类型" style="width: 100%">
                    <el-option label="IVF_FLAT" value="IVF_FLAT" />
                    <el-option label="HNSW" value="HNSW" />
                    <el-option label="ANNOY" value="ANNOY" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 依赖配置 -->
      <el-divider content-position="left">任务依赖配置</el-divider>
      <el-form :model="dependencyForm" label-width="120px">
        <el-form-item label="依赖任务">
          <el-select v-model="dependencyForm.dependencyTaskId" placeholder="选择依赖任务">
            <el-option label="任务A" value="1" />
            <el-option label="任务B" value="2" />
            <el-option label="任务C" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="依赖条件">
          <el-input v-model="dependencyForm.dependencyCondition" placeholder="如：同步成功率≥99.9%" />
        </el-form-item>
      </el-form>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" @click="handleSubmit">创建</el-button>
        <el-button type="success" @click="handleValidateFlow">流程校验</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </div>
    </el-card>
    
    <!-- 向量化预览对话框 -->
    <el-dialog
      v-model="vectorizationPreviewVisible"
      title="向量化结果预览"
      width="800px"
    >
      <div v-if="vectorizationPreviewData" class="preview-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="算法">{{ vectorizationPreviewData.algorithm }}</el-descriptions-item>
          <el-descriptions-item label="向量维度">{{ vectorizationPreviewData.dimension }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ vectorizationPreviewData.processTime }}ms</el-descriptions-item>
          <el-descriptions-item label="源数据">{{ vectorizationPreviewData.sourceData }}</el-descriptions-item>
          <el-descriptions-item label="向量结果">
            <div class="vector-result">
              <pre>{{ JSON.stringify(vectorizationPreviewData.vectorData, null, 2) }}</pre>
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
          <el-button @click="vectorizationPreviewVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi } from '@/api'
import { DataAnalysis, Upload, Plus, Minus, Link, Loading, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 页面加载时滚动到顶部
onMounted(() => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
})

const router = useRouter()
const formRef = ref<any>(null)

// 基础表单
const form = reactive({
  name: '',
  type: 'FULL',
  description: ''
})

// 依赖配置表单
const dependencyForm = reactive({
  dependencyTaskId: '',
  dependencyCondition: ''
})

// 向量化预览
const vectorizationPreviewVisible = ref(false)
const vectorizationPreviewData = ref<any>(null)

// 当前步骤
const currentStep = ref(1)

// 流程配置
const flowConfig = reactive({
  dataSource: {
    type: '',
    name: ''
  },
  syncType: {
    type: 'FULL',
    frequency: 'REALTIME'
  },
  dataProcess: {
    rule: ''
  },
  vectorization: {
    algorithm: 'FASTTEXT',
    dimension: 300
  },
  milvusWrite: {
    collectionName: '',
    indexType: 'IVF_FLAT'
  }
})

const rules = reactive({
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ]
})

// 节点图标映射
const getNodeIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    dataSource: DataAnalysis,
    syncType: Setting,
    dataProcess: DataAnalysis,
    vectorization: DataAnalysis,
    milvusWrite: Upload
  }
  return iconMap[type] || DataAnalysis
}

// 流程校验
const handleValidateFlow = () => {
  // 检查流程配置
  const invalidSteps = []
  
  if (!flowConfig.dataSource.type) {
    invalidSteps.push('数据源选择')
  }
  
  if (!flowConfig.syncType.type) {
    invalidSteps.push('同步类型')
  }
  
  if (!flowConfig.milvusWrite.collectionName) {
    invalidSteps.push('Milvus写入')
  }
  
  if (!flowConfig.vectorization.algorithm || !flowConfig.vectorization.dimension) {
    invalidSteps.push('向量化处理')
  }
  
  if (invalidSteps.length > 0) {
    ElMessage.warning(`请完善以下步骤的配置：${invalidSteps.join(', ')}`)
    return false
  }
  
  ElMessage.success('流程校验通过')
  return true
}

// 提交表单
const handleSubmit = async () => {
  if (formRef.value) {
    await formRef.value.validate(async (valid: boolean) => {
      if (valid) {
        // 先进行流程校验
        if (!handleValidateFlow()) {
          return
        }
        
        try {
          // 构建任务数据
          const taskData = {
            ...form,
            dataSourceId: 1, // 暂时设置为1作为默认值，实际应该从用户选择中获取
            flowConfig: flowConfig,
            dependency: dependencyForm
          }
          
          const result = await taskApi.create(taskData)
          console.log('Create result:', result)
          router.push('/task/list')
        } catch (error) {
          console.error('Failed to create task:', error)
        }
      }
    })
  }
}

// 重置表单
const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  // 重置流程配置
  Object.assign(flowConfig, {
    dataSource: {
      type: '',
      name: ''
    },
    syncType: {
      type: 'FULL',
      frequency: 'REALTIME'
    },
    dataProcess: {
      rule: ''
    },
    vectorization: {
      algorithm: 'FASTTEXT',
      dimension: 300
    },
    milvusWrite: {
      collectionName: '',
      indexType: 'IVF_FLAT'
    }
  })
  
  // 重置当前步骤
  currentStep.value = 1
  
  // 重置依赖配置
  dependencyForm.dependencyTaskId = ''
  dependencyForm.dependencyCondition = ''
}

// 取消操作
const handleCancel = () => {
  router.push('/task/list')
}

// 生成向量化预览
const handlePreviewVectorization = () => {
  vectorizationPreviewVisible.value = true
  vectorizationPreviewData.value = null
  
  // 模拟向量化预览数据
  setTimeout(() => {
    vectorizationPreviewData.value = {
      algorithm: flowConfig.vectorization.algorithm,
      dimension: flowConfig.vectorization.dimension,
      processTime: Math.floor(Math.random() * 1000),
      sourceData: '这是一条测试数据，用于生成向量',
      vectorData: Array(flowConfig.vectorization.dimension).fill(0).map(() => Math.random() - 0.5)
    }
  }, 1000)
}
</script>

<style scoped>
.task-create {
  padding: 20px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
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
  padding: 10px 0;
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

.flow-container {
  display: flex;
  margin: 30px 0;
  height: 600px;
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  background: rgba(15, 23, 42, 0.8);
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

.node-panel {
  width: 240px;
  background: rgba(15, 23, 42, 0.9);
  border-right: 1px solid rgba(59, 130, 246, 0.2);
  padding: 24px;
  overflow-y: auto;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.2);
  animation: slideInLeft 0.6s ease-out 0.4s both;
}

.node-panel h4 {
  margin-bottom: 24px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 18px;
  font-weight: 700;
  position: relative;
  padding-bottom: 12px;
}

.node-panel h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 4px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border-radius: 2px;
  animation: expandWidth 0.8s ease-out 0.6s forwards;
  transform: scaleX(0);
  transform-origin: left;
}

.node-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  margin-bottom: 16px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 12px;
  cursor: grab;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(12px);
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out var(--delay) both;
}

.node-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #4361ee, #3a0ca3);
  transform: scaleY(0);
  transition: transform 0.3s ease;
  transform-origin: bottom;
}

.node-item:hover::before {
  transform: scaleY(1);
}

.node-item:hover {
  border-color: #4361ee;
  box-shadow: 0 12px 30px rgba(59, 130, 246, 0.3);
  transform: translateY(-4px);
  background: rgba(15, 23, 42, 0.8);
}

.node-item:active {
  cursor: grabbing;
  transform: translateY(-2px);
}

.node-item :deep(el-icon) {
  margin-right: 12px;
  color: #4361ee;
  font-size: 20px;
  transition: all 0.4s ease;
  z-index: 1;
}

.node-item:hover :deep(el-icon) {
  transform: scale(1.2) rotate(5deg);
  color: #a855f7;
}

.node-item span {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  transition: color 0.3s ease;
  z-index: 1;
}

.node-item:hover span {
  color: #ffffff;
}

.flow-canvas {
  flex: 1;
  background: rgba(15, 23, 42, 0.6);
  position: relative;
  overflow: hidden;
  animation: fadeIn 1s ease-out 0.6s both;
}

.flow-canvas::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(59, 130, 246, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.1) 1px, transparent 1px);
  background-size: 25px 25px;
  pointer-events: none;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 25px 25px;
  }
}

.flow-canvas :deep(.vue-flow) {
  width: 100%;
  height: 100%;
}

.flow-canvas :deep(.vue-flow__node) {
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.9);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(12px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.flow-canvas :deep(.vue-flow__node)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  transform-origin: left;
}

.flow-canvas :deep(.vue-flow__node:hover::before) {
  transform: scaleX(1);
}

.flow-canvas :deep(.vue-flow__node:hover) {
  box-shadow: 0 15px 40px rgba(67, 97, 238, 0.3);
  transform: translateY(-4px) scale(1.02);
}

.flow-canvas :deep(.vue-flow__handle) {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  border: 2px solid #fff;
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.5);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.flow-canvas :deep(.vue-flow__handle::before) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.3s ease;
}

.flow-canvas :deep(.vue-flow__handle:hover::before) {
  transform: translate(-50%, -50%) scale(1.5);
}

.flow-canvas :deep(.vue-flow__handle:hover) {
  transform: scale(1.4);
  box-shadow: 0 6px 20px rgba(67, 97, 238, 0.7);
}

.flow-canvas :deep(.vue-flow__connection) {
  stroke: #4361ee;
  stroke-width: 3px;
  stroke-dasharray: 0;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 8px rgba(67, 97, 238, 0.3));
}

.flow-canvas :deep(.vue-flow__connection-path:hover) {
  stroke: #3a0ca3;
  stroke-width: 4px;
  filter: drop-shadow(0 4px 12px rgba(58, 12, 163, 0.4));
}

.flow-canvas :deep(.vue-flow__connection-path) {
  marker-end: url(#arrowhead);
}

.flow-canvas :deep(.vue-flow__connection-path.success) {
  stroke: #10b981;
  filter: drop-shadow(0 2px 8px rgba(16, 185, 129, 0.3));
}

.flow-canvas :deep(.vue-flow__connection-path.error) {
  stroke: #ef4444;
  filter: drop-shadow(0 2px 8px rgba(239, 68, 68, 0.3));
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
}

.custom-node {
  width: 220px;
  padding: 20px;
  animation: slideInUp 0.6s ease-out;
}

.custom-node .node-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  position: relative;
  padding-bottom: 8px;
}

.custom-node .node-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border-radius: 1px;
}

.custom-node .node-header :deep(el-icon) {
  margin-right: 12px;
  color: #4361ee;
  font-size: 18px;
  transition: all 0.3s ease;
}

.custom-node:hover .node-header :deep(el-icon) {
  transform: scale(1.1) rotate(5deg);
  color: #a855f7;
}

.custom-node .node-content {
  margin-top: 16px;
}

.custom-node .node-config {
  margin-bottom: 16px;
}

.custom-node .node-config :deep(.el-input),
.custom-node .node-config :deep(.el-select),
.custom-node .node-config :deep(.el-input-number) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.custom-node .node-config :deep(.el-input__wrapper),
.custom-node .node-config :deep(.el-select__wrapper),
.custom-node .node-config :deep(.el-input-number) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.custom-node .node-config :deep(.el-input__wrapper:hover),
.custom-node .node-config :deep(.el-select__wrapper:hover),
.custom-node .node-config :deep(.el-input-number:hover) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  border-color: #4361ee;
}

.custom-node .node-config :deep(.el-button) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.custom-node .node-config :deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.custom-node .node-config :deep(.el-button:hover::before) {
  left: 100%;
}

.custom-node .node-config :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(67, 97, 238, 0.4);
}

.action-buttons {
  margin-top: 40px;
  display: flex;
  gap: 16px;
  justify-content: flex-start;
  padding: 24px;
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(12px);
  animation: slideUp 0.8s ease-out 0.8s both;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.action-buttons :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.6);
  color: rgba(255, 255, 255, 0.9);
}

.action-buttons :deep(.el-button::before) {
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

.action-buttons :deep(.el-button:active::before) {
  width: 300px;
  height: 300px;
}

.action-buttons :deep(.el-button--primary) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  color: #fff;
}

.action-buttons :deep(.el-button--success) {
  background: linear-gradient(90deg, #2ecc71, #27ae60);
  border: none;
  color: #fff;
}

.action-buttons :deep(.el-button--info) {
  background: linear-gradient(90deg, #3498db, #2980b9);
  border: none;
  color: #fff;
}

.action-buttons :deep(.el-button:hover) {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(59, 130, 246, 0.3);
  border-color: #4361ee;
  background: rgba(15, 23, 42, 0.8);
  color: #ffffff;
}

.action-buttons :deep(.el-button:active) {
  transform: translateY(-2px);
}

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
  border-left: 4px solid #4361ee;
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
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.loading :deep(el-icon) {
  margin-right: 16px;
  font-size: 32px;
  animation: spin 1.2s linear infinite;
  color: #4361ee;
}

.loading span {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 流程配置样式 */
.flow-config {
  margin: 30px 0;
  animation: fadeIn 0.8s ease-out 0.2s both;
}

.flow-steps {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.flow-step {
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.6s ease-out var(--delay) both;
}

.flow-step:hover {
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.3);
  transform: translateY(-2px);
  border-color: rgba(59, 130, 246, 0.4);
}

.flow-step.active {
  border-color: #4361ee;
  box-shadow: 0 15px 40px rgba(67, 97, 238, 0.4);
  background: rgba(15, 23, 42, 0.9);
}

.flow-step.active .step-header {
  background: linear-gradient(90deg, rgba(67, 97, 238, 0.2), rgba(58, 12, 163, 0.2));
}

.step-header {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.step-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.3), transparent);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  transform-origin: left;
}

.flow-step:hover .step-header::after {
  transform: scaleX(1);
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  color: white;
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.4);
}

.flow-step:hover .step-number {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 6px 20px rgba(67, 97, 238, 0.6);
}

.flow-step.active .step-number {
  background: linear-gradient(135deg, #10b981, #059669);
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.6);
}

.step-info {
  flex: 1;
  transition: all 0.3s ease;
}

.step-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  transition: color 0.3s ease;
}

.step-info p {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  transition: color 0.3s ease;
}

.flow-step:hover .step-info h4 {
  color: #ffffff;
}

.flow-step:hover .step-info p {
  color: rgba(255, 255, 255, 0.8);
}

.step-icon {
  font-size: 20px;
  color: #4361ee;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.flow-step:hover .step-icon {
  transform: scale(1.2) rotate(5deg);
  color: #a855f7;
}

.flow-step.active .step-icon {
  color: #10b981;
  transform: scale(1.1);
}

.step-content {
  padding: 24px;
  border-top: 1px solid rgba(59, 130, 246, 0.1);
  animation: slideDown 0.4s ease-out;
  background: rgba(15, 23, 42, 0.6);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 表单样式 */
:deep(.el-form-item) {
  margin-bottom: 24px;
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  padding: 0 12px 0 0;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.6);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #4361ee;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.3);
  border-color: #4361ee;
}

:deep(.el-input__inner),
:deep(.el-select__input),
:deep(.el-textarea__inner) {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus),
:deep(.el-select__input:focus),
:deep(.el-textarea__inner:focus) {
  color: #ffffff;
}

/* 分割线样式 */
:deep(.el-divider) {
  margin: 40px 0;
  animation: fadeIn 0.8s ease-out;
}

:deep(.el-divider__text) {
  font-size: 18px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(15, 23, 42, 0.8);
  padding: 0 24px;
  position: relative;
}

:deep(.el-divider__text)::before,
:deep(.el-divider__text)::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #4361ee);
  transform: translateY(-50%);
}

:deep(.el-divider__text)::before {
  left: -70px;
}

:deep(.el-divider__text)::after {
  right: -70px;
  background: linear-gradient(90deg, #4361ee, transparent);
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.2);
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  animation: fadeInUp 0.8s ease-out;
}

:deep(.el-card__body) {
  padding: 32px;
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
  transition: all 0.3s ease;
}

:deep(.el-dialog__close:hover) {
  transform: scale(1.2);
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
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-dialog__footer :deep(.el-button:hover)) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* 描述列表样式 */
:deep(.el-descriptions) {
  background: rgba(15, 23, 42, 0.9);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(12px);
  animation: fadeInUp 0.6s ease-out;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(59, 130, 246, 0.1);
  border-right: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-descriptions__cell) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-descriptions__cell:hover) {
  background: rgba(59, 130, 246, 0.1);
}

:deep(.el-descriptions__row:last-child :deep(.el-descriptions__cell)) {
  border-bottom: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-create {
    padding: 12px;
  }
  
  .flow-steps {
    gap: 12px;
  }
  
  .step-header {
    padding: 16px 20px;
  }
  
  .step-content {
    padding: 20px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 12px;
  }
  
  .action-buttons :deep(.el-button) {
    width: 100%;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .step-number {
    width: 32px;
    height: 32px;
    font-size: 14px;
    margin-right: 16px;
  }
  
  .step-info h4 {
    font-size: 14px;
  }
  
  .step-info p {
    font-size: 12px;
  }
}

/* 动画定义 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

<style>
/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.5);
  border-radius: 4px;
  transition: all 0.3s ease;
  border: 2px solid rgba(15, 23, 42, 0.6);
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.8);
  transform: scale(1.1);
}
</style>