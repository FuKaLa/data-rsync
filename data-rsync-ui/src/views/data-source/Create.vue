<template>
  <div class="data-source-create">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>创建数据源</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据源类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择数据源类型" @change="handleTypeChange">
            <el-option label="MySQL" value="MYSQL" />
            <el-option label="PostgreSQL" value="POSTGRESQL" />
            <el-option label="Oracle" value="ORACLE" />
            <el-option label="SQL Server" value="SQL_SERVER" />
            <el-option label="MongoDB" value="MONGODB" />
            <el-option label="Redis" value="REDIS" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板选择">
          <el-select v-model="selectedTemplate" placeholder="选择预设模板" @change="handleTemplateChange">
            <el-option label="默认模板" value="default" />
            <el-option label="MySQL 模板" value="mysql" />
            <el-option label="PostgreSQL 模板" value="postgresql" />
            <el-option label="Oracle 模板" value="oracle" />
            <el-option label="MongoDB 模板" value="mongodb" />
          </el-select>
          <el-button type="primary" size="small" style="margin-left: 10px" @click="saveAsTemplate">保存为模板</el-button>
        </el-form-item>
        <el-form-item label="连接地址" prop="url">
          <el-input v-model="form.url" placeholder="请输入连接地址" @blur="validateUrl" />
          <el-tooltip v-if="urlError" :content="urlError" placement="right" :effect="'error'">
            <el-icon class="error-icon"><CircleCloseFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="数据库名称" prop="databaseName">
          <el-input v-model="form.databaseName" placeholder="请输入数据库名称" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input v-model="form.port" type="number" placeholder="请输入端口" @blur="validatePort" />
          <el-tooltip v-if="portError" :content="portError" placement="right" :effect="'error'">
            <el-icon class="error-icon"><CircleCloseFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="驱动类" prop="driverClass">
          <el-input v-model="form.driverClass" placeholder="请输入驱动类" />
        </el-form-item>
        <el-form-item label="日志监听方式" prop="logMonitorType">
          <el-select v-model="form.logMonitorType" placeholder="请选择日志监听方式">
            <el-option label="Binlog" value="BINLOG" />
            <el-option label="WAL" value="WAL" />
            <el-option label="CDC" value="CDC" />
            <el-option label="Polling" value="POLLING" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接超时时间" prop="connectionTimeout">
          <el-input v-model="form.connectionTimeout" type="number" placeholder="请输入连接超时时间(毫秒)" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">创建</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { dataSourceApi } from '@/api'
import { CircleCloseFilled } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref<any>(null)
const selectedTemplate = ref('default')
const urlError = ref('')
const portError = ref('')

const form = reactive({
  name: '',
  type: '',
  url: '',
  port: '',
  databaseName: '',
  driverClass: '',
  logMonitorType: '',
  connectionTimeout: 30000,
  username: '',
  password: '',
  enabled: true,
  description: ''
})

const rules = reactive({
  name: [
    { required: true, message: '请输入数据源名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择数据源类型', trigger: 'change' }
  ],
  url: [
    { required: true, message: '请输入连接地址', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
})

// 预设模板配置
const templates = {
  mysql: {
    driverClass: 'com.mysql.cj.jdbc.Driver',
    logMonitorType: 'BINLOG',
    connectionTimeout: 30000,
    port: '3306'
  },
  postgresql: {
    driverClass: 'org.postgresql.Driver',
    logMonitorType: 'WAL',
    connectionTimeout: 30000,
    port: '5432'
  },
  oracle: {
    driverClass: 'oracle.jdbc.OracleDriver',
    logMonitorType: 'CDC',
    connectionTimeout: 60000,
    port: '1521'
  },
  mongodb: {
    driverClass: 'mongodb.driver.MongoDriver',
    logMonitorType: 'CDC',
    connectionTimeout: 30000,
    port: '27017'
  }
}

const handleTypeChange = (type: string) => {
  // 根据数据源类型自动选择对应模板
  if (type === 'MYSQL') {
    selectedTemplate.value = 'mysql'
  } else if (type === 'POSTGRESQL') {
    selectedTemplate.value = 'postgresql'
  } else if (type === 'ORACLE') {
    selectedTemplate.value = 'oracle'
  } else if (type === 'MONGODB') {
    selectedTemplate.value = 'mongodb'
  }
  handleTemplateChange(selectedTemplate.value)
}

const handleTemplateChange = (template: string) => {
  if (templates[template as keyof typeof templates]) {
    const templateConfig = templates[template as keyof typeof templates]
    Object.assign(form, templateConfig)
  }
}

const validateUrl = () => {
  const url = form.url
  if (!url) {
    urlError.value = ''
    return
  }
  
  // 简单的URL格式校验
  const urlPattern = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/
  if (!urlPattern.test(url)) {
    urlError.value = '连接地址格式不正确'
  } else {
    urlError.value = ''
  }
}

const validatePort = () => {
  const port = form.port
  if (!port) {
    portError.value = ''
    return
  }
  
  const portNum = parseInt(port)
  if (isNaN(portNum) || portNum < 1 || portNum > 65535) {
    portError.value = '端口必须是1-65535之间的数字'
  } else {
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
          const result = await dataSourceApi.create(form)
          console.log('Create result:', result)
          router.push('/data-source/list')
        } catch (error) {
          console.error('Failed to create data source:', error)
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
.data-source-create {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-icon {
  color: #f56c6c;
  margin-left: 5px;
}
</style>