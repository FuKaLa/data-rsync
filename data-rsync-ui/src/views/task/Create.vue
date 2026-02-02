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
            <template #node-custom="{ node }">
              <div class="custom-node" :class="node.type">
                <div class="node-header">
                  <el-icon>{{ getNodeIcon(node.type) }}</el-icon>
                  <span>{{ node.label }}</span>
                </div>
                <div class="node-content">
                  <div v-if="node.type === 'dataSource'" class="node-config">
                    <el-select v-model="node.dataSourceType" placeholder="选择数据源类型" style="width: 100%">
                      <el-option label="MySQL" value="MYSQL" />
                      <el-option label="PostgreSQL" value="POSTGRESQL" />
                      <el-option label="Oracle" value="ORACLE" />
                      <el-option label="SQL Server" value="SQL_SERVER" />
                      <el-option label="MongoDB" value="MONGODB" />
                      <el-option label="Redis" value="REDIS" />
                    </el-select>
                  </div>
                  <div v-else-if="node.type === 'syncType'" class="node-config">
                    <el-select v-model="node.syncType" placeholder="选择同步类型" style="width: 100%">
                      <el-option label="全量同步" value="FULL" />
                      <el-option label="增量同步" value="INCREMENTAL" />
                    </el-select>
                  </div>
                  <div v-else-if="node.type === 'dataProcess'" class="node-config">
                    <el-input v-model="node.processRule" placeholder="数据处理规则" style="width: 100%" />
                  </div>
                  <div v-else-if="node.type === 'milvusWrite'" class="node-config">
                    <el-input v-model="node.collectionName" placeholder="集合名称" style="width: 100%" />
                  </div>
                  <div v-else-if="node.type === 'vectorization'" class="node-config">
                    <el-form :model="node.vectorizationConfig" label-width="80px" size="small">
                      <el-form-item label="算法">
                        <el-select v-model="node.vectorizationConfig.algorithm" placeholder="选择算法" style="width: 100%">
                          <el-option label="FastText" value="FASTTEXT" />
                          <el-option label="OpenAI" value="OPENAI" />
                          <el-option label="BERT" value="BERT" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="向量维度">
                        <el-input-number v-model="node.vectorizationConfig.dimension" :min="1" :max="10000" style="width: 100%" />
                      </el-form-item>
                      <el-form-item label="预览">
                        <el-button size="small" @click="handlePreviewVectorization(node)" type="primary">生成预览</el-button>
                      </el-form-item>
                    </el-form>
                  </div>
                </div>
                <VueFlowHandle
                  v-for="(handle, index) in node.handles"
                  :key="index"
                  :type="handle.type"
                  :position="handle.position"
                  :id="handle.id"
                />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi } from '@/api'
import VueFlow, { VueFlowHandle, useVueFlow, addEdge } from '@vue-flow/core'
import { Database, Settings, DataAnalysis, Upload, Plus, Minus, Link, Loading } from '@element-plus/icons-vue'
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
    icon: Database
  },
  {
    id: 'syncType',
    label: '同步类型',
    icon: Settings
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
    position: { x: 500, y: 200 },
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
    position: { x: 700, y: 100 },
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
    dataSource: Database,
    syncType: Settings,
    dataProcess: DataAnalysis,
    vectorization: DataAnalysis,
    milvusWrite: Upload
  }
  return iconMap[type] || Database
}

// 拖拽开始事件
const onNodeDragStart = (event: MouseEvent, nodeType?: any) => {
  if (nodeType) {
    // 从左侧面板拖拽新节点
    event.dataTransfer.setData('application/json', JSON.stringify(nodeType))
  }
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
  }, connections.value)
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
      type: 'milvusWrite',
      label: 'Milvus写入',
      position: { x: 700, y: 100 },
      handles: [
        { id: 'target-4', type: 'target', position: 'left' }
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
      id: '3-4',
      source: '3',
      target: '4',
      sourceHandle: 'source-3',
      targetHandle: 'target-4'
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
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flow-container {
  display: flex;
  margin: 20px 0;
  height: 500px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.node-panel {
  width: 200px;
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  padding: 16px;
  overflow-y: auto;
}

.node-panel h4 {
  margin-bottom: 16px;
  color: #303133;
}

.node-item {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 8px;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: grab;
  transition: all 0.3s;
}

.node-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.node-item:active {
  cursor: grabbing;
}

.node-item el-icon {
  margin-right: 8px;
  color: #409eff;
}

.flow-canvas {
  flex: 1;
  background-color: #fff;
  position: relative;
}

.flow-canvas :deep(.vue-flow) {
  width: 100%;
  height: 100%;
}

.flow-canvas :deep(.vue-flow__node) {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.flow-canvas :deep(.vue-flow__handle) {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #409eff;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #409eff;
}

.flow-canvas :deep(.vue-flow__handle:hover) {
  background-color: #66b1ff;
}

.flow-canvas :deep(.vue-flow__connection) {
  stroke: #409eff;
  stroke-width: 2px;
}

.flow-canvas :deep(.vue-flow__connection-path:hover) {
  stroke: #66b1ff;
}

.custom-node {
  width: 180px;
  padding: 12px;
}

.custom-node .node-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-weight: 500;
  color: #303133;
}

.custom-node .node-header el-icon {
  margin-right: 8px;
  color: #409eff;
}

.custom-node .node-content {
  margin-top: 8px;
}

.custom-node .node-config {
  margin-bottom: 8px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.preview-content {
  max-height: 600px;
  overflow-y: auto;
}

.vector-result {
  max-height: 300px;
  overflow-y: auto;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.vector-result pre {
  margin: 0;
  font-size: 12px;
  line-height: 1.4;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
}

.loading el-icon {
  margin-right: 8px;
}
</style>

<style>
/* 全局样式 */
.vue-flow__node-custom {
  width: 180px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>