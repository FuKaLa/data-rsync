<template>
  <div class="task-create">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>创建任务</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择任务类型">
            <el-option label="全量同步" value="FULL" />
            <el-option label="增量同步" value="INCREMENTAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源类型" prop="dataSourceType">
          <el-select v-model="form.dataSourceType" placeholder="请选择数据源类型">
            <el-option label="MySQL" value="MYSQL" />
            <el-option label="PostgreSQL" value="POSTGRESQL" />
            <el-option label="Oracle" value="ORACLE" />
            <el-option label="SQL Server" value="SQL_SERVER" />
            <el-option label="MongoDB" value="MONGODB" />
            <el-option label="Redis" value="REDIS" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据库名称" prop="databaseName">
          <el-input v-model="form.databaseName" placeholder="请输入数据库名称" />
        </el-form-item>
        <el-form-item label="表名" prop="tableName">
          <el-input v-model="form.tableName" placeholder="请输入表名" />
        </el-form-item>
        <el-form-item label="主键" prop="primaryKey">
          <el-input v-model="form.primaryKey" placeholder="请输入主键" />
        </el-form-item>
        <el-form-item label="并发数" prop="concurrency">
          <el-input v-model="form.concurrency" type="number" placeholder="请输入并发数" />
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi } from '@/api'

const router = useRouter()
const formRef = ref<any>(null)

const form = reactive({
  name: '',
  type: 'FULL',
  dataSourceType: '',
  databaseName: '',
  tableName: '',
  primaryKey: 'id',
  concurrency: 1,
  description: ''
})

const rules = reactive({
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  dataSourceType: [
    { required: true, message: '请选择数据源类型', trigger: 'change' }
  ],
  databaseName: [
    { required: true, message: '请输入数据库名称', trigger: 'blur' }
  ],
  tableName: [
    { required: true, message: '请输入表名', trigger: 'blur' }
  ]
})

const handleSubmit = async () => {
  if (formRef.value) {
    await formRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          const result = await taskApi.create(form)
          console.log('Create result:', result)
          router.push('/task/list')
        } catch (error) {
          console.error('Failed to create task:', error)
        }
      }
    })
  }
}

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleCancel = () => {
  router.push('/task/list')
}
</script>

<style scoped>
.task-create {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>