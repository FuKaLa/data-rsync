<template>
  <div class="dict-item-list">
    <div class="page-header">
      <h2>字典项管理</h2>
      <el-button type="primary" @click="handleAdd">新增</el-button>
    </div>
    
    <el-card>
      <div class="search-form">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="字典类型">
            <el-select v-model="searchForm.typeId" placeholder="请选择字典类型">
              <el-option 
                v-for="type in dictTypeList" 
                :key="type.id" 
                :label="type.typeName" 
                :value="type.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="字典项编码">
            <el-input v-model="searchForm.itemCode" placeholder="请输入字典项编码" />
          </el-form-item>
          <el-form-item label="字典项名称">
            <el-input v-model="searchForm.itemName" placeholder="请输入字典项名称" />
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
      
      <el-table :data="dictItemList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="typeId" label="字典类型ID" width="120" />
        <el-table-column prop="typeCode" label="字典类型编码" />
        <el-table-column prop="itemCode" label="字典项编码" />
        <el-table-column prop="itemName" label="字典项名称" />
        <el-table-column prop="itemValue" label="字典项值" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
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
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="handleStatusChange(scope.row.id, scope.row.status === 1 ? 0 : 1)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
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
        <el-form-item label="字典类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择字典类型">
            <el-option 
              v-for="type in dictTypeList" 
              :key="type.id" 
              :label="type.typeName" 
              :value="type.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="字典项编码" prop="itemCode">
          <el-input v-model="form.itemCode" placeholder="请输入字典项编码" />
        </el-form-item>
        <el-form-item label="字典项名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入字典项名称" />
        </el-form-item>
        <el-form-item label="字典项值" prop="itemValue">
          <el-input v-model="form.itemValue" placeholder="请输入字典项值" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入描述" />
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
}

interface DictItem {
  id: number
  typeId: number
  typeCode: string
  itemCode: string
  itemName: string
  itemValue: string
  sortOrder: number
  status: number
  description: string
  createTime: string
  updateTime: string
}

interface Pagination {
  current: number
  size: number
  total: number
}

const dictTypeList = ref<DictType[]>([])
const dictItemList = ref<DictItem[]>([])
const pagination = reactive<Pagination>({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  typeId: '',
  itemCode: '',
  itemName: '',
  status: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增字典项')
const form = reactive({
  id: 0,
  typeId: '',
  typeCode: '',
  itemCode: '',
  itemName: '',
  itemValue: '',
  sortOrder: 0,
  status: 1,
  description: ''
})

const rules = reactive({
  typeId: [{ required: true, message: '请选择字典类型', trigger: 'change' }],
  itemCode: [{ required: true, message: '请输入字典项编码', trigger: 'blur' }],
  itemName: [{ required: true, message: '请输入字典项名称', trigger: 'blur' }],
  itemValue: [{ required: true, message: '请输入字典项值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const formRef = ref()
const router = useRouter()

// 获取字典类型列表
const getDictTypeList = async () => {
  try {
    const response = await fetch('/api/dict/type/list')
    const data = await response.json()
    if (data.code === 200) {
      dictTypeList.value = data.data
    } else {
      ElMessage.error(data.msg || '获取字典类型列表失败')
    }
  } catch (error) {
    ElMessage.error('网络请求失败')
  }
}

// 获取字典项列表
const getDictItemList = async () => {
  try {
    const response = await fetch(`/api/dict/item/page?page=${pagination.current}&size=${pagination.size}&typeId=${searchForm.typeId}&itemCode=${searchForm.itemCode}&itemName=${searchForm.itemName}&status=${searchForm.status}`)
    const data = await response.json()
    if (data.code === 200) {
      dictItemList.value = data.data
      pagination.total = data.total
    } else {
      ElMessage.error(data.msg || '获取字典项列表失败')
    }
  } catch (error) {
    ElMessage.error('网络请求失败')
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增字典项'
  Object.assign(form, {
    id: 0,
    typeId: '',
    typeCode: '',
    itemCode: '',
    itemName: '',
    itemValue: '',
    sortOrder: 0,
    status: 1,
    description: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: DictItem) => {
  dialogTitle.value = '编辑字典项'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  try {
    const response = await fetch(`/api/dict/item/delete?id=${id}`, {
      method: 'DELETE'
    })
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('删除成功')
      getDictItemList()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    ElMessage.error('网络请求失败')
  }
}

// 状态变更
const handleStatusChange = async (id: number, status: number) => {
  try {
    const url = status === 1 ? `/api/dict/item/enable?id=${id}` : `/api/dict/item/disable?id=${id}`
    const response = await fetch(url, {
      method: 'PUT'
    })
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(data.msg || (status === 1 ? '启用成功' : '禁用成功'))
      getDictItemList()
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
    
    // 检查字典项编码是否存在
    const checkResponse = await fetch(`/api/dict/item/checkCode?typeId=${form.typeId}&itemCode=${form.itemCode}&excludeId=${form.id || ''}`)
    const checkData = await checkResponse.json()
    if (checkData.code === 200 && checkData.data) {
      ElMessage.error('字典项编码已存在')
      return
    }
    
    // 获取字典类型编码
    const type = dictTypeList.value.find(t => t.id === form.typeId)
    if (type) {
      form.typeCode = type.typeCode
    }
    
    const url = form.id ? '/api/dict/item/update' : '/api/dict/item/add'
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
      getDictItemList()
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
  getDictItemList()
}

// 重置
const resetForm = () => {
  Object.assign(searchForm, {
    typeId: '',
    itemCode: '',
    itemName: '',
    status: ''
  })
  pagination.current = 1
  getDictItemList()
}

// 分页
const handleSizeChange = (size: number) => {
  pagination.size = size
  getDictItemList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  getDictItemList()
}

// 初始化
onMounted(() => {
  getDictTypeList()
  getDictItemList()
})
</script>

<style scoped>
.dict-item-list {
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
