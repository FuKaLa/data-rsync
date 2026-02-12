<template>
  <div class="data-source-detail">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon class="title-icon"><DataAnalysis /></el-icon>
        数据源详情
      </h1>
      <p class="page-subtitle">查看数据源的详细信息、元数据和表结构</p>
    </div>

    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/data-source/list' }">数据源管理</el-breadcrumb-item>
      <el-breadcrumb-item>{{ dataSource?.name || '数据源详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading loading-spinner"><Loading /></el-icon>
      <p>正在加载数据源详情...</p>
    </div>

    <!-- 数据源不存在 -->
    <div v-else-if="!dataSource" class="error-container">
      <el-icon class="error-icon"><Close /></el-icon>
      <h3>数据源不存在</h3>
      <p>请检查数据源ID是否正确</p>
      <el-button type="primary" @click="goBack">返回数据源列表</el-button>
    </div>

    <!-- 数据源详情内容 -->
    <div v-else class="detail-content">
      <!-- 基本信息卡片 -->
      <el-card class="base-info-card" shadow="hover">
        <template #header>
          <div class="card-header-content">
            <h2 class="card-title">{{ dataSource.name }}</h2>
            <el-tag :type="getTypeTagType(dataSource.type)" class="type-tag">{{ getTypeName(dataSource.type) }}</el-tag>
            <el-switch
              v-model="dataSource.enabled"
              @change="handleEnableChange"
              active-color="#4361ee"
              inactive-color="#6b7280"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              size="small"
            />
          </div>
        </template>
        <div class="base-info-grid">
          <div class="info-item">
            <span class="info-label">数据源类型:</span>
            <span class="info-value">{{ getTypeName(dataSource.type) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">连接地址:</span>
            <span class="info-value">{{ dataSource.url }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">用户名:</span>
            <span class="info-value">{{ dataSource.username }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">健康状态:</span>
            <span class="info-value" :class="dataSource.healthStatus.toLowerCase()">
              {{ getHealthStatusText(dataSource.healthStatus) }}
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间:</span>
            <span class="info-value">{{ dataSource.createTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">更新时间:</span>
            <span class="info-value">{{ dataSource.updateTime }}</span>
          </div>
        </div>
      </el-card>

      <!-- 标签页导航 -->
      <el-tabs v-model="activeTab" class="detail-tabs" type="border-card">
        <el-tab-pane label="元数据" name="metadata">
          <div class="tab-content">
            <h3>元数据信息</h3>
            <p>显示数据源的元数据信息，包括数据库版本、字符集、 collation 等。</p>
            <div v-if="metadata" class="metadata-grid">
              <div v-for="(value, key) in metadata" :key="key" class="metadata-item">
                <span class="metadata-key">{{ key }}:</span>
                <span class="metadata-value">{{ value }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="表结构" name="tables">
          <div class="tab-content">
            <h3>表结构</h3>
            <p>显示数据源中的表结构信息。</p>
            <div class="table-filter">
              <el-input
                v-model="tableSearch"
                placeholder="搜索表"
                clearable
                @input="handleTableSearch"
              >
                <template #prefix>
                  <el-icon class="search-icon"><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="refreshTables">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
            <div v-if="loadingTables" class="loading-tables">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>正在加载表结构...</span>
            </div>
            <div v-else-if="tables.length === 0" class="empty-tables">
              <el-icon><DocumentRemove /></el-icon>
              <p>暂无表结构信息</p>
              <el-button type="primary" @click="refreshTables">刷新</el-button>
            </div>
            <div v-else class="tables-grid">
              <el-card
                v-for="table in filteredTables"
                :key="table.name"
                class="table-card"
                shadow="hover"
              >
                <template #header>
                  <div class="table-header">
                    <h4 class="table-name">{{ table.name }}</h4>
                    <el-tag :type="getTableStatusType(table.status)" class="table-status-tag">
                      {{ getTableStatusText(table.status) }}
                    </el-tag>
                  </div>
                </template>
                <div class="table-info">
                  <div class="table-meta">
                    <span class="meta-item">
                      <el-icon><DataAnalysis /></el-icon>
                      列数: {{ table.columnCount }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Timer /></el-icon>
                      行数: {{ table.rowCount }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Calendar /></el-icon>
                      最后更新: {{ table.lastUpdated }}
                    </span>
                  </div>
                  <div class="table-actions">
                    <el-button size="small" @click="viewTableStructure(table)">
                      <el-icon><View /></el-icon>
                      查看结构
                    </el-button>
                    <el-button size="small" @click="viewTableData(table)">
                      <el-icon><DataAnalysis /></el-icon>
                      查看数据
                    </el-button>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="数据同步" name="sync">
          <div class="tab-content">
            <h3>数据同步状态</h3>
            <p>显示数据源的数据同步情况，包括最近同步时间、同步状态等。</p>
            <el-card class="sync-status-card" shadow="hover">
              <template #header>
                <div class="card-header-content">
                  <h4 class="card-title">同步状态</h4>
                  <el-button type="primary" @click="startSync">
                    <el-icon><Play /></el-icon>
                    开始同步
                  </el-button>
                </div>
              </template>
              <div class="sync-status-info">
                <div class="status-item">
                  <span class="status-label">最近同步时间:</span>
                  <span class="status-value">{{ lastSyncTime || '从未同步' }}</span>
                </div>
                <div class="status-item">
                  <span class="status-label">同步状态:</span>
                  <span class="status-value" :class="syncStatus.toLowerCase()">
                    {{ syncStatus }}
                  </span>
                </div>
                <div class="status-item">
                  <span class="status-label">同步记录数:</span>
                  <span class="status-value">{{ syncRecordCount || 0 }}</span>
                </div>
                <div class="status-item">
                  <span class="status-label">同步失败数:</span>
                  <span class="status-value">{{ syncFailureCount || 0 }}</span>
                </div>
              </div>
              <div class="sync-history">
                <h4>同步历史</h4>
                <el-table :data="syncHistory" style="width: 100%">
                  <el-table-column prop="syncTime" label="同步时间" width="180" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'">
                        {{ scope.row.status === 'SUCCESS' ? '成功' : '失败' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="recordCount" label="同步记录数" width="120" />
                  <el-table-column prop="duration" label="耗时(ms)" width="100" />
                  <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
                </el-table>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 表结构详情对话框 -->
    <el-dialog
      v-model="tableStructureDialogVisible"
      :title="`表结构 - ${selectedTable?.name}`"
      width="80%"
      destroy-on-close
    >
      <div v-if="loadingTableStructure" class="loading-structure">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载表结构...</span>
      </div>
      <div v-else-if="selectedTable" class="table-structure">
        <h3>{{ selectedTable.name }}</h3>
        <p class="table-description">{{ selectedTable.description || '无描述' }}</p>
        <el-table :data="selectedTable.columns" style="width: 100%">
          <el-table-column prop="name" label="列名" width="180" />
          <el-table-column prop="type" label="数据类型" width="150" />
          <el-table-column prop="length" label="长度" width="100" />
          <el-table-column prop="nullable" label="可空" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.nullable ? 'success' : 'danger'">
                {{ scope.row.nullable ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="defaultValue" label="默认值" />
          <el-table-column prop="comment" label="备注" show-overflow-tooltip />
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="tableStructureDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 表数据查看对话框 -->
    <el-dialog
      v-model="tableDataDialogVisible"
      :title="`表数据 - ${selectedTable?.name}`"
      width="80%"
      destroy-on-close
    >
      <div v-if="loadingTableData" class="loading-data">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在加载表数据...</span>
      </div>
      <div v-else-if="selectedTable" class="table-data-content">
        <div class="table-data-header">
          <h3>{{ selectedTable.name }}</h3>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="tableDataTotal"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
        <el-table :data="tableData" style="width: 100%">
          <el-table-column
            v-for="column in tableColumns"
            :key="column.name"
            :prop="column.name"
            :label="column.name"
          />
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="tableDataDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { dataSourceApi } from '@/api'
import { DataAnalysis, Loading, Close, Refresh, Search, View, Play } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const dataSource = ref<any>(null)
const loading = ref(true)
const activeTab = ref('metadata')
const metadata = ref<any>(null)
const tables = ref<any[]>([])
const loadingTables = ref(false)
const tableSearch = ref('')
const syncHistory = ref<any[]>([])
const lastSyncTime = ref<string | null>(null)
const syncStatus = ref('未知')
const syncRecordCount = ref<number | null>(null)
const syncFailureCount = ref<number | null>(null)
const loadingTableStructure = ref(false)
const selectedTable = ref<any>(null)
const tableStructureDialogVisible = ref(false)
const tableDataDialogVisible = ref(false)
const loadingTableData = ref(false)
const tableData = ref<any[]>([])
const tableColumns = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const tableDataTotal = ref(0)

// 计算属性
const filteredTables = computed(() => {
  if (!tableSearch.value) return tables.value
  return tables.value.filter(table => 
    table.name.toLowerCase().includes(tableSearch.value.toLowerCase())
  )
})

// 方法
const getDataSourceId = () => {
  return route.query.id as string
}

const loadDataSourceDetail = async () => {
  const id = getDataSourceId()
  if (!id) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const response = await dataSourceApi.getDetail(Number(id))
    dataSource.value = response.data
    loadMetadata()
    loadTables()
    loadSyncHistory()
  } catch (error) {
    console.error('Failed to load data source detail:', error)
  } finally {
    loading.value = false
  }
}

const loadMetadata = async () => {
  if (!dataSource.value) return
  try {
    const response = await dataSourceApi.getMetadata(Number(getDataSourceId()))
    metadata.value = response.data
  } catch (error) {
    console.error('Failed to load metadata:', error)
  }
}

const loadTables = async () => {
  if (!dataSource.value) return
  loadingTables.value = true
  try {
    const response = await dataSourceApi.getTables(Number(getDataSourceId()))
    tables.value = response.data
  } catch (error) {
    console.error('Failed to load tables:', error)
    tables.value = []
  } finally {
    loadingTables.value = false
  }
}

const loadSyncHistory = async () => {
  if (!dataSource.value) return
  try {
    const response = await dataSourceApi.getSyncHistory(Number(getDataSourceId()))
    syncHistory.value = response.data
    if (syncHistory.value.length > 0) {
      const lastSync = syncHistory.value[0]
      lastSyncTime.value = lastSync.syncTime
      syncStatus.value = lastSync.status === 'SUCCESS' ? '成功' : '失败'
      syncRecordCount.value = lastSync.recordCount
      syncFailureCount.value = syncHistory.value.filter((item: any) => item.status === 'FAILURE').length
    }
  } catch (error) {
    console.error('Failed to load sync history:', error)
  }
}

const handleEnableChange = async (enabled: boolean) => {
  if (!dataSource.value) return
  try {
    await dataSourceApi.enable(dataSource.value.id, enabled)
    // 无需刷新，因为我们已经更新了本地状态
  } catch (error) {
    console.error('Failed to change enable status:', error)
    // 恢复原来的状态
    dataSource.value.enabled = !enabled
  }
}

const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    'MYSQL': 'info',
    'POSTGRESQL': 'success',
    'ORACLE': 'warning',
    'SQL_SERVER': 'danger',
    'MONGODB': 'primary',
    'REDIS': 'success'
  }
  return types[type] || 'info'
}

const getTypeName = (type: string) => {
  const names: Record<string, string> = {
    'MYSQL': 'MySQL',
    'POSTGRESQL': 'PostgreSQL',
    'ORACLE': 'Oracle',
    'SQL_SERVER': 'SQL Server',
    'MONGODB': 'MongoDB',
    'REDIS': 'Redis'
  }
  return names[type] || type
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

const handleTableSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

const refreshTables = () => {
  loadTables()
}

const viewTableStructure = (table: any) => {
  selectedTable.value = table
  loadingTableStructure.value = true
  // 模拟加载表结构
  setTimeout(() => {
    // 模拟表结构数据
    selectedTable.value.columns = [
      { name: 'id', type: 'INT', length: 11, nullable: false, defaultValue: null, comment: '主键' },
      { name: 'name', type: 'VARCHAR', length: 255, nullable: false, defaultValue: null, comment: '名称' },
      { name: 'age', type: 'INT', length: 11, nullable: true, defaultValue: 0, comment: '年龄' },
      { name: 'created_at', type: 'TIMESTAMP', length: null, nullable: false, defaultValue: 'CURRENT_TIMESTAMP', comment: '创建时间' }
    ]
    loadingTableStructure.value = false
    tableStructureDialogVisible.value = true
  }, 1000)
}

const viewTableData = (table: any) => {
  selectedTable.value = table
  loadingTableData.value = true
  currentPage.value = 1
  // 模拟加载表数据
  setTimeout(() => {
    // 模拟表数据
    tableData.value = [
      { id: 1, name: '张三', age: 25, created_at: '2024-01-01 10:00:00' },
      { id: 2, name: '李四', age: 30, created_at: '2024-01-02 11:00:00' },
      { id: 3, name: '王五', age: 35, created_at: '2024-01-03 12:00:00' }
    ]
    tableColumns.value = [
      { name: 'id' },
      { name: 'name' },
      { name: 'age' },
      { name: 'created_at' }
    ]
    tableDataTotal.value = 3
    loadingTableData.value = false
    tableDataDialogVisible.value = true
  }, 1000)
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  // 重新加载数据
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  // 重新加载数据
}

const goBack = () => {
  router.push('/data-source/list')
}

onMounted(() => {
  loadDataSourceDetail()
})
</script>

<style scoped>
.data-source-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  padding: 32px;
  color: white;
}

.page-header {
  margin-bottom: 32px;
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(90deg, #4361ee, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.breadcrumb {
  margin-bottom: 32px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
}

.loading-spinner {
  font-size: 48px;
  color: #4361ee;
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
}

.error-icon {
  font-size: 48px;
  color: #ef4444;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.base-info-card {
  margin-bottom: 32px;
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.type-tag {
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 600;
}

.base-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  min-width: 120px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.detail-tabs {
  margin-bottom: 32px;
}

.tab-content {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 16px;
  padding: 32px;
  backdrop-filter: blur(15px);
}

.tab-content h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: white;
}

.tab-content p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 24px;
}

.table-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-icon {
  color: rgba(255, 255, 255, 0.7);
}

.loading-tables {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 24px 0;
  color: rgba(255, 255, 255, 0.7);
}

.empty-tables {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  gap: 16px;
  color: rgba(255, 255, 255, 0.7);
}

.tables-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.table-card {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
}

.table-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(64, 158, 255, 0.4);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.table-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.table-status-tag {
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
}

.table-info {
  margin-bottom: 16px;
}

.table-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.table-actions {
  display: flex;
  gap: 12px;
}

.sync-status-card {
  margin-bottom: 32px;
}

.sync-status-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.status-label {
  color: rgba(255, 255, 255, 0.7);
  min-width: 100px;
}

.status-value {
  font-weight: 600;
  color: white;
}

.sync-history {
  margin-top: 24px;
}

.loading-structure,
.loading-data {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 24px 0;
  color: rgba(255, 255, 255, 0.7);
}

.table-structure {
  margin-top: 24px;
}

.table-description {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 24px;
}

.empty-tables {
  text-align: center;
}

.metadata-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.metadata-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(15, 23, 42, 0.6);
  border-radius: 10px;
  border: 1px solid rgba(64, 158, 255, 0.1);
}

.metadata-key {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.metadata-value {
  font-size: 14px;
  font-weight: 600;
  color: white;
}

@media (max-width: 768px) {
  .data-source-detail {
    padding: 16px;
  }

  .base-info-grid {
    grid-template-columns: 1fr;
  }

  .tables-grid {
    grid-template-columns: 1fr;
  }

  .sync-status-info {
    grid-template-columns: 1fr;
  }

  .metadata-grid {
    grid-template-columns: 1fr;
  }
}
</style>
