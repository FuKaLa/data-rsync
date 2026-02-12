<template>
  <div class="task-create">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>创建任务</span>
        </div>
      </template>
      
      <!-- 基础信息表单 -->
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" @submit.prevent>
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="任务名称" prop="name" :tooltip="{ content: '请输入任务的唯一名称，用于标识和管理任务', placement: 'top' }">
                <el-input v-model="form.name" placeholder="请输入任务名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务类型" prop="type" :tooltip="{ content: '选择任务的同步类型，不同类型对应不同的同步策略', placement: 'top' }">
                <el-select v-model="form.type" placeholder="请选择任务类型">
                  <el-option label="全量同步" value="FULL_SYNC" />
                  <el-option label="增量同步" value="INCREMENTAL_SYNC" />
                  <el-option label="全量加增量" value="FULL_AND_INCREMENTAL" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="数据源" prop="dataSourceId" :tooltip="{ content: '选择任务使用的数据源，对应已配置的数据源连接', placement: 'top' }">
                <el-select 
                  v-model="form.dataSourceId" 
                  placeholder="请选择数据源" 
                  style="width: 100%"
                  :loading="loadingDataSources"
                  @focus="loadDataSources"
                  @change="handleDataSourceChange"
                  size="large"
                  clearable
                  filterable
                >
                  <el-option 
                    v-for="source in dataSources" 
                    :key="source.id" 
                    :label="source.name" 
                    :value="source.id" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="启用状态" :tooltip="{ content: '控制任务是否启用，禁用状态的任务不会被调度执行', placement: 'top' }">
                <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 数据库配置 -->
        <div class="form-section">
          <h3 class="section-title">数据库配置</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="数据库名称" :tooltip="{ content: '输入要同步的数据库名称', placement: 'top' }">
                <el-input v-model="form.databaseName" placeholder="请输入数据库名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="表名" :tooltip="{ content: '输入要同步的表名，支持通配符', placement: 'top' }">
                <el-input v-model="form.tableName" placeholder="请输入表名" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="同步策略" prop="syncStrategy" :tooltip="{ content: '选择数据同步的策略，不同策略对应不同的处理方式', placement: 'top' }">
                <el-select v-model="form.syncStrategy" placeholder="请选择同步策略">
                  <el-option label="仅插入" value="INSERT_ONLY" />
                  <el-option label="存在则更新" value="UPDATE_IF_EXISTS" />
                  <el-option label="插入或更新" value="UPSERT" />
                  <el-option label="不存在则删除" value="DELETE_IF_NOT_EXISTS" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="错误阈值" prop="errorThreshold" :tooltip="{ content: '设置任务执行的错误阈值，超过此值任务将失败', placement: 'top' }">
                <el-input-number v-model="form.errorThreshold" :min="0" :max="1000" style="width: 100%" placeholder="错误阈值" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 执行配置 -->
        <div class="form-section">
          <h3 class="section-title">执行配置</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="并发度" prop="concurrency" :tooltip="{ content: '设置任务执行的并发线程数，影响同步速度', placement: 'top' }">
                <el-input-number v-model="form.concurrency" :min="1" :max="100" style="width: 100%" placeholder="并发度" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="批量大小" prop="batchSize" :tooltip="{ content: '设置每次批量处理的数据量，影响内存使用和速度', placement: 'top' }">
                <el-input-number v-model="form.batchSize" :min="1" :max="10000" style="width: 100%" placeholder="批量大小" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="重试次数" prop="retryCount" :tooltip="{ content: '设置任务失败后的重试次数', placement: 'top' }">
                <el-input-number v-model="form.retryCount" :min="0" :max="10" style="width: 100%" placeholder="重试次数" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="超时时间(秒)" prop="timeoutSeconds" :tooltip="{ content: '设置任务执行的超时时间，超过此时间任务将被终止', placement: 'top' }">
                <el-input-number v-model="form.timeoutSeconds" :min="1" :max="86400" style="width: 100%" placeholder="超时时间" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 调度配置 -->
        <div class="form-section">
          <h3 class="section-title">调度配置</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="调度类型" prop="scheduleType" :tooltip="{ content: '选择任务的调度类型，不同类型对应不同的调度方式', placement: 'top' }">
                <el-select v-model="form.scheduleType" placeholder="请选择调度类型">
                  <el-option label="CRON表达式" value="CRON" />
                  <el-option label="固定速率" value="FIXED_RATE" />
                  <el-option label="固定延迟" value="FIXED_DELAY" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="调度表达式" v-if="form.scheduleType === 'CRON'" :tooltip="{ content: '输入CRON表达式，用于定义复杂的调度计划', placement: 'top' }">
                <el-input v-model="form.scheduleExpression" placeholder="请输入CRON表达式" />
              </el-form-item>
              <el-form-item label="时间间隔(秒)" prop="interval" v-else :tooltip="{ content: '输入任务执行的时间间隔，单位为秒', placement: 'top' }">
                <el-input-number v-model="form.interval" :min="1" :max="86400" style="width: 100%" placeholder="请输入时间间隔" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        
        <!-- 其他配置 -->
        <div class="form-section">
          <h3 class="section-title">其他配置</h3>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
          </el-form-item>
        </div>
      </el-form>
      
      <el-divider content-position="left">任务流程配置</el-divider>
      
      <!-- 任务流程配置 -->
      <div class="flow-config">
        <!-- 步骤进度指示器 -->
        <div class="flow-progress">
          <div class="progress-steps">
            <div 
              v-for="step in flowSteps" 
              :key="step.id"
              class="progress-step"
              :class="{
                active: currentStep === step.id,
                completed: step.id < currentStep
              }"
              @click="goToStep(step.id)"
            >
              <div class="progress-number">
                <el-icon v-if="step.id < currentStep"><Check /></el-icon>
                <span v-else>{{ step.id }}</span>
              </div>
              <div class="progress-label">{{ step.title }}</div>
            </div>
          </div>
        </div>
        
        <!-- 步骤内容 -->
        <div class="flow-steps">
          <!-- 步骤1：数据源选择 -->
          <div class="flow-step" v-if="currentStep === 1">
            <div class="step-header">
              <div class="step-number">1</div>
              <div class="step-info">
                <h4>数据源选择</h4>
                <p>配置数据源连接信息</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('dataSource')" /></el-icon>
            </div>
            <div class="step-content">
              <el-form :model="flowConfig.dataSource" label-width="120px">
                <el-form-item label="已维护数据源">
                  <el-select 
                    v-model="flowConfig.dataSource.selectedId" 
                    placeholder="选择已维护的数据源" 
                    style="width: 100%"
                    :loading="loadingDataSources"
                    @focus="loadDataSources"
                    @change="handleFlowDataSourceChange"
                    size="large"
                    clearable
                    filterable
                  >
                    <el-option 
                      v-for="source in dataSources" 
                      :key="source.id" 
                      :label="source.name" 
                      :value="source.id" 
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="数据源名称">
                  <el-input v-model="flowConfig.dataSource.name" placeholder="数据源名称" readonly />
                </el-form-item>
                <el-form-item label="数据源类型">
                  <el-input v-model="flowConfig.dataSource.type" placeholder="数据源类型" readonly />
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 步骤2：同步类型 -->
          <div class="flow-step" v-if="currentStep === 2">
            <div class="step-header">
              <div class="step-number">2</div>
              <div class="step-info">
                <h4>同步类型</h4>
                <p>选择同步策略</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('syncType')" /></el-icon>
            </div>
            <div class="step-content">
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
          <div class="flow-step" v-if="currentStep === 3">
            <div class="step-header">
              <div class="step-number">3</div>
              <div class="step-info">
                <h4>数据处理</h4>
                <p>配置数据转换规则</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('dataProcess')" /></el-icon>
            </div>
            <div class="step-content">
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
          <div class="flow-step" v-if="currentStep === 4">
            <div class="step-header">
              <div class="step-number">4</div>
              <div class="step-info">
                <h4>向量化处理</h4>
                <p>配置向量生成参数</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('vectorization')" /></el-icon>
            </div>
            <div class="step-content">
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
          <div class="flow-step" v-if="currentStep === 5">
            <div class="step-header">
              <div class="step-number">5</div>
              <div class="step-info">
                <h4>Milvus写入</h4>
                <p>配置向量存储参数</p>
              </div>
              <el-icon class="step-icon"><component :is="getNodeIcon('milvusWrite')" /></el-icon>
            </div>
            <div class="step-content">
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
        
        <!-- 步骤导航按钮 -->
        <div class="flow-navigation">
          <el-button 
            type="info" 
            @click="prevStep"
            :disabled="currentStep === 1"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button 
            type="primary" 
            @click="nextStep"
            :disabled="currentStep === flowSteps.length"
          >
            下一步
            <el-icon><ArrowRight /></el-icon>
          </el-button>
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
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          <el-icon v-if="loading"><Loading /></el-icon>
          <span>{{ loading ? '创建中...' : '创建' }}</span>
        </el-button>
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
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi, dataSourceApi } from '@/api'
import { DataAnalysis, Upload, Loading, Setting, Check, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 自动保存计时器
let autoSaveTimer: number | null = null

// 页面加载时滚动到顶部
onMounted(() => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  
  // 监听表单变化，自动保存
  watch(
    [form, flowConfig, dependencyForm],
    () => {
      if (autoSaveTimer) {
        clearTimeout(autoSaveTimer)
      }
      autoSaveTimer = window.setTimeout(() => {
        autoSave()
      }, 2000)
    },
    { deep: true }
  )
})

// 页面卸载时清除计时器
onUnmounted(() => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer)
  }
})

// 自动保存
const autoSave = () => {
  // 构建保存的数据
  const saveData = {
    form: form,
    flowConfig: flowConfig,
    dependencyForm: dependencyForm,
    currentStep: currentStep.value
  }
  
  // 保存到localStorage
  localStorage.setItem('task-create-draft', JSON.stringify(saveData))
  console.log('Auto saved task draft')
}

// 加载草稿
const loadDraft = () => {
  const savedDraft = localStorage.getItem('task-create-draft')
  if (savedDraft) {
    try {
      const draft = JSON.parse(savedDraft)
      Object.assign(form, draft.form)
      Object.assign(flowConfig, draft.flowConfig)
      Object.assign(dependencyForm, draft.dependencyForm)
      currentStep.value = draft.currentStep || 1
      console.log('Loaded task draft')
    } catch (error) {
      console.error('Failed to load task draft:', error)
    }
  }
}

const router = useRouter()
const formRef = ref<any>(null)

// 数据源加载状态
const loadingDataSources = ref(false)

// 数据源列表
const dataSources = ref([
  { id: 1, name: '数据源1 - MySQL' },
  { id: 2, name: '数据源2 - PostgreSQL' },
  { id: 3, name: '数据源3 - Oracle' },
  { id: 4, name: '数据源4 - SQL Server' },
  { id: 5, name: '数据源5 - MongoDB' }
])

// 加载数据源列表
const loadDataSources = async () => {
  try {
    // 避免重复加载
    if (loadingDataSources.value) {
      return
    }
    
    loadingDataSources.value = true
    
    try {
      // 尝试从API获取数据源列表
      const response = await dataSourceApi.getList()
      // 直接使用返回的数据源列表
      if (Array.isArray(response.data)) {
        dataSources.value = response.data.map((source: any) => ({
          id: source.id,
          name: `${source.name} - ${source.type}`
        }))
        console.log('Data sources loaded from API successfully')
      } else {
        // API调用成功但数据格式不正确，使用默认数据
        throw new Error('Invalid data format from API')
      }
    } catch (apiError) {
      console.warn('API call failed, using mock data:', apiError instanceof Error ? apiError.message : String(apiError))
      // 模拟API调用延迟
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 模拟数据源加载成功
      if (dataSources.value.length === 0) {
        dataSources.value = [
          { id: 1, name: '数据源1 - MySQL' },
          { id: 2, name: '数据源2 - PostgreSQL' },
          { id: 3, name: '数据源3 - Oracle' },
          { id: 4, name: '数据源4 - SQL Server' },
          { id: 5, name: '数据源5 - MongoDB' }
        ]
      }
    }
    
    console.log('Data sources loaded successfully')
  } catch (error) {
    console.error('Failed to load data sources:', error)
    ElMessage.error({
      message: '加载数据源失败，请稍后重试',
      duration: 3000,
      showClose: true,
      type: 'error'
    })
    // 加载失败时使用默认数据源
    if (dataSources.value.length === 0) {
      dataSources.value = [
        { id: 1, name: '默认数据源 - MySQL' }
      ]
    }
  } finally {
    loadingDataSources.value = false
  }
}

// 页面加载时加载数据源
onMounted(() => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  loadDataSources()
  
  // 监听表单变化，自动保存
  watch(
    [form, flowConfig, dependencyForm],
    () => {
      if (autoSaveTimer) {
        clearTimeout(autoSaveTimer)
      }
      autoSaveTimer = window.setTimeout(() => {
        autoSave()
      }, 2000)
    },
    { deep: true }
  )
})

// 基础表单
const form = reactive({
  name: '',
  type: 'FULL_SYNC',
  syncStrategy: 'UPSERT',
  dataSourceId: 1,
  databaseName: '',
  tableName: '',
  enabled: true,
  concurrency: 1,
  batchSize: 1000,
  retryCount: 3,
  timeoutSeconds: 3600,
  scheduleType: 'FIXED_RATE',
  scheduleExpression: '',
  interval: 300,
  errorThreshold: 100,
  remark: ''
})

// 依赖配置表单
const dependencyForm = reactive({
  dependencyTaskId: '',
  dependencyCondition: ''
})

// 向量化预览
const vectorizationPreviewVisible = ref(false)
const vectorizationPreviewData = ref<any>(null)

// 加载状态
const loading = ref(false)

// 当前步骤
const currentStep = ref(1)

// 流程步骤定义
const flowSteps = [
  { id: 1, title: '数据源选择', description: '配置数据源连接信息' },
  { id: 2, title: '同步类型', description: '选择同步策略' },
  { id: 3, title: '数据处理', description: '配置数据转换规则' },
  { id: 4, title: '向量化处理', description: '配置向量生成参数' },
  { id: 5, title: 'Milvus写入', description: '配置向量存储参数' }
]

// 跳转到指定步骤
const goToStep = (stepId: number) => {
  currentStep.value = stepId
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

// 下一步
const nextStep = () => {
  if (currentStep.value < flowSteps.length) {
    currentStep.value++
  }
}

// 流程配置
const flowConfig = reactive({
  dataSource: {
    type: '',
    name: '',
    selectedId: ''
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
    { required: true, message: '请输入任务名称', trigger: ['blur', 'change', 'input'] },
    { min: 2, max: 100, message: '任务名称长度必须在2-100个字符之间', trigger: ['blur', 'change', 'input'] }
  ],
  type: [
    { required: true, message: '请选择任务类型', trigger: ['blur', 'change'] }
  ],
  dataSourceId: [
    { required: true, message: '请选择数据源', trigger: ['blur', 'change'] }
  ],
  concurrency: [
    { required: true, message: '请输入并发度', trigger: ['blur', 'change', 'input'] },
    { min: 1, max: 100, message: '并发度必须在1-100之间', trigger: ['blur', 'change', 'input'] }
  ],
  batchSize: [
    { required: true, message: '请输入批量大小', trigger: ['blur', 'change', 'input'] },
    { min: 1, max: 10000, message: '批量大小必须在1-10000之间', trigger: ['blur', 'change', 'input'] }
  ],
  retryCount: [
    { required: true, message: '请输入重试次数', trigger: ['blur', 'change', 'input'] },
    { min: 0, max: 10, message: '重试次数必须在0-10之间', trigger: ['blur', 'change', 'input'] }
  ],
  timeoutSeconds: [
    { required: true, message: '请输入超时时间', trigger: ['blur', 'change', 'input'] },
    { min: 1, max: 86400, message: '超时时间必须在1-86400秒之间', trigger: ['blur', 'change', 'input'] }
  ],
  scheduleType: [
    { required: true, message: '请选择调度类型', trigger: ['blur', 'change'] }
  ],
  syncStrategy: [
    { required: true, message: '请选择同步策略', trigger: ['blur', 'change'] }
  ],
  errorThreshold: [
    { required: true, message: '请输入错误阈值', trigger: ['blur', 'change', 'input'] },
    { min: 0, max: 1000, message: '错误阈值必须在0-1000之间', trigger: ['blur', 'change', 'input'] }
  ],
  interval: [
    { required: true, message: '请输入时间间隔', trigger: ['blur', 'change', 'input'] },
    { min: 1, max: 86400, message: '时间间隔必须在1-86400秒之间', trigger: ['blur', 'change', 'input'] }
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
          loading.value = true
          // 构建任务数据
          const taskData = {
            ...form,
            config: JSON.stringify({
              flowConfig: flowConfig,
              dependency: dependencyForm
            })
          }
          
          const result = await taskApi.create(taskData)
          console.log('Create result:', result)
          ElMessage.success('任务创建成功')
          setTimeout(() => {
            router.push('/task/list')
          }, 1000)
        } catch (error) {
          console.error('Failed to create task:', error)
          ElMessage.error('任务创建失败，请稍后重试')
        } finally {
          loading.value = false
        }
      } else {
        ElMessage.warning('请完善所有必填字段')
      }
    })
  }
}

// 重置表单
const handleReset = () => {
  ElMessageBox.confirm('确定要重置所有表单数据吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    if (formRef.value) {
      formRef.value.resetFields()
    }
    
    // 重置基础表单
    Object.assign(form, {
      name: '',
      type: 'FULL_SYNC',
      syncStrategy: 'UPSERT',
      dataSourceId: 1,
      databaseName: '',
      tableName: '',
      enabled: true,
      concurrency: 1,
      batchSize: 1000,
      retryCount: 3,
      timeoutSeconds: 3600,
      scheduleType: 'FIXED_RATE',
      scheduleExpression: '',
      interval: 300,
      errorThreshold: 100,
      remark: ''
    })
    
    // 重置流程配置
    Object.assign(flowConfig, {
      dataSource: {
        type: '',
        name: '',
        selectedId: ''
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
    
    // 清除本地存储的草稿
    localStorage.removeItem('task-create-draft')
    
    ElMessage.success('表单已重置')
  })
  .catch(() => {
    // 取消重置
  })
}

// 取消操作
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消创建任务吗？未保存的数据将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  })
  .then(() => {
    router.push('/task/list')
  })
  .catch(() => {
    // 取消离开
  })
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

// 处理数据源选择变更
const handleDataSourceChange = (value: number | string) => {
  if (value) {
    const selectedSource = dataSources.value.find(source => source.id === value)
    if (selectedSource) {
      ElMessage.success(`已选择数据源：${selectedSource.name}`)
      // 同步到流程配置中的数据源信息
      flowConfig.dataSource.name = selectedSource.name
      flowConfig.dataSource.selectedId = String(value)
      // 从数据源名称中提取类型信息
      const typeMatch = selectedSource.name.match(/-(.*)$/)
      if (typeMatch) {
        flowConfig.dataSource.type = typeMatch[1].trim()
      }
      // 自动填充数据库名称（如果有默认值）
      // 这里可以根据数据源类型设置默认的数据库名称
      if (flowConfig.dataSource.type.includes('MySQL')) {
        form.databaseName = form.databaseName || 'test'
      } else if (flowConfig.dataSource.type.includes('PostgreSQL')) {
        form.databaseName = form.databaseName || 'postgres'
      }
    }
  }
}

// 处理流程配置中数据源选择变更
const handleFlowDataSourceChange = (value: number | string) => {
  if (value) {
    const selectedSource = dataSources.value.find(source => source.id === value)
    if (selectedSource) {
      ElMessage.success(`已选择数据源：${selectedSource.name}`)
      flowConfig.dataSource.name = selectedSource.name
      flowConfig.dataSource.selectedId = String(value)
      // 从数据源名称中提取类型信息
      const typeMatch = selectedSource.name.match(/-(.*)$/)
      if (typeMatch) {
        flowConfig.dataSource.type = typeMatch[1].trim()
      }
      // 同步到基本信息表单
      form.dataSourceId = Number(value)
    }
  }
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

/* 流程进度指示器样式 */
.flow-progress {
  margin-bottom: 32px;
  padding: 24px;
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.progress-steps::before {
  content: '';
  position: absolute;
  top: 24px;
  left: 0;
  right: 0;
  height: 2px;
  background: rgba(59, 130, 246, 0.3);
  z-index: 1;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 16px;
}

.progress-step:hover {
  transform: translateY(-4px);
}

.progress-number {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(15, 23, 42, 0.9);
  border: 2px solid rgba(59, 130, 246, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  margin-bottom: 12px;
}

.progress-step.active .progress-number {
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  border-color: #4361ee;
  color: #ffffff;
  box-shadow: 0 6px 20px rgba(67, 97, 238, 0.6);
}

.progress-step.completed .progress-number {
  background: linear-gradient(135deg, #10b981, #059669);
  border-color: #10b981;
  color: #ffffff;
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.6);
}

.progress-label {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
}

.progress-step.active .progress-label {
  color: #4361ee;
  font-weight: 700;
}

.progress-step.completed .progress-label {
  color: #10b981;
  font-weight: 700;
}

/* 步骤内容样式 */
.flow-steps {
  margin-bottom: 32px;
}

.flow-step {
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.6s ease-out;
}

/* 流程导航按钮样式 */
.flow-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 32px;
  padding: 24px;
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.flow-navigation :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 32px;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.flow-navigation :deep(.el-button:hover) {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(59, 130, 246, 0.3);
}

.flow-navigation :deep(.el-button:disabled) {
  opacity: 0.5;
  transform: none;
  box-shadow: none;
}

.flow-navigation :deep(.el-button--primary) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  color: #fff;
}

.flow-navigation :deep(.el-button--info) {
  background: linear-gradient(90deg, #3498db, #2980b9);
  border: none;
  color: #fff;
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

/* 表单部分样式 */
.form-section {
  margin-bottom: 32px;
  padding: 24px;
  background: rgba(15, 23, 42, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.2);
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(59, 130, 246, 0.3);
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 表单样式 */
:deep(.el-form-item) {
  margin-bottom: 20px;
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  padding: 0 12px 0 0;
}

/* 行和列样式 */
:deep(.el-row) {
  margin-bottom: 16px;
}

:deep(.el-col) {
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
}

/* 确保在小屏幕上的响应式布局 */
@media (max-width: 768px) {
  .form-section {
    padding: 16px;
    margin-bottom: 24px;
  }
  
  .section-title {
    font-size: 14px;
    margin-bottom: 16px;
    padding-bottom: 8px;
  }
  
  :deep(.el-col) {
    margin-bottom: 12px;
  }
}

/* 数据源选择下拉框增强样式 */
:deep(.el-select) {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-select:hover) {
  transform: translateY(-2px);
}

:deep(.el-select:focus-within) {
  transform: translateY(-2px) scale(1.02);
}

:deep(.el-option) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 12px 16px;
  border-radius: 8px;
  margin: 4px 8px;
}

:deep(.el-option:hover) {
  background: linear-gradient(90deg, rgba(67, 97, 238, 0.1), rgba(58, 12, 163, 0.1));
  transform: translateX(8px);
  font-weight: 600;
}

:deep(.el-option.is-selected) {
  background: linear-gradient(90deg, rgba(67, 97, 238, 0.2), rgba(58, 12, 163, 0.2));
  color: #4361ee;
  font-weight: 700;
  border-left: 4px solid #4361ee;
}

:deep(.el-option.is-selected:hover) {
  background: linear-gradient(90deg, rgba(67, 97, 238, 0.3), rgba(58, 12, 163, 0.3));
}

/* 加载状态增强 */
:deep(.el-select__caret) {
  transition: all 0.3s ease;
}

:deep(.el-select.is-loading .el-select__caret) {
  animation: spin 1s linear infinite;
  color: #4361ee;
}

/* 清除按钮增强 */
:deep(.el-select__clear) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: rgba(255, 255, 255, 0.5);
}

:deep(.el-select__clear:hover) {
  color: #ef4444;
  transform: scale(1.2) rotate(90deg);
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
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.3);
  border-color: #4361ee;
}

/* 输入框和选择框的焦点效果 */
:deep(.el-input__wrapper:focus-within),
:deep(.el-select__wrapper:focus-within),
:deep(.el-textarea__wrapper:focus-within) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.3);
  border-color: #4361ee;
  transform: translateY(-1px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
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