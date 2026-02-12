<template>
  <div class="dict-type-list">
    <div class="page-header">
      <h2>字典类型管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    
    <el-card>
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="类型编码">
            <el-input v-model="searchForm.typeCode" placeholder="请输入类型编码" />
          </el-form-item>
          <el-form-item label="类型名称">
            <el-input v-model="searchForm.typeName" placeholder="请输入类型名称" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态">
              <el-option label="启用" value="1" />
              <el-option label="禁用" value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <el-table :data="dictTypeList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="typeCode" label="类型编码" />
        <el-table-column prop="typeName" label="类型名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '1' ? 'success' : 'danger'">
              {{ scope.row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === '1' ? 'warning' : 'success'"
              @click="handleStatusChange(scope.row.id, scope.row.status === '1' ? '0' : '1')"
            >
              {{ scope.row.status === '1' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="类型编码" prop="typeCode">
          <el-input v-model="form.typeCode" placeholder="请输入类型编码" />
        </el-form-item>
        <el-form-item label="类型名称" prop="typeName">
          <el-input v-model="form.typeName" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

interface DictType {
  id: number
  typeCode: string
  typeName: string
  description: string
  status: string
  createTime: string
  updateTime: string
}

interface Pagination {
  current: number
  size: number
  total: number
}

const dictTypeList = ref<DictType[]>([])
const pagination = reactive<Pagination>({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  typeCode: '',
  typeName: '',
  status: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增字典类型')
const form = reactive({
  id: 0,
  typeCode: '',
  typeName: '',
  description: '',
  status: '1'
})

const rules = reactive({
  typeCode: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
  typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const formRef = ref()
const router = useRouter()

// 获取字典类型列表
const getDictTypeList = async () => {
  try {
    const response = await fetch(`/api/dict/type/page?page=${pagination.current}&size=${pagination.size}&typeCode=${searchForm.typeCode}&typeName=${searchForm.typeName}&status=${searchForm.status}`)
    const data = await response.json()
    if (data.code === 200) {
      dictTypeList.value = data.data
      pagination.total = data.total
    } else {
      ElMessage.error(data.msg || '获取字典类型列表失败')
    }
  } catch (error) {
    ElMessage.error('网络请求失败')
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增字典类型'
  Object.assign(form, {
    id: 0,
    typeCode: '',
    typeName: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: DictType) => {
  dialogTitle.value = '编辑字典类型'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  try {
    const response = await fetch(`/api/dict/type/delete?id=${id}`, {
      method: 'DELETE'
    })
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(data.msg || '删除成功')
      getDictTypeList()
    } else {
      ElMessage.error(data.msg || '删除失败')
    }
  } catch (error) {
    ElMessage.error('网络请求失败')
  }
}

// 状态变更
const handleStatusChange = async (id: number, status: number) => {
  try {
    const url = status === 1 ? `/api/dict/type/enable?id=${id}` : `/api/dict/type/disable?id=${id}`
    const response = await fetch(url, {
      method: 'PUT'
    })
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(data.msg || (status === 1 ? '启用成功' : '禁用成功'))
      getDictTypeList()
    } else {
      ElMessage.error(data.msg || (status === 1 ? '启用失败' : '禁用失败'))
    }
  } catch (error) {
    ElMessage.error('网络请求失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 检查类型编码是否存在
    const checkResponse = await fetch(`/api/dict/type/checkCode?typeCode=${form.typeCode}&excludeId=${form.id || ''}`)
    const checkData = await checkResponse.json()
    if (checkData.code === 200 && checkData.data) {
      ElMessage.error('类型编码已存在')
      return
    }
    
    const url = form.id ? '/api/dict/type/update' : '/api/dict/type/add'
    const method = form.id ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(form)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(data.msg || (form.id ? '修改成功' : '新增成功'))
      dialogVisible.value = false
      getDictTypeList()
    } else {
      ElMessage.error(data.msg || (form.id ? '修改失败' : '新增失败'))
    }
  } catch (error) {
    ElMessage.error('网络请求失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getDictTypeList()
}

// 重置
const resetForm = () => {
  Object.assign(searchForm, {
    typeCode: '',
    typeName: '',
    status: ''
  })
  pagination.current = 1
  getDictTypeList()
}

// 分页
const handleSizeChange = (size: number) => {
  pagination.size = size
  getDictTypeList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  getDictTypeList()
}

// 初始化
onMounted(() => {
  getDictTypeList()
})
</script>

<style scoped>
.dict-type-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
