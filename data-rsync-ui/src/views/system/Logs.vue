<template>
  <div class="system-logs">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
        </div>
      </template>
      <el-table :data="logs" style="width: 100%">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="username" label="操作人" />
        <el-table-column prop="operation" label="操作" />
        <el-table-column prop="targetType" label="操作对象" />
        <el-table-column prop="targetId" label="对象ID" />
        <el-table-column prop="ip" label="IP地址" />
        <el-table-column prop="userAgent" label="用户代理" />
        <el-table-column prop="createTime" label="操作时间" />
        <el-table-column prop="success" label="操作结果">
          <template #default="scope">
            <el-tag :type="scope.row.success ? 'success' : 'danger'">
              {{ scope.row.success ? '成功' : '失败' }}
            </el-tag>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const logs = ref([
  {
    id: 1,
    username: 'admin',
    operation: 'CREATE',
    targetType: 'DATASOURCE',
    targetId: '1',
    ip: '127.0.0.1',
    userAgent: 'Mozilla/5.0...',
    createTime: '2026-02-02T10:00:00',
    success: true
  }
])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(1)

const handleSizeChange = (size: number) => {
  pageSize.value = size
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
}
</script>

<style scoped>
.system-logs {
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
</style>