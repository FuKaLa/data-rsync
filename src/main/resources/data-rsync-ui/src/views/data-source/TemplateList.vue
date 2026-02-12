<template>
  <div class="template-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据源模板管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleCreate">创建模板</el-button>
            <el-button @click="handleInitSystemTemplates">初始化系统模板</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <el-form :inline="true" :model="searchForm" class="mb-4">
          <el-form-item label="模板名称">
            <el-input v-model="searchForm.name" placeholder="请输入模板名称" />
          </el-form-item>
          <el-form-item label="数据源类型">
            <el-select v-model="searchForm.dataSourceType" placeholder="请选择数据源类型">
              <el-option label="MySQL" value="MySQL" />
              <el-option label="PostgreSQL" value="PostgreSQL" />
              <el-option label="Oracle" value="Oracle" />
              <el-option label="SQL Server" value="SQL Server" />
              <el-option label="MongoDB" value="MongoDB" />
              <el-option label="Redis" value="Redis" />
              <el-option label="Elasticsearch" value="Elasticsearch" />
            </el-select>
          </el-form-item>
          <el-form-item label="系统模板">
            <el-select v-model="searchForm.isSystem" placeholder="请选择">
              <el-option label="是" value="true" />
              <el-option label="否" value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 模板列表 -->
      <el-table :data="templates" style="width: 100%">
        <el-table-column prop="id" label="模板ID" width="80" />
        <el-table-column prop="name" label="模板名称" />
        <el-table-column prop="dataSourceType" label="数据源类型" />
        <el-table-column prop="driverClass" label="驱动类" />
        <el-table-column prop="defaultPort" label="默认端口" width="100" />
        <el-table-column prop="connectionTimeout" label="连接超时" width="120">
          <template #default="scope">
            {{ scope.row.connectionTimeout }}ms
          </template>
        </el-table-column>
        <el-table-column prop="isSystem" label="系统模板" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isSystem ? 'success' : 'info'">
              {{ scope.row.isSystem ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)" :disabled="scope.row.isSystem">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
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
    
    <!-- 模板编辑对话框 -->
    <el-dialog
      v-model="templateDialogVisible"
      :title="templateDialogTitle"
      width="700px"
    >
      <el-form :model="templateForm" label-width="120px">
        <el-form-item label="模板名称" required>
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="数据源类型" required>
          <el-select v-model="templateForm.dataSourceType" placeholder="请选择数据源类型">
            <el-option label="MySQL" value="MySQL" />
            <el-option label="PostgreSQL" value="PostgreSQL" />
            <el-option label="Oracle" value="Oracle" />
            <el-option label="SQL Server" value="SQL Server" />
            <el-option label="MongoDB" value="MongoDB" />
            <el-option label="Redis" value="Redis" />
            <el-option label="Elasticsearch" value="Elasticsearch" />
          </el-select>
        </el-form-item>
        <el-form-item label="驱动类" required>
          <el-input v-model="templateForm.driverClass" placeholder="请输入驱动类" />
        </el-form-item>
        <el-form-item label="日志监听方式">
          <el-select v-model="templateForm.logMonitorType" placeholder="请选择日志监听方式">
            <el-option label="文件" value="FILE" />
            <el-option label="数据库" value="DATABASE" />
            <el-option label="API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认端口" required>
          <el-input-number v-model="templateForm.defaultPort" :min="1" :max="65535" placeholder="请输入默认端口" />
        </el-form-item>
        <el-form-item label="连接超时(ms)" required>
          <el-input-number v-model="templateForm.connectionTimeout" :min="1000" placeholder="请输入连接超时时间" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="templateForm.description" placeholder="请输入模板描述" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="templateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmTemplate">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { dataSourceApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

// 模板列表
const templates = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  name: '',
  dataSourceType: '',
  isSystem: ''
})

// 模板对话框
const templateDialogVisible = ref(false)
const templateDialogTitle = ref('创建模板')
const templateForm = reactive({
  id: 0,
  name: '',
  dataSourceType: '',
  driverClass: '',
  logMonitorType: '',
  defaultPort: 0,
  connectionTimeout: 30000,
  description: '',
  isSystem: false
})

onMounted(() => {
  loadTemplates()
})

// 加载模板列表
const loadTemplates = async () => {
  try {
    const response = await dataSourceApi.getTemplates()
    templates.value = response.data || []
    total.value = templates.value.length
  } catch (error) {
    console.error('Failed to load templates:', error)
    ElMessage.error('加载模板列表失败')
  }
}

// 搜索
const handleSearch = () => {
  // 这里可以根据搜索条件过滤模板
  loadTemplates()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    dataSourceType: '',
    isSystem: ''
  })
  loadTemplates()
}

// 创建模板
const handleCreate = () => {
  Object.assign(templateForm, {
    id: 0,
    name: '',
    dataSourceType: '',
    driverClass: '',
    logMonitorType: '',
    defaultPort: 0,
    connectionTimeout: 30000,
    description: '',
    isSystem: false
  })
  templateDialogTitle.value = '创建模板'
  templateDialogVisible.value = true
}

// 编辑模板
const handleEdit = (row: any) => {
  Object.assign(templateForm, row)
  templateDialogTitle.value = '编辑模板'
  templateDialogVisible.value = true
}

// 删除模板
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await dataSourceApi.deleteTemplate(id)
    await loadTemplates()
    ElMessage.success('模板删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete template:', error)
      ElMessage.error('删除模板失败')
    }
  }
}

// 初始化系统模板
const handleInitSystemTemplates = async () => {
  try {
    await ElMessageBox.confirm('确定要初始化系统模板吗？这将创建默认的系统模板', '初始化确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await dataSourceApi.initSystemTemplates()
    await loadTemplates()
    ElMessage.success('系统模板初始化成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to init system templates:', error)
      ElMessage.error('初始化系统模板失败')
    }
  }
}

// 确认模板操作
const confirmTemplate = async () => {
  try {
    if (templateForm.id === 0) {
      // 创建模板
      const response = await dataSourceApi.createTemplate(templateForm)
      ElMessage.success('模板创建成功')
    } else {
      // 更新模板
      const response = await dataSourceApi.updateTemplate(templateForm.id, templateForm)
      ElMessage.success('模板更新成功')
    }
    templateDialogVisible.value = false
    await loadTemplates()
  } catch (error) {
    console.error('Failed to save template:', error)
    ElMessage.error('保存模板失败')
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadTemplates()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  loadTemplates()
}
</script>

<style scoped>
.template-list {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.header-actions :deep(.el-button--primary) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border: none;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  color: #ffffff;
}

.header-actions :deep(.el-button--primary::before) {
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

.header-actions :deep(.el-button--primary:active::before) {
  width: 300px;
  height: 300px;
}

.header-actions :deep(.el-button--primary:hover) {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.5);
}

/* 搜索筛选区域 */
.search-filter {
  margin-bottom: 24px;
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

:deep(.el-form) {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

:deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select-dropdown) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
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

/* 表格样式 */
:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.8s ease-out 0.4s both;
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

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 32px;
  padding: 24px;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(12px);
  animation: slideUp 0.8s ease-out 0.6s both;
  border: 1px solid rgba(59, 130, 246, 0.2);
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
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination__item:hover) {
  color: #3b82f6;
  border-color: #3b82f6;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  background: rgba(59, 130, 246, 0.1);
}

:deep(.el-pagination__item.is-active) {
  background: linear-gradient(90deg, #3b82f6, #a855f7);
  border-color: transparent;
  color: #ffffff;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

:deep(.el-pagination__prev),
:deep(.el-pagination__next) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination__prev:hover),
:deep(.el-pagination__next:hover) {
  border-color: #3b82f6;
  color: #3b82f6;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
  background: rgba(59, 130, 246, 0.1);
}

:deep(.el-pagination__sizes) {
  margin-right: 16px;
}

:deep(.el-pagination__sizes .el-select .el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-pagination__sizes .el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
  border-color: #3b82f6;
}

:deep(.el-pagination__sizes .el-select .el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
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

:deep(.el-dialog__body .el-form) {
  background: transparent;
  padding: 0;
  box-shadow: none;
  border: none;
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

:deep(.el-button:disabled) {
  opacity: 0.5;
  transform: none;
  box-shadow: none;
  background: rgba(15, 23, 42, 0.6) !important;
  color: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid rgba(59, 130, 246, 0.2) !important;
}

:deep(.el-button:disabled:hover) {
  transform: none;
  box-shadow: none;
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
  
  .header-actions :deep(.el-button) {
    padding: 10px 20px;
    font-size: 14px;
  }
}

@media (max-width: 992px) {
  .template-list {
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
  
  :deep(.el-form) {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 0 16px;
  }
  
  .header-actions {
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