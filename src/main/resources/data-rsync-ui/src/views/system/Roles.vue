<template>
  <div class="system-roles">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
        </div>
      </template>
      <el-table :data="roles" style="width: 100%">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="permissions" label="权限">
          <template #default="scope">
            <el-tag v-for="permission in scope.row.permissions" :key="permission" style="margin-right: 5px">
              {{ permission }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small">编辑</el-button>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const roles = ref([
  {
    id: 1,
    name: 'ADMIN',
    description: '管理员',
    permissions: ['ALL'],
    createTime: '2026-02-01T10:00:00'
  },
  {
    id: 2,
    name: 'USER',
    description: '普通用户',
    permissions: ['READ'],
    createTime: '2026-02-01T10:00:00'
  }
])
</script>

<style scoped>
.system-roles {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>