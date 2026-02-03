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
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
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

.error-icon {
  color: #ef4444;
  margin-left: 8px;
  font-size: 16px;
  animation: shake 0.5s ease-in-out;
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

/* 表单样式 */
:deep(.el-form) {
  margin-top: 24px;
  animation: slideUp 0.8s ease-out 0.4s both;
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

:deep(.el-form-item) {
  margin-bottom: 24px;
  animation: fadeInUp 0.6s ease-out var(--delay, 0s) both;
  position: relative;
}

:deep(.el-form-item:nth-child(1)) {
  --delay: 0.5s;
}

:deep(.el-form-item:nth-child(2)) {
  --delay: 0.6s;
}

:deep(.el-form-item:nth-child(3)) {
  --delay: 0.7s;
}

:deep(.el-form-item:nth-child(4)) {
  --delay: 0.8s;
}

:deep(.el-form-item:nth-child(5)) {
  --delay: 0.9s;
}

:deep(.el-form-item:nth-child(6)) {
  --delay: 1s;
}

:deep(.el-form-item:nth-child(7)) {
  --delay: 1.1s;
}

:deep(.el-form-item:nth-child(8)) {
  --delay: 1.2s;
}

:deep(.el-form-item:nth-child(9)) {
  --delay: 1.3s;
}

:deep(.el-form-item:nth-child(10)) {
  --delay: 1.4s;
}

:deep(.el-form-item:nth-child(11)) {
  --delay: 1.5s;
}

:deep(.el-form-item:nth-child(12)) {
  --delay: 1.6s;
}

:deep(.el-form-item:nth-child(13)) {
  --delay: 1.7s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  padding: 0 16px 0 0;
  width: 120px;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.1);
  border-color: #4361ee;
  transform: translateY(-1px);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.2);
  border-color: #4361ee;
  transform: translateY(-1px);
}

:deep(.el-input__inner),
:deep(.el-select__input),
:deep(.el-textarea__inner) {
  font-size: 14px;
  color: #334155;
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus),
:deep(.el-select__input:focus),
:deep(.el-textarea__inner:focus) {
  color: #3a0ca3;
}

:deep(.el-textarea__wrapper) {
  min-height: 100px;
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  padding: 10px 20px;
  font-size: 14px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-right: 12px;
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
  width: 300px;
  height: 300px;
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

:deep(.el-button--primary) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
}

:deep(.el-button--success) {
  background: linear-gradient(90deg, #2ecc71, #27ae60);
  border: none;
  color: #fff;
}

:deep(.el-button--info) {
  background: linear-gradient(90deg, #3498db, #2980b9);
  border: none;
  color: #fff;
}

:deep(.el-button--danger) {
  background: linear-gradient(90deg, #e74c3c, #c0392b);
  border: none;
}

/* 开关样式 */
:deep(.el-switch) {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  transform: scale(1);
}

:deep(.el-switch:hover) {
  transform: scale(1.1);
}

:deep(.el-switch__core) {
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-switch.is-checked .el-switch__core) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
}

/* 工具提示样式 */
:deep(.el-tooltip__popper) {
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

:deep(.el-tooltip__popper.is-error) {
  background: #fee2e2;
  border-color: #fca5a5;
  color: #dc2626;
}

/* 选择器样式 */
:deep(.el-select-dropdown) {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  border: none;
  animation: fadeInScale 0.3s ease-out;
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
  padding: 12px 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  margin: 4px 8px;
}

:deep(.el-select-dropdown__item:hover) {
  background: rgba(67, 97, 238, 0.1);
  color: #4361ee;
  transform: translateX(8px);
}

:deep(.el-select-dropdown__item.selected) {
  background: rgba(67, 97, 238, 0.15);
  color: #3a0ca3;
  font-weight: 600;
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .data-source-create {
    padding: 16px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__label) {
    width: 100px;
    font-size: 13px;
  }
  
  :deep(.el-button) {
    padding: 8px 16px;
    font-size: 13px;
    margin-right: 8px;
  }
}

@media (max-width: 992px) {
  :deep(.el-form-item__label) {
    width: 90px;
    font-size: 12px;
  }
  
  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    width: 100%;
  }
  
  :deep(.el-form-item__content) {
    margin-left: 100px !important;
  }
}

@media (max-width: 768px) {
  .data-source-create {
    padding: 12px;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  :deep(.el-form) {
    margin-top: 20px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
  
  :deep(.el-form-item__label) {
    width: 100%;
    text-align: left;
    margin-bottom: 8px;
  }
  
  :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }
  
  .card-header {
    padding: 0 16px;
  }
  
  .card-header span {
    font-size: 18px;
  }
  
  :deep(.el-button) {
    padding: 8px 12px;
    font-size: 12px;
    margin-right: 6px;
  }
}

/* 模板选择区域样式 */
:deep(.el-form-item:nth-child(3)) {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.el-form-item:nth-child(3) .el-form-item__content) {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

:deep(.el-form-item:nth-child(3) .el-select) {
  flex: 1;
  min-width: 200px;
}

:deep(.el-form-item:nth-child(3) .el-button) {
  white-space: nowrap;
  margin-right: 0;
}

@media (max-width: 768px) {
  :deep(.el-form-item:nth-child(3) .el-form-item__content) {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  :deep(.el-form-item:nth-child(3) .el-button) {
    width: 100%;
  }
}

/* 实时验证样式 */
:deep(.el-form-item.is-error .el-input__wrapper),
:deep(.el-form-item.is-error .el-select__wrapper),
:deep(.el-form-item.is-error .el-textarea__wrapper) {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
  border-color: #ef4444;
}

:deep(.el-form-item.is-success .el-input__wrapper),
:deep(.el-form-item.is-success .el-select__wrapper),
:deep(.el-form-item.is-success .el-textarea__wrapper) {
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
  border-color: #10b981;
}

/* 输入框聚焦动画 */
:deep(.el-input__wrapper.is-focus) {
  animation: pulse 0.6s ease-out;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(67, 97, 238, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(67, 97, 238, 0);
  }
  100% {
    box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.2);
  }
}

/* 提交按钮区域样式 */
:deep(.el-form-item:last-child) {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-start;
  gap: 16px;
}

:deep(.el-form-item:last-child .el-form-item__content) {
  display: flex;
  gap: 16px;
}

@media (max-width: 768px) {
  :deep(.el-form-item:last-child .el-form-item__content) {
    flex-direction: column;
    gap: 8px;
  }
  
  :deep(.el-form-item:last-child .el-button) {
    width: 100%;
    margin-right: 0;
  }
}
</style>