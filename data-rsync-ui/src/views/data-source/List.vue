<template>
  <div class="data-source-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据源列表</span>
          <el-button type="primary" @click="handleCreate">创建数据源</el-button>
        </div>
      </template>
      <el-table :data="dataSources" style="width: 100%">
        <el-table-column prop="name" label="数据源名称" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="url" label="连接地址" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="enabled" label="启用状态">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" @change="handleEnableChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="healthStatus" label="健康状态">
          <template #default="scope">
            <el-tooltip :content="getHealthTooltip(scope.row)" placement="top">
              <el-tag :type="getHealthStatusType(scope.row.healthStatus)">
                {{ getHealthStatusText(scope.row.healthStatus) }}
              </el-tag>
            </el-tooltip>
            <el-button 
              v-if="scope.row.healthStatus !== 'HEALTHY'" 
              size="small" 
              type="info" 
              style="margin-left: 8px"
              @click="handleQuickFix(scope.row)"
            >
              快速修复
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            <el-button size="small" @click="handleTestConnection(scope.row.id)">测试连接</el-button>
            <el-button size="small" type="primary" @click="handleDiagnose(scope.row.id)">一键诊断</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination" style="margin-top: 20px">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 诊断报告对话框 -->
    <el-dialog
      v-model="diagnoseDialogVisible"
      title="数据源诊断报告"
      width="800px"
    >
      <div v-if="diagnoseReport" class="diagnose-report">
        <h4>诊断结果: <span :class="diagnoseReport.overallStatus === 'SUCCESS' ? 'success' : 'error'">{{ diagnoseReport.overallStatus }}</span></h4>
        <el-divider />
        <div class="diagnose-item" v-for="item in diagnoseReport.details" :key="item.type">
          <h5>{{ getItemTitle(item.type) }}</h5>
          <el-tag :type="item.status === 'SUCCESS' ? 'success' : 'danger'" style="margin-right: 10px">{{ item.status }}</el-tag>
          <span v-if="item.message">{{ item.message }}</span>
        </div>
      </div>
      <div v-else class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在诊断中...</span>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="diagnoseDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 快速修复对话框 -->
    <el-dialog
      v-model="quickFixDialogVisible"
      title="快速修复"
      width="600px"
    >
      <div v-if="currentDataSource" class="quick-fix">
        <h4>问题分析</h4>
        <p>{{ quickFixSuggestion }}</p>
        <el-divider />
        <h4>修复建议</h4>
        <ul>
          <li v-for="(suggestion, index) in quickFixSuggestions" :key="index">{{ suggestion }}</li>
        </ul>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="quickFixDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApplyFix">应用修复</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dataSourceApi } from '@/api'
import { Loading } from '@element-plus/icons-vue'

const router = useRouter()
const dataSources = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 诊断相关
const diagnoseDialogVisible = ref(false)
const diagnoseReport = ref<any>(null)

// 快速修复相关
const quickFixDialogVisible = ref(false)
const currentDataSource = ref<any>(null)
const quickFixSuggestion = ref('')
const quickFixSuggestions = ref<string[]>([])

onMounted(() => {
  loadDataSourceList()
})

const loadDataSourceList = async () => {
  try {
    const response = await dataSourceApi.getList()
    dataSources.value = response.data || []
    total.value = dataSources.value.length
  } catch (error) {
    console.error('Failed to load data sources:', error)
  }
}

const handleCreate = () => {
  router.push('/data-source/create')
}

const handleEdit = (row: any) => {
  // 这里可以跳转到编辑页面，带上id参数
  console.log('Edit data source:', row)
}

const handleDelete = async (id: number) => {
  try {
    await dataSourceApi.delete(id)
    await loadDataSourceList()
  } catch (error) {
    console.error('Failed to delete data source:', error)
  }
}

const handleTestConnection = async (id: number) => {
  try {
    const result = await dataSourceApi.testConnection(id)
    console.log('Test connection result:', result)
    // 这里可以显示测试结果
  } catch (error) {
    console.error('Failed to test connection:', error)
  }
}

const handleEnableChange = async (row: any) => {
  try {
    // 这里可以调用启用/禁用接口
    console.log('Enable change:', row)
  } catch (error) {
    console.error('Failed to change enable status:', error)
  }
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadDataSourceList()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  loadDataSourceList()
}

// 健康状态相关方法
const getHealthStatusType = (status: string) => {
  switch (status) {
    case 'HEALTHY':
      return 'success'
    case 'UNSTABLE':
      return 'warning'
    case 'UNHEALTHY':
      return 'danger'
    default:
      return 'info'
  }
}

const getHealthStatusText = (status: string) => {
  switch (status) {
    case 'HEALTHY':
      return '正常'
    case 'UNSTABLE':
      return '连接不稳定'
    case 'UNHEALTHY':
      return '断开'
    default:
      return status
  }
}

const getHealthTooltip = (row: any) => {
  if (row.healthStatus === 'HEALTHY') {
    return '连接正常'
  } else if (row.healthStatus === 'UNSTABLE') {
    return '连接不稳定，可能存在网络波动'
  } else if (row.healthStatus === 'UNHEALTHY') {
    return `连接断开，最近一次失败原因: ${row.lastFailureReason || '未知'}`
  }
  return '未知状态'
}

// 诊断相关方法
const handleDiagnose = async (id: number) => {
  diagnoseDialogVisible.value = true
  diagnoseReport.value = null
  
  try {
    // 模拟诊断过程
    setTimeout(() => {
      diagnoseReport.value = {
        overallStatus: 'SUCCESS',
        details: [
          {
            type: 'network',
            status: 'SUCCESS',
            message: '网络连通性正常'
          },
          {
            type: 'authentication',
            status: 'SUCCESS',
            message: '账号权限正常'
          },
          {
            type: 'logMonitor',
            status: 'SUCCESS',
            message: '日志监听端口可用'
          },
          {
            type: 'connection',
            status: 'SUCCESS',
            message: '数据库连接正常'
          }
        ]
      }
    }, 1500)
  } catch (error) {
    console.error('Failed to diagnose data source:', error)
    diagnoseDialogVisible.value = false
  }
}

const getItemTitle = (type: string) => {
  const titles: Record<string, string> = {
    network: '网络连通性',
    authentication: '账号权限',
    logMonitor: '日志监听端口',
    connection: '数据库连接'
  }
  return titles[type] || type
}

// 快速修复相关方法
const handleQuickFix = (dataSource: any) => {
  currentDataSource.value = dataSource
  quickFixDialogVisible.value = true
  
  // 生成修复建议
  generateQuickFixSuggestions(dataSource)
}

const generateQuickFixSuggestions = (dataSource: any) => {
  quickFixSuggestion.value = '根据健康状态分析，发现以下问题：'
  quickFixSuggestions.value = []
  
  if (dataSource.healthStatus === 'UNHEALTHY') {
    quickFixSuggestions.value.push('检查网络连接是否正常')
    quickFixSuggestions.value.push('验证数据库账号密码是否正确')
    quickFixSuggestions.value.push('确认数据库服务是否正常运行')
    quickFixSuggestions.value.push('检查防火墙是否阻止了连接')
  } else if (dataSource.healthStatus === 'UNSTABLE') {
    quickFixSuggestions.value.push('检查网络稳定性，可能存在丢包')
    quickFixSuggestions.value.push('考虑增加连接超时时间')
    quickFixSuggestions.value.push('检查数据库服务器负载是否过高')
  }
}

const handleApplyFix = () => {
  // 应用修复建议的逻辑
  console.log('Applying quick fix for data source:', currentDataSource.value.name)
  quickFixDialogVisible.value = false
  // 可以在这里调用修复接口
}
</script>

<style scoped>
.data-source-list {
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

.card-header :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
  position: relative;
  overflow: hidden;
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
  box-shadow: 0 8px 30px rgba(67, 97, 238, 0.5);
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  margin-top: 24px;
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

:deep(.el-table__header-wrapper) {
  background: linear-gradient(90deg, #f8fafc 0%, #e2e8f0 100%);
  border-bottom: 2px solid #4361ee;
}

:deep(.el-table__header th) {
  font-weight: 700;
  color: #1e293b;
  background: transparent;
  border-bottom: none;
  padding: 18px 16px;
  font-size: 14px;
  position: relative;
}

:deep(.el-table__header th::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 16px;
  right: 16px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #4361ee, transparent);
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
  background: rgba(67, 97, 238, 0.08) !important;
  transform: translateX(8px) scale(1.01);
  box-shadow: 0 4px 20px rgba(67, 97, 238, 0.2);
}

:deep(.el-table__row.el-table__row--striped) {
  background: rgba(67, 97, 238, 0.02) !important;
}

:deep(.el-table__cell) {
  padding: 18px 16px;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  color: #334155;
}

:deep(.el-table__cell:hover) {
  background: rgba(67, 97, 238, 0.05);
  transform: scale(1.02);
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 32px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(12px);
  animation: slideUp 0.8s ease-out 0.4s both;
}

:deep(.el-pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-pagination__item) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 6px 12px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #e2e8f0;
}

:deep(.el-pagination__item:hover) {
  color: #4361ee;
  border-color: #4361ee;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.2);
}

:deep(.el-pagination__item.is-active) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border-color: transparent;
  color: #fff;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.4);
}

:deep(.el-pagination__prev),
:deep(.el-pagination__next) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e2e8f0;
}

:deep(.el-pagination__prev:hover),
:deep(.el-pagination__next:hover) {
  border-color: #4361ee;
  color: #4361ee;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.2);
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  padding: 8px 16px;
  font-size: 12px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

:deep(.el-button--primary) {
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border: none;
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

:deep(.el-button--success) {
  background: linear-gradient(90deg, #2ecc71, #27ae60);
  border: none;
  color: #fff;
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

:deep(.el-tag::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

:deep(.el-tag:hover::before) {
  left: 100%;
}

:deep(.el-tag:hover) {
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

:deep(.el-tag--success) {
  background: linear-gradient(90deg, #2ecc71, #27ae60);
  border: none;
}

:deep(.el-tag--warning) {
  background: linear-gradient(90deg, #f59e0b, #d97706);
  border: none;
}

:deep(.el-tag--danger) {
  background: linear-gradient(90deg, #dc2626, #b91c1c);
  border: none;
}

:deep(.el-tag--info) {
  background: linear-gradient(90deg, #3b82f6, #2563eb);
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

/* 诊断报告样式 */
.diagnose-report {
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 16px;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.05);
  animation: fadeInUp 0.6s ease-out;
}

.diagnose-report h4 {
  margin-bottom: 24px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 12px;
}

.diagnose-report h4 span {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 14px;
}

.diagnose-item {
  margin-bottom: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(12px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 4px solid transparent;
}

.diagnose-item:hover {
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
  transform: translateY(-3px);
  border-left-color: #4361ee;
}

.diagnose-item h5 {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 12px;
}

.diagnose-item span {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

/* 快速修复样式 */
.quick-fix {
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 16px;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.05);
  animation: fadeInUp 0.6s ease-out;
}

.quick-fix h4 {
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  position: relative;
  padding-bottom: 12px;
}

.quick-fix h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 4px;
  background: linear-gradient(90deg, #4361ee, #3a0ca3);
  border-radius: 2px;
  animation: expandWidth 0.8s ease-out 0.3s forwards;
  transform: scaleX(0);
  transform-origin: left;
}

.quick-fix p {
  margin-bottom: 24px;
  color: #64748b;
  line-height: 1.6;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.8);
  padding: 16px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.quick-fix ul {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.8);
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.quick-fix li {
  margin-bottom: 12px;
  padding-left: 24px;
  position: relative;
  color: #64748b;
  line-height: 1.5;
  font-size: 14px;
  transition: all 0.3s ease;
}

.quick-fix li:hover {
  transform: translateX(8px);
  color: #3a0ca3;
}

.quick-fix li::before {
  content: '▶';
  position: absolute;
  left: 0;
  color: #4361ee;
  font-weight: bold;
  font-size: 12px;
  transition: all 0.3s ease;
}

.quick-fix li:hover::before {
  transform: translateX(4px);
  color: #3a0ca3;
}

/* 加载样式 */
.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 16px;
  animation: fadeInUp 0.6s ease-out;
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
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-dialog__close:hover) {
  transform: scale(1.2) rotate(90deg);
  color: #f1f5f9;
}

:deep(.el-dialog__body) {
  padding: 32px;
  background: #f8fafc;
}

:deep(.el-dialog__footer) {
  padding: 24px;
  background: #f1f5f9;
  border-top: 1px solid #e2e8f0;
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
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
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

/* 分割线样式 */
:deep(.el-divider) {
  margin: 24px 0;
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

:deep(.el-divider__text) {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  background: #f8fafc;
  padding: 0 20px;
}

/* 成功和错误文本样式 */
.success {
  color: #10b981;
  font-weight: 600;
  background: rgba(16, 185, 129, 0.1);
  padding: 4px 12px;
  border-radius: 16px;
}

.error {
  color: #ef4444;
  font-weight: 600;
  background: rgba(239, 68, 68, 0.1);
  padding: 4px 12px;
  border-radius: 16px;
}

/* 动画效果 */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 5px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 5px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid #f1f5f9;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
  transform: scale(1.1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  :deep(.el-table__cell) {
    padding: 14px 12px;
    font-size: 13px;
  }
  
  :deep(.el-table__header th) {
    padding: 14px 12px;
    font-size: 13px;
  }
  
  :deep(.el-button) {
    padding: 6px 12px;
    font-size: 11px;
    margin-right: 4px;
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
  .data-source-list {
    padding: 16px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  :deep(.el-table__cell) {
    padding: 12px 8px;
  }
  
  .pagination {
    padding: 20px;
  }
  
  .diagnose-report,
  .quick-fix {
    padding: 20px;
  }
  
  :deep(.el-dialog__body) {
    padding: 24px;
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
    padding: 4px 8px;
    font-size: 10px;
    margin-right: 2px;
  }
  
  .pagination {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  :deep(.el-dialog) {
    width: 95% !important;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}
</style>