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
      
      <el-divider content-position="left">任务编排画布</el-divider>
      
      <!-- 任务编排画布 -->
      <div class="flow-container">
        <!-- 左侧节点面板 -->
        <div class="node-panel">
          <h4>节点类型</h4>
          <div 
            v-for="nodeType in nodeTypes" 
            :key="nodeType.id"
            class="node-item"
            @mousedown="onNodeDragStart($event, nodeType)"
          >
            <el-icon>{{ nodeType.icon }}</el-icon>
            <span>{{ nodeType.label }}</span>
          </div>
        </div>
        
        <!-- 右侧画布区域 -->
        <div class="flow-canvas">
          <VueFlow
            v-model="nodes"
            v-model:connections="connections"
            @node-drag-start="onNodeDragStart"
            @node-drag-end="onNodeDragEnd"
            @connect="onConnect"
            @connection-remove="onConnectionRemove"
          >
            <template #node-custom="{ data }">
              <div class="custom-node" :class="data.type">
                <div class="node-header">
                  <el-icon>{{ getNodeIcon(data.type) }}</el-icon>
                  <span>{{ data.label }}</span>
                </div>
                <div class="node-content">
                  <div v-if="data.type === 'dataSource'" class="node-config">
                    <el-select v-model="data.dataSourceType" placeholder="选择数据源类型" style="width: 100%">
                      <el-option label="MySQL" value="MYSQL" />
                      <el-option label="PostgreSQL" value="POSTGRESQL" />
                      <el-option label="Oracle" value="ORACLE" />
                      <el-option label="SQL Server" value="SQL_SERVER" />
                      <el-option label="MongoDB" value="MONGODB" />
                      <el-option label="Redis" value="REDIS" />
                    </el-select>
                  </div>
                  <div v-else-if="data.type === 'syncType'" class="node-config">
                    <el-select v-model="data.syncType" placeholder="选择同步类型" style="width: 100%">
                      <el-option label="全量同步" value="FULL" />
                      <el-option label="增量同步" value="INCREMENTAL" />
                    </el-select>
                  </div>
                  <div v-else-if="data.type === 'dataProcess'" class="node-config">
                    <el-input v-model="data.processRule" placeholder="数据处理规则" style="width: 100%" />
                  </div>
                  <div v-else-if="data.type === 'milvusWrite'" class="node-config">
                    <el-input v-model="data.collectionName" placeholder="集合名称" style="width: 100%" />
                  </div>
                  <div v-else-if="data.type === 'vectorization'" class="node-config">
                    <el-form :model="data.vectorizationConfig" label-width="80px" size="small">
                      <el-form-item label="算法">
                        <el-select v-model="data.vectorizationConfig.algorithm" placeholder="选择算法" style="width: 100%">
                          <el-option label="FastText" value="FASTTEXT" />
                          <el-option label="OpenAI" value="OPENAI" />
                          <el-option label="BERT" value="BERT" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="向量维度">
                        <el-input-number v-model="data.vectorizationConfig.dimension" :min="1" :max="10000" style="width: 100%" />
                      </el-form-item>
                      <el-form-item label="预览">
                        <el-button size="small" @click="handlePreviewVectorization(data)" type="primary">生成预览</el-button>
                      </el-form-item>
                    </el-form>
                  </div>
                </div>
                <!-- 移除VueFlowHandle，使用VueFlow默认的连接点方式 -->
              </div>
            </template>
          </VueFlow>
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
  </div>

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
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi } from '@/api'
import { VueFlow, useVueFlow, addEdge } from '@vue-flow/core'
import { DataAnalysis, Upload, Plus, Minus, Link, Loading, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

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

const rules = reactive({
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ]
})

// 节点类型定义
const nodeTypes = [
  {
    id: 'dataSource',
    label: '数据源选择',
    icon: DataAnalysis
  },
  {
    id: 'syncType',
    label: '同步类型',
    icon: Setting
  },
  {
    id: 'dataProcess',
    label: '数据处理',
    icon: DataAnalysis
  },
  {
    id: 'vectorization',
    label: '向量化处理',
    icon: DataAnalysis
  },
  {
    id: 'milvusWrite',
    label: 'Milvus写入',
    icon: Upload
  }
]

// 节点和连接
const nodes = ref([
  {
    id: '1',
    type: 'dataSource',
    label: '数据源选择',
    position: { x: 100, y: 100 },
    handles: [
      { id: 'source-1', type: 'source', position: 'right' }
    ],
    dataSourceType: ''
  },
  {
    id: '2',
    type: 'syncType',
    label: '同步类型',
    position: { x: 300, y: 100 },
    handles: [
      { id: 'target-2', type: 'target', position: 'left' },
      { id: 'source-2', type: 'source', position: 'right' }
    ],
    syncType: 'FULL'
  },
  {
    id: '3',
    type: 'dataProcess',
    label: '数据处理',
    position: { x: 500, y: 100 },
    handles: [
      { id: 'target-3', type: 'target', position: 'left' },
      { id: 'source-3', type: 'source', position: 'right' }
    ],
    processRule: ''
  },
  {
    id: '4',
    type: 'vectorization',
    label: '向量化处理',
    position: { x: 500, y: 250 },
    handles: [
      { id: 'target-4', type: 'target', position: 'left' },
      { id: 'source-4', type: 'source', position: 'right' }
    ],
    vectorizationConfig: {
      algorithm: 'FASTTEXT',
      dimension: 300,
      fieldMappings: []
    }
  },
  {
    id: '5',
    type: 'milvusWrite',
    label: 'Milvus写入',
    position: { x: 700, y: 175 },
    handles: [
      { id: 'target-5', type: 'target', position: 'left' }
    ],
    collectionName: ''
  }
])

const connections = ref([
  {
    id: '1-2',
    source: '1',
    target: '2',
    sourceHandle: 'source-1',
    targetHandle: 'target-2'
  },
  {
    id: '2-3',
    source: '2',
    target: '3',
    sourceHandle: 'source-2',
    targetHandle: 'target-3'
  },
  {
    id: '2-4',
    source: '2',
    target: '4',
    sourceHandle: 'source-2',
    targetHandle: 'target-4'
  },
  {
    id: '3-5',
    source: '3',
    target: '5',
    sourceHandle: 'source-3',
    targetHandle: 'target-5'
  },
  {
    id: '4-5',
    source: '4',
    target: '5',
    sourceHandle: 'source-4',
    targetHandle: 'target-5'
  }
])

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

// 拖拽开始事件
const onNodeDragStart = (event: any, nodeType?: any) => {
  // 从左侧面板拖拽新节点
  console.log('Node drag start:', nodeType || event)
}

// 拖拽结束事件
const onNodeDragEnd = (event: any, node: any) => {
  console.log('Node dragged:', node.id, 'to:', node.position)
}

// 连接事件
const onConnect = (params: any) => {
  connections.value = addEdge({
    id: `${params.source}-${params.target}`,
    source: params.source,
    target: params.target,
    sourceHandle: params.sourceHandle,
    targetHandle: params.targetHandle
  }, connections.value) as any
}

// 连接删除事件
const onConnectionRemove = (connectionId: string) => {
  connections.value = connections.value.filter(c => c.id !== connectionId)
}

// 流程校验
const handleValidateFlow = () => {
  // 检查节点配置
  const invalidNodes = nodes.value.filter(node => {
    if (node.type === 'dataSource' && !node.dataSourceType) {
      return true
    }
    if (node.type === 'syncType' && !node.syncType) {
      return true
    }
    if (node.type === 'milvusWrite' && !node.collectionName) {
      return true
    }
    if (node.type === 'vectorization') {
      const config = node.vectorizationConfig
      if (!config || !config.algorithm || !config.dimension) {
        return true
      }
    }
    return false
  })
  
  if (invalidNodes.length > 0) {
    ElMessage.warning(`请完善以下节点的配置：${invalidNodes.map(n => n.label).join(', ')}`)
    return false
  }
  
  // 检查连接完整性
  if (connections.value.length < nodes.value.length - 1) {
    ElMessage.warning('流程连接不完整，请检查节点间的连线')
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
            nodes: nodes.value,
            connections: connections.value,
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
  
  // 重置节点和连接
  nodes.value = [
    {
      id: '1',
      type: 'dataSource',
      label: '数据源选择',
      position: { x: 100, y: 100 },
      handles: [
        { id: 'source-1', type: 'source', position: 'right' }
      ],
      dataSourceType: ''
    },
    {
      id: '2',
      type: 'syncType',
      label: '同步类型',
      position: { x: 300, y: 100 },
      handles: [
        { id: 'target-2', type: 'target', position: 'left' },
        { id: 'source-2', type: 'source', position: 'right' }
      ],
      syncType: 'FULL'
    },
    {
      id: '3',
      type: 'dataProcess',
      label: '数据处理',
      position: { x: 500, y: 100 },
      handles: [
        { id: 'target-3', type: 'target', position: 'left' },
        { id: 'source-3', type: 'source', position: 'right' }
      ],
      processRule: ''
    },
    {
      id: '4',
      type: 'vectorization',
      label: '向量化处理',
      position: { x: 500, y: 250 },
      handles: [
        { id: 'target-4', type: 'target', position: 'left' },
        { id: 'source-4', type: 'source', position: 'right' }
      ],
      vectorizationConfig: {
        algorithm: 'FASTTEXT',
        dimension: 300,
        fieldMappings: []
      }
    },
    {
      id: '5',
      type: 'milvusWrite',
      label: 'Milvus写入',
      position: { x: 700, y: 175 },
      handles: [
        { id: 'target-5', type: 'target', position: 'left' }
      ],
      collectionName: ''
    }
  ]
  
  connections.value = [
    {
      id: '1-2',
      source: '1',
      target: '2',
      sourceHandle: 'source-1',
      targetHandle: 'target-2'
    },
    {
      id: '2-3',
      source: '2',
      target: '3',
      sourceHandle: 'source-2',
      targetHandle: 'target-3'
    },
    {
      id: '2-4',
      source: '2',
      target: '4',
      sourceHandle: 'source-2',
      targetHandle: 'target-4'
    },
    {
      id: '3-5',
      source: '3',
      target: '5',
      sourceHandle: 'source-3',
      targetHandle: 'target-5'
    },
    {
      id: '4-5',
      source: '4',
      target: '5',
      sourceHandle: 'source-4',
      targetHandle: 'target-5'
    }
  ]
  
  // 重置依赖配置
  dependencyForm.dependencyTaskId = ''
  dependencyForm.dependencyCondition = ''
}

// 取消操作
const handleCancel = () => {
  router.push('/task/list')
}

// 生成向量化预览
const handlePreviewVectorization = (node: any) => {
  vectorizationPreviewVisible.value = true
  vectorizationPreviewData.value = null
  
  // 模拟向量化预览数据
  setTimeout(() => {
    vectorizationPreviewData.value = {
      algorithm: node.vectorizationConfig.algorithm,
      dimension: node.vectorizationConfig.dimension,
      processTime: Math.floor(Math.random() * 1000),
      sourceData: '这是一条测试数据，用于生成向量',
      vectorData: Array(node.vectorizationConfig.dimension).fill(0).map(() => Math.random() - 0.5)
    }
  }, 1000)
}
</script>

<style scoped>
.task-create {
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
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  background: #fff;
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
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
  border-right: 1px solid #e2e8f0;
  padding: 24px;
  overflow-y: auto;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.08);
  animation: slideInLeft 0.6s ease-out 0.4s both;
}

.node-panel h4 {
  margin-bottom: 24px;
  color: #1e293b;
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
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #cbd5e1;
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
  box-shadow: 0 12px 30px rgba(67, 97, 238, 0.3);
  transform: translateY(-4px);
  background: #fff;
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
  color: #3a0ca3;
}

.node-item span {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  transition: color 0.3s ease;
  z-index: 1;
}

.node-item:hover span {
  color: #3a0ca3;
}

.flow-canvas {
  flex: 1;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
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
    linear-gradient(rgba(67, 97, 238, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(67, 97, 238, 0.08) 1px, transparent 1px);
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
  border: 1px solid #cbd5e1;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
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
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 2px 8px rgba(67, 97, 238, 0.3));
}

.flow-canvas :deep(.vue-flow__connection-path:hover) {
  stroke: #3a0ca3;
  stroke-width: 4px;
  filter: drop-shadow(0 4px 12px rgba(58, 12, 163, 0.4));
}

.flow-canvas :deep(.vue-flow__connection-path) {
  marker-end: url(#arrowhead);
  animation: pulse 2s infinite;
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
  color: #1e293b;
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
  color: #3a0ca3;
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
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(12px);
  animation: slideUp 0.8s ease-out 0.8s both;
}

.action-buttons :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
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
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
}

.action-buttons :deep(.el-button:active) {
  transform: translateY(-2px);
}

.preview-content {
  max-height: 600px;
  overflow-y: auto;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 16px;
  padding: 24px;
  box-shadow: inset 0 4px 20px rgba(0, 0, 0, 0.08);
  animation: fadeIn 0.8s ease-out;
}

.vector-result {
  max-height: 350px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(12px);
  font-family: 'Courier New', Courier, monospace;
  border-left: 4px solid #4361ee;
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

.vector-result pre {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #1e293b;
  animation: fadeIn 1s ease-out 0.3s both;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #94a3b8;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 16px;
  animation: fadeIn 0.8s ease-out;
}

.loading :deep(el-icon) {
  margin-right: 16px;
  font-size: 32px;
  animation: spin 1.2s linear infinite;
  color: #4361ee;
}

.loading span {
  font-size: 16px;
  color: #64748b;
  font-weight: 500;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 表单样式 */
:deep(.el-form-item) {
  margin-bottom: 24px;
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  padding: 0 12px 0 0;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  border-color: #4361ee;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.2);
  border-color: #4361ee;
}

:deep(.el-input__inner),
:deep(.el-select__input),
:deep(.el-textarea__inner) {
  font-size: 14px;
  color: #1e293b;
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus),
:deep(.el-select__input:focus),
:deep(.el-textarea__inner:focus) {
  color: #3a0ca3;
}

/* 分割线样式 */
:deep(.el-divider) {
  margin: 40px 0;
  animation: fadeIn 0.8s ease-out;
}

:deep(.el-divider__text) {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  background: #fff;
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
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  border: none;
  background: #fff;
  animation: fadeInUp 0.8s ease-out;
}

:deep(.el-card__body) {
  padding: 32px;
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
  transition: all 0.3s ease;
}

:deep(.el-dialog__close:hover) {
  transform: scale(1.2);
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
}

:deep(.el-descriptions__cell) {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

:deep(.el-descriptions__cell:hover) {
  background: rgba(67, 97, 238, 0.05);
}

:deep(.el-descriptions__row:last-child :deep(.el-descriptions__cell)) {
  border-bottom: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-create {
    padding: 12px;
  }
  
  .flow-container {
    flex-direction: column;
    height: auto;
  }
  
  .node-panel {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }
  
  .flow-canvas {
    height: 400px;
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
}
</style>

<style>
/* 全局样式 */
.vue-flow__node-custom {
  width: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

/* 箭头标记 */
#arrowhead {
  fill: #409eff;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
  transition: all 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>