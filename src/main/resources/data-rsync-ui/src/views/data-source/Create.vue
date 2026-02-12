<template>
  <div class="data-source-create">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon class="title-icon"><Plus /></el-icon>
        创建数据源
      </h1>
      <p class="page-subtitle">配置新的数据同步源连接</p>
    </div>

    <!-- 主表单卡片 -->
    <div class="form-card">
      <!-- 表单步骤指示器 -->
      <div class="form-steps">
        <div class="step-item active">
          <div class="step-number">1</div>
          <div class="step-label">基本信息</div>
        </div>
        <div class="step-item">
          <div class="step-number">2</div>
          <div class="step-label">连接配置</div>
        </div>
        <div class="step-item">
          <div class="step-number">3</div>
          <div class="step-label">高级设置</div>
        </div>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="140px" class="main-form">
        <!-- 基本信息部分 -->
        <div class="form-section">
          <h3 class="section-title">
            <el-icon class="section-icon"><InfoFilled /></el-icon>
            基本信息
          </h3>
          <div class="form-grid">
            <el-form-item label="数据源名称" prop="name" class="form-item">
              <el-input v-model="form.name" placeholder="请输入数据源名称" class="form-input" />
            </el-form-item>
            <el-form-item label="数据源类型" prop="type" class="form-item">
              <el-select v-model="form.type" placeholder="请选择数据源类型" class="form-select" @change="handleTypeChange">
                <el-option label="MySQL" value="MYSQL" />
                <el-option label="PostgreSQL" value="POSTGRESQL" />
                <el-option label="Oracle" value="ORACLE" />
                <el-option label="SQL Server" value="SQL_SERVER" />
                <el-option label="MongoDB" value="MONGODB" />
                <el-option label="Redis" value="REDIS" />
              </el-select>
            </el-form-item>
            <el-form-item label="模板选择" class="form-item">
              <div class="template-selector">
                <el-select v-model="selectedTemplate" placeholder="选择预设模板" class="form-select" @change="handleTemplateChange">
                  <el-option label="默认模板" value="default" />
                  <el-option label="MySQL 模板" value="mysql" />
                  <el-option label="PostgreSQL 模板" value="postgresql" />
                  <el-option label="Oracle 模板" value="oracle" />
                  <el-option label="MongoDB 模板" value="mongodb" />
                </el-select>
                <el-button type="primary" size="small" class="template-btn" @click="saveAsTemplate">
                  <el-icon><Download /></el-icon>
                  保存为模板
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="描述" class="form-item full-width">
              <el-input v-model="form.description" type="textarea" placeholder="请输入数据源描述" class="form-textarea" :rows="3" />
            </el-form-item>
          </div>
        </div>

        <!-- 连接配置部分 -->
        <div class="form-section">
          <h3 class="section-title">
            <el-icon class="section-icon"><Link /></el-icon>
            连接配置
          </h3>
          <div class="form-grid">
            <!-- 主机和端口输入 -->
            <el-form-item label="主机地址" prop="host" v-if="showHostPortFields" class="form-item">
              <el-input v-model="form.host" placeholder="请输入主机地址" class="form-input" @blur="generateUrl" />
            </el-form-item>
            <el-form-item label="端口" prop="port" v-if="showHostPortFields" class="form-item">
              <el-input v-model="form.port" type="number" placeholder="请输入端口" class="form-input" @blur="[validatePort, generateUrl]" />
              <el-tooltip v-if="portError" :content="portError" placement="right" :effect="'error'">
                <el-icon class="error-icon"><CircleCloseFilled /></el-icon>
              </el-tooltip>
            </el-form-item>
            
            <!-- 数据库名称 -->
            <el-form-item label="数据库名称" prop="databaseName" v-if="showDatabaseNameField" class="form-item">
              <el-input v-model="form.databaseName" placeholder="请输入数据库名称" class="form-input" @blur="generateUrl" />
            </el-form-item>
            
            <!-- 连接地址 -->
            <el-form-item label="连接地址" prop="url" class="form-item full-width">
              <div class="url-input-group">
                <el-input v-model="form.url" placeholder="请输入连接地址" class="form-input" @blur="validateUrl" />
                <el-button type="info" size="small" class="url-btn" @click="generateUrl" v-if="canGenerateUrl">
                  <el-icon><Refresh /></el-icon>
                  自动生成
                </el-button>
              </div>
              <el-tooltip v-if="urlError" :content="urlError" placement="right" :effect="'error'">
                <el-icon class="error-icon"><CircleCloseFilled /></el-icon>
              </el-tooltip>
            </el-form-item>
            
            <!-- 驱动类 -->
            <el-form-item label="驱动类" prop="driverClass" class="form-item full-width">
              <el-input v-model="form.driverClass" placeholder="请输入驱动类" class="form-input" />
            </el-form-item>
            
            <!-- 日志监听方式 -->
            <el-form-item label="日志监听方式" prop="logMonitorType" class="form-item">
              <el-select v-model="form.logMonitorType" placeholder="请选择日志监听方式" class="form-select">
                <el-option label="Binlog" value="BINLOG" />
                <el-option label="WAL" value="WAL" />
                <el-option label="CDC" value="CDC" />
                <el-option label="Polling" value="POLLING" />
              </el-select>
            </el-form-item>
            
            <!-- 日志监控频率 -->
            <el-form-item label="日志监控频率" prop="logMonitorInterval" v-if="form.logMonitorType" class="form-item">
              <el-input-number v-model="form.logMonitorInterval" :min="1000" :max="3600000" :step="1000" class="form-input" />
              <span class="input-suffix">毫秒</span>
            </el-form-item>
            
            <!-- 日志监控超时时间 -->
            <el-form-item label="日志监控超时" prop="logMonitorTimeout" v-if="form.logMonitorType" class="form-item">
              <el-input-number v-model="form.logMonitorTimeout" :min="5000" :max="300000" :step="5000" class="form-input" />
              <span class="input-suffix">毫秒</span>
            </el-form-item>
            
            <!-- 连接超时时间 -->
            <el-form-item label="连接超时时间" prop="connectionTimeout" class="form-item">
              <el-input-number v-model="form.connectionTimeout" :min="1000" :max="60000" :step="1000" class="form-input-number" />
              <span class="input-suffix">毫秒</span>
            </el-form-item>
          </div>
        </div>

        <!-- 认证信息部分 -->
        <div class="form-section">
          <h3 class="section-title">
            <el-icon class="section-icon"><Lock /></el-icon>
            认证信息
          </h3>
          <div class="form-grid">
            <el-form-item label="用户名" prop="username" v-if="showCredentialsFields" class="form-item">
              <el-input v-model="form.username" placeholder="请输入用户名" class="form-input" />
            </el-form-item>
            <el-form-item label="密码" prop="password" v-if="showCredentialsFields" class="form-item">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" class="form-input" show-password />
            </el-form-item>
          </div>
        </div>

        <!-- 高级设置部分 -->
        <div class="form-section">
          <h3 class="section-title">
            <el-icon class="section-icon"><Setting /></el-icon>
            高级设置
          </h3>
          <div class="form-grid">
            <el-form-item label="启用状态" prop="enabled" class="form-item">
              <el-switch v-model="form.enabled" active-color="#4361ee" inactive-color="#6b7280" inline-prompt active-text="启用" inactive-text="禁用" />
            </el-form-item>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button type="primary" class="action-btn submit" @click="handleSubmit">
            <el-icon><Check /></el-icon>
            创建
          </el-button>
          <el-button type="success" class="action-btn test" @click="handleTestConnection">
            <el-icon><CircleCheck /></el-icon>
            测试连接
          </el-button>
          <el-button type="info" class="action-btn reset" @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button class="action-btn cancel" @click="handleCancel">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 测试连接结果对话框 -->
    <el-dialog
      v-model="testResultVisible"
      title="连接测试结果"
      width="400px"
    >
      <div class="test-result">
        <el-icon :class="testResult.success ? 'success-icon' : 'error-icon'">
          {{ testResult.success ? 'Check' : 'Close' }}
        </el-icon>
        <h4 :class="testResult.success ? 'success' : 'error'">
          {{ testResult.success ? '连接成功' : '连接失败' }}
        </h4>
        <p v-if="testResult.message">{{ testResult.message }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="testResultVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { dataSourceApi } from '@/api'
import { Plus, InfoFilled, Link, Lock, Setting, Check, CircleCheck, Refresh, Close, Download } from '@element-plus/icons-vue'
import { CircleCloseFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref<any>(null)
const selectedTemplate = ref('default')
const urlError = ref('')
const portError = ref('')

// 测试连接结果
const testResultVisible = ref(false)
const testResult = ref({ success: false, message: '' })

const form = ref({
  name: '',
  type: '',
  host: '',
  port: null as number | null,
  databaseName: '',
  username: '',
  password: '',
  url: '',
  driverClass: '',
  logMonitorType: '',
  logMonitorInterval: 60,
  logMonitorTimeout: 30,
  connectionTimeout: 30,
  enabled: true,
  description: ''
})

// 计算属性：根据数据库类型显示/隐藏主机和端口字段
const showHostPortFields = computed(() => {
  const type = form.value.type
  return type && ['MYSQL', 'POSTGRESQL', 'ORACLE', 'SQL_SERVER', 'MONGODB', 'REDIS'].includes(type)
})

// 计算属性：根据数据库类型显示/隐藏数据库名称字段
const showDatabaseNameField = computed(() => {
  const type = form.value.type
  return type && ['MYSQL', 'POSTGRESQL', 'SQL_SERVER', 'MONGODB'].includes(type)
})

// 计算属性：根据数据库类型显示/隐藏凭证字段
const showCredentialsFields = computed(() => {
  const type = form.value.type
  return type && ['MYSQL', 'POSTGRESQL', 'ORACLE', 'SQL_SERVER', 'MONGODB', 'REDIS'].includes(type)
})

// 计算属性：是否可以自动生成URL
const canGenerateUrl = computed(() => {
  const type = form.value.type
  return type && ['MYSQL', 'POSTGRESQL', 'ORACLE', 'SQL_SERVER', 'MONGODB', 'REDIS'].includes(type)
})

const rules = reactive({
  name: [
    { required: true, message: '请输入数据源名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择数据源类型', trigger: 'change' }
  ],
  host: [
    { required: computed(() => showHostPortFields.value), message: '请输入主机地址', trigger: 'blur' }
  ],
  port: [
    { required: computed(() => showHostPortFields.value), message: '请输入端口', trigger: 'blur' }
  ],
  databaseName: [
    { required: computed(() => showDatabaseNameField.value), message: '请输入数据库名称', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入连接地址', trigger: 'blur' }
  ],
  username: [
    { required: computed(() => showCredentialsFields.value), message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: computed(() => showCredentialsFields.value), message: '请输入密码', trigger: 'blur' }
  ]
})

// 预设模板配置
const templates = {
  mysql: {
    driverClass: 'com.mysql.cj.jdbc.Driver',
    logMonitorType: 'BINLOG',
    logMonitorInterval: 5000,
    logMonitorTimeout: 60000,
    connectionTimeout: 30000,
    port: '3306'
  },
  postgresql: {
    driverClass: 'org.postgresql.Driver',
    logMonitorType: 'WAL',
    logMonitorInterval: 5000,
    logMonitorTimeout: 60000,
    connectionTimeout: 30000,
    port: '5432'
  },
  oracle: {
    driverClass: 'oracle.jdbc.OracleDriver',
    logMonitorType: 'CDC',
    logMonitorInterval: 10000,
    logMonitorTimeout: 120000,
    connectionTimeout: 60000,
    port: '1521'
  },
  mongodb: {
    driverClass: 'mongodb.driver.MongoDriver',
    logMonitorType: 'CDC',
    logMonitorInterval: 5000,
    logMonitorTimeout: 60000,
    connectionTimeout: 30000,
    port: '27017'
  }
}

// 数据库类型对应的连接URL模板
const urlTemplates = {
  MYSQL: (host: string, port: string, database: string = '') => 
    `jdbc:mysql://${host}:${port}/${database}?useSSL=false&serverTimezone=UTC`,
  POSTGRESQL: (host: string, port: string, database: string = '') => 
    `jdbc:postgresql://${host}:${port}/${database}`,
  ORACLE: (host: string, port: string, database: string = '') => 
    `jdbc:oracle:thin:@${host}:${port}:${database}`,
  SQL_SERVER: (host: string, port: string, database: string = '') => 
    `jdbc:sqlserver://${host}:${port};databaseName=${database}`,
  MONGODB: (host: string, port: string, database: string = '') => 
    `mongodb://${host}:${port}/${database}`,
  REDIS: (host: string, port: string, database: string = '') => 
    `redis://${host}:${port}`
}

const handleTypeChange = (type: string) => {
  // 重置相关字段
  form.value.host = ''
  form.value.databaseName = ''
  form.value.url = ''
  
  // 根据数据源类型自动选择对应模板
  if (type === 'MYSQL') {
    selectedTemplate.value = 'mysql'
  } else if (type === 'POSTGRESQL') {
    selectedTemplate.value = 'postgresql'
  } else if (type === 'ORACLE') {
    selectedTemplate.value = 'oracle'
  } else if (type === 'MONGODB') {
    selectedTemplate.value = 'mongodb'
  } else {
    selectedTemplate.value = 'default'
  }
  handleTemplateChange(selectedTemplate.value)
}

const handleTemplateChange = (template: string) => {
  if (templates[template as keyof typeof templates]) {
    const templateConfig = templates[template as keyof typeof templates]
    Object.assign(form, templateConfig)
  }
}

// 生成连接URL
const generateUrl = () => {
  const type = form.value.type
  const host = form.value.host
  const port = form.value.port
  const database = form.value.databaseName
  
  if (!type || !host || !port) {
    urlError.value = '请填写主机和端口信息'
    return
  }
  
  try {
    const portStr = port ? port.toString() : ''
    const template = urlTemplates[type as keyof typeof urlTemplates]
    if (template) {
      if (type === 'REDIS') {
        form.value.url = template(host, portStr)
      } else {
        form.value.url = template(host, portStr, database)
      }
      urlError.value = ''
    }
  } catch (error) {
    urlError.value = '生成连接地址失败'
  }
}

const validateUrl = () => {
  const url = form.value.url
  if (!url) {
    urlError.value = ''
    return
  }
  
  const type = form.value.type
  let isValid = true
  
  // 根据数据库类型进行特定的URL格式校验
  switch (type) {
    case 'MYSQL':
      isValid = /^jdbc:mysql:\/\/.+:\d+\/.+/.test(url)
      break
    case 'POSTGRESQL':
      isValid = /^jdbc:postgresql:\/\/.+:\d+\/.+/.test(url)
      break
    case 'ORACLE':
      isValid = /^jdbc:oracle:thin:@.+:\d+:.+/.test(url)
      break
    case 'SQL_SERVER':
      isValid = /^jdbc:sqlserver:\/\/.+:\d+;databaseName=.+/.test(url)
      break
    case 'MONGODB':
      isValid = /^mongodb:\/\/.+:\d+/.test(url)
      break
    case 'REDIS':
      isValid = /^redis:\/\/.+:\d+/.test(url)
      break
    default:
      // 通用URL校验
      isValid = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/.test(url)
  }
  
  if (!isValid) {
    urlError.value = '连接地址格式不正确'
  } else {
    urlError.value = ''
  }
}

const validatePort = () => {
  const port = form.value.port
  if (port === null) {
    portError.value = ''
    return
  }
  
  const portStr = port.toString()
  const portNum = parseInt(portStr)
  if (isNaN(portNum) || portNum < 1 || portNum > 65535) {
    portError.value = '端口必须是1-65535之间的数字'
  } else {
    form.value.port = portNum
    portError.value = ''
  }
}

const saveAsTemplate = () => {
  // 保存当前配置为模板的逻辑
  console.log('保存为模板:', form)
  // 这里可以调用API保存模板
}

const handleSubmit = async () => {
  if (formRef.value) {
    await formRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          // 转换form对象以符合CreateDataSourceRequest类型
          const createData = {
            ...form.value,
            port: form.value.port || undefined
          }
          const result = await dataSourceApi.create(createData)
          console.log('Create result:', result)
          if (result.data.code === 200) {
            // 创建成功，返回列表页
            router.push('/data-source/list')
          } else {
            // 创建失败，显示错误信息
            ElMessage.error(`创建失败: ${result.data.message}`)
          }
        } catch (error) {
          console.error('Failed to create data source:', error)
          ElMessage.error('创建失败: 网络错误或服务器异常')
        }
      }
    })
  }
}

const handleTestConnection = async () => {
  if (formRef.value) {
    await formRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          // 调用测试新连接的API
          const testData = {
            ...form.value,
            port: form.value.port || undefined
          }
          const response = await dataSourceApi.testNewConnection(testData)
          testResult.value = {
            success: response.data.success,
            message: response.data.message
          }
          testResultVisible.value = true
        } catch (error) {
          console.error('Failed to test connection:', error)
          testResult.value = {
            success: false,
            message: '连接失败: ' + (error as Error).message
          }
          testResultVisible.value = true
        }
      }
    })
  }
}

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  selectedTemplate.value = 'default'
  urlError.value = ''
  portError.value = ''
}

const handleCancel = () => {
  router.push('/data-source/list')
}
</script>

<style scoped>
/* 全局样式 */
.data-source-create {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  padding: 32px;
  position: relative;
  overflow: hidden;
}

/* 背景网格 */
.data-source-create::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 50px 50px;
  }
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 48px;
  animation: fadeInUp 0.8s ease-out;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(90deg, #4361ee, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.title-icon {
  font-size: 40px;
  color: #4361ee;
  animation: pulse 2s infinite;
}

.page-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

/* 表单卡片 */
.form-card {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 24px;
  padding: 40px;
  backdrop-filter: blur(15px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(64, 158, 255, 0.1);
  animation: fadeInUp 0.8s ease-out 0.2s both;
  position: relative;
  overflow: hidden;
}

.form-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #4361ee, #667eea, #764ba2);
  border-radius: 24px 24px 0 0;
}

/* 表单步骤指示器 */
.form-steps {
  display: flex;
  gap: 32px;
  margin-bottom: 40px;
  animation: fadeInUp 0.8s ease-out 0.3s both;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;
  flex: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(15, 23, 42, 0.8);
  border: 2px solid rgba(64, 158, 255, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 2;
}

.step-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.step-item.active .step-number {
  background: linear-gradient(135deg, #4361ee, #3a0ca3);
  border-color: #4361ee;
  color: white;
  box-shadow: 0 4px 20px rgba(67, 97, 238, 0.4);
  transform: scale(1.1);
}

.step-item.active .step-label {
  color: white;
  font-weight: 600;
}

.step-item:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  left: 50%;
  right: -50%;
  height: 2px;
  background: rgba(64, 158, 255, 0.3);
  transform: translateY(-50%);
  z-index: 1;
}

.step-item.active:not(:last-child)::after {
  background: linear-gradient(90deg, #4361ee, rgba(64, 158, 255, 0.3));
}

/* 主表单 */
.main-form {
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

/* 表单部分 */
.form-section {
  margin-bottom: 40px;
  padding: 32px;
  background: rgba(15, 23, 42, 0.6);
  border-radius: 16px;
  border: 1px solid rgba(64, 158, 255, 0.1);
  animation: slideUp 0.6s ease-out;
  position: relative;
  overflow: hidden;
}

.form-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #4361ee, #3a0ca3);
  border-radius: 16px 0 0 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  font-size: 20px;
  color: #4361ee;
}

/* 表单网格 */
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.form-item {
  animation: fadeInUp 0.6s ease-out;
}

.form-item.full-width {
  grid-column: 1 / -1;
}

/* 表单输入 */
.form-input {
  width: 100%;
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-input:focus {
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.2);
}

.form-select {
  width: 100%;
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.form-textarea {
  width: 100%;
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  resize: vertical;
  min-height: 100px;
}

.form-input-number {
  width: 100%;
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-suffix {
  margin-left: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

/* 模板选择器 */
.template-selector {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.template-btn {
  border-radius: 10px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.template-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(67, 97, 238, 0.4);
}

/* URL输入组 */
.url-input-group {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.url-btn {
  border-radius: 10px;
  background: linear-gradient(90deg, #3498db, #2980b9);
  border: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}

.url-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
}

/* 错误图标 */
.error-icon {
  color: #ef4444;
  margin-left: 8px;
  animation: shake 0.5s ease-in-out;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid rgba(64, 158, 255, 0.1);
  animation: fadeInUp 0.8s ease-out 0.6s both;
}

.action-btn {
  border-radius: 12px;
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  min-width: 120px;
}

.action-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
}

.action-btn.submit {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  color: white;
}

.action-btn.test {
  background: linear-gradient(90deg, #10b981, #059669);
  border: none;
  color: white;
}

.action-btn.reset {
  background: linear-gradient(90deg, #3498db, #2980b9);
  border: none;
  color: white;
}

.action-btn.cancel {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.3);
  color: rgba(255, 255, 255, 0.9);
}

.action-btn.cancel:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: #4361ee;
  color: white;
}

/* 测试连接结果样式 */
.test-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  text-align: center;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  margin: -24px;
  animation: fadeInUp 0.6s ease-out;
}

.test-result :deep(.el-icon) {
  font-size: 64px;
  margin-bottom: 24px;
  animation: zoomIn 0.6s ease-out;
}

.success-icon {
  color: #10b981;
}

.error-icon {
  color: #ef4444;
}

.test-result h4 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
  color: white;
}

.test-result p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  animation: fadeInUp 0.6s ease-out 0.4s both;
  max-width: 320px;
}

/* 动画 */
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

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-5px);
  }
  75% {
    transform: translateX(5px);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .form-grid {
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
  }

  .form-card {
    padding: 32px;
  }

  .form-section {
    padding: 24px;
  }

  .form-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .data-source-create {
    padding: 16px;
  }

  .page-title {
    font-size: 28px;
  }

  .form-card {
    padding: 24px;
  }

  .form-steps {
    gap: 16px;
  }

  .step-number {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }

  .step-label {
    font-size: 12px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .template-selector {
    flex-direction: column;
    align-items: stretch;
  }

  .url-input-group {
    flex-direction: column;
    align-items: stretch;
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .action-btn {
    width: 100%;
  }

  .form-section {
    padding: 20px;
  }

  .section-title {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .form-card {
    padding: 20px;
  }

  .form-steps {
    flex-direction: column;
    gap: 16px;
  }

  .step-item {
    flex-direction: row;
    justify-content: flex-start;
    align-items: center;
    gap: 12px;
  }

  .step-item:not(:last-child)::after {
    display: none;
  }

  .form-section {
    padding: 16px;
  }

  .page-subtitle {
    font-size: 14px;
  }
}

/* 表单标签样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  padding: 0 16px 0 0;
  width: 140px;
}

/* 输入框样式覆盖 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__wrapper),
:deep(.el-input-number) {
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(64, 158, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__wrapper:hover),
:deep(.el-input-number:hover) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  border-color: #4361ee;
  transform: translateY(-1px);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__wrapper.is-focus),
:deep(.el-input-number.is-focus) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.2);
  border-color: #4361ee;
  transform: translateY(-1px);
}

:deep(.el-input__inner),
:deep(.el-select__input),
:deep(.el-textarea__inner),
:deep(.el-input-number__input) {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  background: transparent;
}

:deep(.el-input__inner:focus),
:deep(.el-select__input:focus),
:deep(.el-textarea__inner:focus),
:deep(.el-input-number__input:focus) {
  color: #409eff;
}

:deep(.el-input__placeholder),
:deep(.el-select__placeholder),
:deep(.el-textarea__placeholder) {
  color: rgba(255, 255, 255, 0.5) !important;
}

:deep(.el-select-dropdown) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(64, 158, 255, 0.3);
  animation: fadeInScale 0.3s ease-out;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
}

:deep(.el-select-dropdown__item) {
  padding: 12px 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  margin: 4px 8px;
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-select-dropdown__item:hover) {
  background: rgba(67, 97, 238, 0.2);
  color: #4361ee;
  transform: translateX(8px);
}

:deep(.el-select-dropdown__item.selected) {
  background: rgba(67, 97, 238, 0.3);
  color: #4361ee;
  font-weight: 600;
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
</style>