<template>
  <div class="milvus-indexes">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>索引管理</span>
        </div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="margin-bottom: 20px">
        <el-form-item label="集合名称" prop="collectionName">
          <el-select v-model="form.collectionName" placeholder="请选择集合名称" @change="loadIndexes">
            <el-option v-for="collection in collections" :key="collection.name" :label="collection.name" :value="collection.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="indexes" style="width: 100%">
        <el-table-column prop="name" label="索引名称" />
        <el-table-column prop="type" label="索引类型" />
        <el-table-column prop="metricType" label="度量类型" />
        <el-table-column prop="dimension" label="维度" />
        <el-table-column prop="nlist" label="nlist" />
        <el-table-column prop="createdTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" type="danger" @click="handleDeleteIndex(scope.row.name)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-divider>创建索引</el-divider>
      <el-form :model="indexForm" :rules="indexRules" ref="indexFormRef" label-width="120px">
        <el-form-item label="索引名称" prop="name">
          <el-input v-model="indexForm.name" placeholder="请输入索引名称" />
        </el-form-item>
        <el-form-item label="索引类型" prop="type">
          <el-select v-model="indexForm.type" placeholder="请选择索引类型">
            <el-option label="IVF_FLAT" value="IVF_FLAT" />
            <el-option label="IVF_SQ8" value="IVF_SQ8" />
            <el-option label="IVF_PQ" value="IVF_PQ" />
            <el-option label="HNSW" value="HNSW" />
          </el-select>
        </el-form-item>
        <el-form-item label="度量类型" prop="metricType">
          <el-select v-model="indexForm.metricType" placeholder="请选择度量类型">
            <el-option label="L2" value="L2" />
            <el-option label="IP" value="IP" />
            <el-option label="COSINE" value="COSINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="维度" prop="dimension">
          <el-input v-model="indexForm.dimension" type="number" placeholder="请输入维度" />
        </el-form-item>
        <el-form-item label="nlist" prop="nlist">
          <el-input v-model="indexForm.nlist" type="number" placeholder="请输入nlist" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCreateIndex">创建索引</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { milvusApi } from '@/api'

const form = ref({
  collectionName: ''
})

const formRef = ref<any>(null)

const indexForm = ref({
  name: '',
  type: 'IVF_FLAT',
  metricType: 'L2',
  dimension: 128,
  nlist: 1024
})

const indexFormRef = ref<any>(null)

const collections = ref<any[]>([])
const indexes = ref<any[]>([])

const rules = ref({
  collectionName: [
    { required: true, message: '请选择集合名称', trigger: 'change' }
  ]
})

const indexRules = ref({
  name: [
    { required: true, message: '请输入索引名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择索引类型', trigger: 'change' }
  ],
  metricType: [
    { required: true, message: '请选择度量类型', trigger: 'change' }
  ],
  dimension: [
    { required: true, message: '请输入维度', trigger: 'blur' }
  ],
  nlist: [
    { required: true, message: '请输入nlist', trigger: 'blur' }
  ]
})

onMounted(() => {
  loadCollections()
})

const loadCollections = async () => {
  try {
    const response = await milvusApi.getCollections()
    collections.value = response.data || []
  } catch (error) {
    console.error('Failed to load collections:', error)
  }
}

const loadIndexes = async () => {
  if (!form.value.collectionName) return
  
  try {
    const response = await milvusApi.getIndexes(form.value.collectionName)
    indexes.value = response.data || []
  } catch (error) {
    console.error('Failed to load indexes:', error)
  }
}

const handleCreateIndex = async () => {
  if (indexFormRef.value) {
    await indexFormRef.value.validate(async (valid: boolean) => {
      if (valid && form.value.collectionName) {
        try {
          const result = await milvusApi.createIndex(form.value.collectionName, indexForm.value)
          console.log('Create index result:', result)
          await loadIndexes()
        } catch (error) {
          console.error('Failed to create index:', error)
        }
      }
    })
  }
}

const handleDeleteIndex = async (indexName: string) => {
  if (!form.value.collectionName) return
  
  try {
    const result = await milvusApi.deleteIndex(form.value.collectionName, indexName)
    console.log('Delete index result:', result)
    await loadIndexes()
  } catch (error) {
    console.error('Failed to delete index:', error)
  }
}
</script>

<style scoped>
.milvus-indexes {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>