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
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

.diagnose-report {
  padding: 10px;
}

.diagnose-item {
  margin-bottom: 15px;
}

.quick-fix {
  padding: 10px;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.success {
  color: #67c23a;
}

.error {
  color: #f56c6c;
}
</style>