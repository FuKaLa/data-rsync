<template>
  <div class="task-detail">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>任务详情</span>
          <el-button type="primary" @click="handleBack">返回列表</el-button>
        </div>
      </template>
      
      <!-- 任务基本信息 -->
      <div class="task-info">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="任务名称">{{ task.name }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag :type="getTaskType(task.type)">{{ getTaskTypeLabel(task.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(task.status)">{{ task.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="数据源">
            {{ task.dataSourceName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="数据库">{{ task.databaseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="表名">{{ task.tableName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ task.createTime }}</el-descriptions-item>
          <el-descriptions-item label="最后执行时间">{{ task.lastExecTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下次执行时间">{{ task.nextExecTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 标签页 -->
      <el-tabs v-model="activeTab" class="task-tabs">
        <!-- 任务节点管理 -->
        <el-tab-pane label="任务节点" name="nodes">
          <div class="tab-content">
            <el-button type="primary" @click="handleAddNode" style="margin-bottom: 20px">添加节点</el-button>
            <el-table :data="nodes" style="width: 100%">
              <el-table-column prop="name" label="节点名称" />
              <el-table-column prop="type" label="节点类型" />
              <el-table-column prop="status" label="状态" />
              <el-table-column prop="createTime" label="创建时间" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" @click="handleEditNode(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteNode(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- 任务连接管理 -->
        <el-tab-pane label="任务连接" name="connections">
          <div class="tab-content">
            <el-button type="primary" @click="handleAddConnection" style="margin-bottom: 20px">添加连接</el-button>
            <el-table :data="connections" style="width: 100%">
              <el-table-column prop="sourceNodeId" label="源节点" />
              <el-table-column prop="targetNodeId" label="目标节点" />
              <el-table-column prop="type" label="连接类型" />
              <el-table-column prop="createTime" label="创建时间" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" @click="handleEditConnection(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteConnection(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- 任务依赖管理 -->
        <el-tab-pane label="任务依赖" name="dependencies">
          <div class="tab-content">
            <el-button type="primary" @click="handleAddDependency" style="margin-bottom: 20px">添加依赖</el-button>
            <el-table :data="dependencies" style="width: 100%">
              <el-table-column prop="dependencyTaskId" label="依赖任务" />
              <el-table-column prop="dependencyType" label="依赖类型" />
              <el-table-column prop="createTime" label="创建时间" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" @click="handleEditDependency(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteDependency(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- 向量化配置管理 -->
        <el-tab-pane label="向量化配置" name="vectorization">
          <div class="tab-content">
            <el-button type="primary" @click="handleAddVectorizationConfig" style="margin-bottom: 20px">添加配置</el-button>
            <el-table :data="vectorizationConfigs" style="width: 100%">
              <el-table-column prop="algorithm" label="算法" />
              <el-table-column prop="dimension" label="维度" />
              <el-table-column prop="modelName" label="模型名称" />
              <el-table-column prop="createTime" label="创建时间" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" @click="handleEditVectorizationConfig(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteVectorizationConfig(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- Milvus索引管理 -->
        <el-tab-pane label="Milvus索引" name="milvusIndex">
          <div class="tab-content">
            <el-button type="primary" @click="handleAddMilvusIndex" style="margin-bottom: 20px">添加索引</el-button>
            <el-table :data="milvusIndexes" style="width: 100%">
              <el-table-column prop="collectionName" label="集合名称" />
              <el-table-column prop="indexName" label="索引名称" />
              <el-table-column prop="indexType" label="索引类型" />
              <el-table-column prop="status" label="状态" />
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" @click="handleEditMilvusIndex(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteMilvusIndex(scope.row.id)">删除</el-button>
                  <el-button size="small" @click="handleRebuildMilvusIndex(scope.row)">重建</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 节点编辑对话框 -->
    <el-dialog
      v-model="nodeDialogVisible"
      :title="nodeDialogTitle"
      width="600px"
    >
      <el-form :model="nodeForm" label-width="100px">
        <el-form-item label="节点名称">
          <el-input v-model="nodeForm.name" placeholder="请输入节点名称" />
        </el-form-item>
        <el-form-item label="节点类型">
          <el-select v-model="nodeForm.type" placeholder="请选择节点类型">
            <el-option label="数据源" value="DATA_SOURCE" />
            <el-option label="转换" value="TRANSFORM" />
            <el-option label="目标" value="TARGET" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置">
          <el-input type="textarea" v-model="nodeForm.config" placeholder="请输入节点配置" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="nodeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmNode">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 连接编辑对话框 -->
    <el-dialog
      v-model="connectionDialogVisible"
      :title="connectionDialogTitle"
      width="600px"
    >
      <el-form :model="connectionForm" label-width="100px">
        <el-form-item label="源节点">
          <el-select v-model="connectionForm.sourceNodeId" placeholder="请选择源节点">
            <el-option v-for="node in nodes" :key="node.id" :label="node.name" :value="node.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标节点">
          <el-select v-model="connectionForm.targetNodeId" placeholder="请选择目标节点">
            <el-option v-for="node in nodes" :key="node.id" :label="node.name" :value="node.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接类型">
          <el-select v-model="connectionForm.type" placeholder="请选择连接类型">
            <el-option label="数据流" value="DATA_FLOW" />
            <el-option label="控制流" value="CONTROL_FLOW" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="connectionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmConnection">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 依赖编辑对话框 -->
    <el-dialog
      v-model="dependencyDialogVisible"
      :title="dependencyDialogTitle"
      width="600px"
    >
      <el-form :model="dependencyForm" label-width="100px">
        <el-form-item label="依赖任务">
          <el-select v-model="dependencyForm.dependencyTaskId" placeholder="请选择依赖任务">
            <el-option v-for="t in allTasks" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="依赖类型">
          <el-select v-model="dependencyForm.dependencyType" placeholder="请选择依赖类型">
            <el-option label="前置依赖" value="PRECEDENCE" />
            <el-option label="资源依赖" value="RESOURCE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dependencyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDependency">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 向量化配置编辑对话框 -->
    <el-dialog
      v-model="vectorizationDialogVisible"
      :title="vectorizationDialogTitle"
      width="600px"
    >
      <el-form :model="vectorizationForm" label-width="100px">
        <el-form-item label="算法">
          <el-input v-model="vectorizationForm.algorithm" placeholder="请输入算法名称" />
        </el-form-item>
        <el-form-item label="维度">
          <el-input-number v-model="vectorizationForm.dimension" :min="1" placeholder="请输入维度" />
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="vectorizationForm.modelName" placeholder="请输入模型名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="vectorizationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmVectorizationConfig">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- Milvus索引编辑对话框 -->
    <el-dialog
      v-model="milvusIndexDialogVisible"
      :title="milvusIndexDialogTitle"
      width="600px"
    >
      <el-form :model="milvusIndexForm" label-width="100px">
        <el-form-item label="集合名称">
          <el-input v-model="milvusIndexForm.collectionName" placeholder="请输入集合名称" />
        </el-form-item>
        <el-form-item label="索引名称">
          <el-input v-model="milvusIndexForm.indexName" placeholder="请输入索引名称" />
        </el-form-item>
        <el-form-item label="索引类型">
          <el-select v-model="milvusIndexForm.indexType" placeholder="请选择索引类型">
            <el-option label="IVF_FLAT" value="IVF_FLAT" />
            <el-option label="IVF_SQ8" value="IVF_SQ8" />
            <el-option label="IVF_PQ" value="IVF_PQ" />
            <el-option label="HNSW" value="HNSW" />
          </el-select>
        </el-form-item>
        <el-form-item label="字段名称">
          <el-input v-model="milvusIndexForm.fieldName" placeholder="请输入字段名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="milvusIndexDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmMilvusIndex">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { taskApi } from '../../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const taskId = computed(() => Number(route.params.id))

// 任务信息
const task = ref({
  id: 0,
  name: '',
  type: '',
  status: '',
  dataSourceName: '',
  databaseName: '',
  tableName: '',
  createTime: '',
  lastExecTime: '',
  nextExecTime: ''
})

// 标签页
const activeTab = ref('nodes')

// 节点列表
const nodes = ref<any[]>([])
// 连接列表
const connections = ref<any[]>([])
// 依赖列表
const dependencies = ref<any[]>([])
// 向量化配置列表
const vectorizationConfigs = ref<any[]>([])
// Milvus索引列表
const milvusIndexes = ref<any[]>([])
// 所有任务列表（用于依赖选择）
const allTasks = ref<any[]>([])

// 节点对话框
const nodeDialogVisible = ref(false)
const nodeDialogTitle = ref('添加节点')
const nodeForm = ref({
  id: 0,
  name: '',
  type: '',
  config: ''
})

// 连接对话框
const connectionDialogVisible = ref(false)
const connectionDialogTitle = ref('添加连接')
const connectionForm = ref({
  id: 0,
  sourceNodeId: '',
  targetNodeId: '',
  type: ''
})

// 依赖对话框
const dependencyDialogVisible = ref(false)
const dependencyDialogTitle = ref('添加依赖')
const dependencyForm = ref({
  id: 0,
  dependencyTaskId: '',
  dependencyType: ''
})

// 向量化配置对话框
const vectorizationDialogVisible = ref(false)
const vectorizationDialogTitle = ref('添加向量化配置')
const vectorizationForm = ref({
  id: 0,
  algorithm: '',
  dimension: 0,
  modelName: ''
})

// Milvus索引对话框
const milvusIndexDialogVisible = ref(false)
const milvusIndexDialogTitle = ref('添加Milvus索引')
const milvusIndexForm = ref({
  id: 0,
  collectionName: '',
  indexName: '',
  indexType: '',
  fieldName: ''
})

onMounted(() => {
  loadTaskDetail()
  loadNodes()
  loadConnections()
  loadDependencies()
  loadVectorizationConfigs()
  loadMilvusIndexes()
  loadAllTasks()
})

// 加载任务详情
const loadTaskDetail = async () => {
  try {
    const response = await taskApi.getDetail(taskId.value)
    task.value = response.data || {}
  } catch (error) {
    console.error('Failed to load task detail:', error)
    ElMessage.error('加载任务详情失败')
  }
}

// 加载节点列表
const loadNodes = async () => {
  try {
    // 这里应该调用实际的API获取节点列表
    // 暂时使用模拟数据
    nodes.value = [
      { id: 1, name: '数据源节点', type: 'DATA_SOURCE', status: 'ACTIVE', createTime: '2024-01-01 10:00:00' },
      { id: 2, name: '转换节点', type: 'TRANSFORM', status: 'ACTIVE', createTime: '2024-01-01 10:01:00' },
      { id: 3, name: '目标节点', type: 'TARGET', status: 'ACTIVE', createTime: '2024-01-01 10:02:00' }
    ]
  } catch (error) {
    console.error('Failed to load nodes:', error)
    ElMessage.error('加载节点列表失败')
  }
}

// 加载连接列表
const loadConnections = async () => {
  try {
    // 这里应该调用实际的API获取连接列表
    // 暂时使用模拟数据
    connections.value = [
      { id: 1, sourceNodeId: 1, targetNodeId: 2, type: 'DATA_FLOW', createTime: '2024-01-01 10:03:00' },
      { id: 2, sourceNodeId: 2, targetNodeId: 3, type: 'DATA_FLOW', createTime: '2024-01-01 10:04:00' }
    ]
  } catch (error) {
    console.error('Failed to load connections:', error)
    ElMessage.error('加载连接列表失败')
  }
}

// 加载依赖列表
const loadDependencies = async () => {
  try {
    // 这里应该调用实际的API获取依赖列表
    // 暂时使用模拟数据
    dependencies.value = [
      { id: 1, dependencyTaskId: 2, dependencyType: 'PRECEDENCE', createTime: '2024-01-01 10:05:00' }
    ]
  } catch (error) {
    console.error('Failed to load dependencies:', error)
    ElMessage.error('加载依赖列表失败')
  }
}

// 加载向量化配置列表
const loadVectorizationConfigs = async () => {
  try {
    // 这里应该调用实际的API获取向量化配置列表
    // 暂时使用模拟数据
    vectorizationConfigs.value = [
      { id: 1, algorithm: 'BERT', dimension: 768, modelName: 'bert-base-chinese', createTime: '2024-01-01 10:06:00' }
    ]
  } catch (error) {
    console.error('Failed to load vectorization configs:', error)
    ElMessage.error('加载向量化配置列表失败')
  }
}

// 加载Milvus索引列表
const loadMilvusIndexes = async () => {
  try {
    // 这里应该调用实际的API获取Milvus索引列表
    // 暂时使用模拟数据
    milvusIndexes.value = [
      { id: 1, collectionName: 'test_collection', indexName: 'test_index', indexType: 'IVF_FLAT', status: 'READY', createTime: '2024-01-01 10:07:00' }
    ]
  } catch (error) {
    console.error('Failed to load Milvus indexes:', error)
    ElMessage.error('加载Milvus索引列表失败')
  }
}

// 加载所有任务列表
const loadAllTasks = async () => {
  try {
    const response = await taskApi.getList()
    allTasks.value = response.data || []
  } catch (error) {
    console.error('Failed to load all tasks:', error)
    ElMessage.error('加载任务列表失败')
  }
}

// 返回列表
const handleBack = () => {
  router.push('/task/list')
}

// 状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'RUNNING':
      return 'success'
    case 'PAUSED':
      return 'warning'
    case 'FAILED':
      return 'danger'
    case 'COMPLETED':
      return 'info'
    case 'ROLLED_BACK':
      return 'info'
    default:
      return ''
  }
}

// 任务类型
const getTaskType = (type: string) => {
  switch (type) {
    case 'FULL_SYNC':
      return 'primary'
    case 'INCREMENTAL_SYNC':
      return 'success'
    case 'FULL_AND_INCREMENTAL':
      return 'info'
    default:
      return ''
  }
}

// 任务类型标签
const getTaskTypeLabel = (type: string) => {
  switch (type) {
    case 'FULL_SYNC':
      return '全量同步'
    case 'INCREMENTAL_SYNC':
      return '增量同步'
    case 'FULL_AND_INCREMENTAL':
      return '全量加增量'
    default:
      return type
  }
}

// 节点操作
const handleAddNode = () => {
  nodeForm.value = {
    id: 0,
    name: '',
    type: '',
    config: ''
  }
  nodeDialogTitle.value = '添加节点'
  nodeDialogVisible.value = true
}

const handleEditNode = (row: any) => {
  nodeForm.value = { ...row }
  nodeDialogTitle.value = '编辑节点'
  nodeDialogVisible.value = true
}

const handleDeleteNode = async (id: number) => {
  try {
    // 这里应该调用实际的API删除节点
    nodes.value = nodes.value.filter(node => node.id !== id)
    ElMessage.success('节点删除成功')
  } catch (error) {
    console.error('Failed to delete node:', error)
    ElMessage.error('删除节点失败')
  }
}

const confirmNode = async () => {
  try {
    // 这里应该调用实际的API保存节点
    if (nodeForm.value.id === 0) {
      // 添加新节点
      const newNode = {
        id: Date.now(),
        ...nodeForm.value,
        status: 'ACTIVE',
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      nodes.value.push(newNode)
      ElMessage.success('节点添加成功')
    } else {
      // 更新节点
      const index = nodes.value.findIndex(node => node.id === nodeForm.value.id)
      if (index !== -1) {
        nodes.value[index] = { ...nodeForm.value }
        ElMessage.success('节点更新成功')
      }
    }
    nodeDialogVisible.value = false
  } catch (error) {
    console.error('Failed to save node:', error)
    ElMessage.error('保存节点失败')
  }
}

// 连接操作
const handleAddConnection = () => {
  connectionForm.value = {
    id: 0,
    sourceNodeId: '',
    targetNodeId: '',
    type: ''
  }
  connectionDialogTitle.value = '添加连接'
  connectionDialogVisible.value = true
}

const handleEditConnection = (row: any) => {
  connectionForm.value = { ...row }
  connectionDialogTitle.value = '编辑连接'
  connectionDialogVisible.value = true
}

const handleDeleteConnection = async (id: number) => {
  try {
    // 这里应该调用实际的API删除连接
    connections.value = connections.value.filter(conn => conn.id !== id)
    ElMessage.success('连接删除成功')
  } catch (error) {
    console.error('Failed to delete connection:', error)
    ElMessage.error('删除连接失败')
  }
}

const confirmConnection = async () => {
  try {
    // 这里应该调用实际的API保存连接
    if (connectionForm.value.id === 0) {
      // 添加新连接
      const newConnection = {
        id: Date.now(),
        ...connectionForm.value,
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      connections.value.push(newConnection)
      ElMessage.success('连接添加成功')
    } else {
      // 更新连接
      const index = connections.value.findIndex(conn => conn.id === connectionForm.value.id)
      if (index !== -1) {
        connections.value[index] = { ...connectionForm.value }
        ElMessage.success('连接更新成功')
      }
    }
    connectionDialogVisible.value = false
  } catch (error) {
    console.error('Failed to save connection:', error)
    ElMessage.error('保存连接失败')
  }
}

// 依赖操作
const handleAddDependency = () => {
  dependencyForm.value = {
    id: 0,
    dependencyTaskId: '',
    dependencyType: ''
  }
  dependencyDialogTitle.value = '添加依赖'
  dependencyDialogVisible.value = true
}

const handleEditDependency = (row: any) => {
  dependencyForm.value = { ...row }
  dependencyDialogTitle.value = '编辑依赖'
  dependencyDialogVisible.value = true
}

const handleDeleteDependency = async (id: number) => {
  try {
    // 这里应该调用实际的API删除依赖
    dependencies.value = dependencies.value.filter(dep => dep.id !== id)
    ElMessage.success('依赖删除成功')
  } catch (error) {
    console.error('Failed to delete dependency:', error)
    ElMessage.error('删除依赖失败')
  }
}

const confirmDependency = async () => {
  try {
    // 这里应该调用实际的API保存依赖
    if (dependencyForm.value.id === 0) {
      // 添加新依赖
      const newDependency = {
        id: Date.now(),
        ...dependencyForm.value,
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      dependencies.value.push(newDependency)
      ElMessage.success('依赖添加成功')
    } else {
      // 更新依赖
      const index = dependencies.value.findIndex(dep => dep.id === dependencyForm.value.id)
      if (index !== -1) {
        dependencies.value[index] = { ...dependencyForm.value }
        ElMessage.success('依赖更新成功')
      }
    }
    dependencyDialogVisible.value = false
  } catch (error) {
    console.error('Failed to save dependency:', error)
    ElMessage.error('保存依赖失败')
  }
}

// 向量化配置操作
const handleAddVectorizationConfig = () => {
  vectorizationForm.value = {
    id: 0,
    algorithm: '',
    dimension: 0,
    modelName: ''
  }
  vectorizationDialogTitle.value = '添加向量化配置'
  vectorizationDialogVisible.value = true
}

const handleEditVectorizationConfig = (row: any) => {
  vectorizationForm.value = { ...row }
  vectorizationDialogTitle.value = '编辑向量化配置'
  vectorizationDialogVisible.value = true
}

const handleDeleteVectorizationConfig = async (id: number) => {
  try {
    // 这里应该调用实际的API删除向量化配置
    vectorizationConfigs.value = vectorizationConfigs.value.filter(config => config.id !== id)
    ElMessage.success('向量化配置删除成功')
  } catch (error) {
    console.error('Failed to delete vectorization config:', error)
    ElMessage.error('删除向量化配置失败')
  }
}

const confirmVectorizationConfig = async () => {
  try {
    // 这里应该调用实际的API保存向量化配置
    if (vectorizationForm.value.id === 0) {
      // 添加新配置
      const newConfig = {
        id: Date.now(),
        ...vectorizationForm.value,
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      vectorizationConfigs.value.push(newConfig)
      ElMessage.success('向量化配置添加成功')
    } else {
      // 更新配置
      const index = vectorizationConfigs.value.findIndex(config => config.id === vectorizationForm.value.id)
      if (index !== -1) {
        vectorizationConfigs.value[index] = { ...vectorizationForm.value }
        ElMessage.success('向量化配置更新成功')
      }
    }
    vectorizationDialogVisible.value = false
  } catch (error) {
    console.error('Failed to save vectorization config:', error)
    ElMessage.error('保存向量化配置失败')
  }
}

// Milvus索引操作
const handleAddMilvusIndex = () => {
  milvusIndexForm.value = {
    id: 0,
    collectionName: '',
    indexName: '',
    indexType: '',
    fieldName: ''
  }
  milvusIndexDialogTitle.value = '添加Milvus索引'
  milvusIndexDialogVisible.value = true
}

const handleEditMilvusIndex = (row: any) => {
  milvusIndexForm.value = { ...row }
  milvusIndexDialogTitle.value = '编辑Milvus索引'
  milvusIndexDialogVisible.value = true
}

const handleDeleteMilvusIndex = async (id: number) => {
  try {
    // 这里应该调用实际的API删除Milvus索引
    milvusIndexes.value = milvusIndexes.value.filter(index => index.id !== id)
    ElMessage.success('Milvus索引删除成功')
  } catch (error) {
    console.error('Failed to delete Milvus index:', error)
    ElMessage.error('删除Milvus索引失败')
  }
}

const handleRebuildMilvusIndex = async (row: any) => {
  try {
    // 这里应该调用实际的API重建Milvus索引
    ElMessage.success('Milvus索引重建成功')
  } catch (error) {
    console.error('Failed to rebuild Milvus index:', error)
    ElMessage.error('重建Milvus索引失败')
  }
}

const confirmMilvusIndex = async () => {
  try {
    // 这里应该调用实际的API保存Milvus索引
    if (milvusIndexForm.value.id === 0) {
      // 添加新索引
      const newIndex = {
        id: Date.now(),
        ...milvusIndexForm.value,
        status: 'READY',
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
      }
      milvusIndexes.value.push(newIndex)
      ElMessage.success('Milvus索引添加成功')
    } else {
      // 更新索引
      const index = milvusIndexes.value.findIndex(idx => idx.id === milvusIndexForm.value.id)
      if (index !== -1) {
        milvusIndexes.value[index] = { ...milvusIndexForm.value }
        ElMessage.success('Milvus索引更新成功')
      }
    }
    milvusIndexDialogVisible.value = false
  } catch (error) {
    console.error('Failed to save Milvus index:', error)
    ElMessage.error('保存Milvus索引失败')
  }
}
</script>

<style scoped>
.task-detail {
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

/* 任务信息样式 */
.task-info {
  margin-bottom: 32px;
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

:deep(.el-descriptions) {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-descriptions__header) {
  background: rgba(15, 23, 42, 0.9);
  padding: 20px;
  border-bottom: 2px solid rgba(59, 130, 246, 0.5);
}

:deep(.el-descriptions__body) {
  padding: 20px;
}

:deep(.el-descriptions__item) {
  padding: 16px;
  border-right: 1px solid rgba(59, 130, 246, 0.1);
  border-bottom: 1px solid rgba(59, 130, 246, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-descriptions__item:last-child) {
  border-right: none;
}

:deep(.el-descriptions__item::before) {
  background: rgba(59, 130, 246, 0.1);
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  width: 120px;
}

/* 标签页样式 */
.task-tabs {
  margin-top: 32px;
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

:deep(.el-tabs) {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-tabs__header) {
  background: rgba(15, 23, 42, 0.9);
  border-bottom: 2px solid rgba(59, 130, 246, 0.5);
  padding: 0 24px;
}

:deep(.el-tabs__nav) {
  height: 60px;
}

:deep(.el-tabs__item) {
  height: 60px;
  line-height: 60px;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

:deep(.el-tabs__item:hover) {
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-tabs__item.is-active) {
  color: #ffffff;
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-tabs__item.is-active::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border-radius: 3px 3px 0 0;
}

:deep(.el-tabs__content) {
  padding: 24px;
}

.tab-content {
  animation: fadeIn 0.6s ease-out;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  margin-top: 24px;
  animation: slideUp 0.8s ease-out 0.2s both;
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
  .task-detail {
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
  
  :deep(.el-descriptions) {
    :deep(.el-descriptions-item) {
      padding: 12px;
    }
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
}
</style>