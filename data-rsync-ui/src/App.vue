<template>
  <div id="app">
    <el-container style="height: 100vh">
      <el-header>
        <div class="header-content">
          <h1>数据同步系统管理平台</h1>
          <div class="header-actions">
            <el-dropdown>
              <span class="user-info">
                <el-avatar size="small">用户</el-avatar>
                <span style="margin-left: 8px">管理员</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人中心</el-dropdown-item>
                  <el-dropdown-item>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" style="background-color: #f0f2f5">
          <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
            <el-menu-item index="dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>控制台</span>
            </el-menu-item>
            <el-sub-menu index="data-source">
              <template #title>
                <el-icon><DataAnalysis /></el-icon>
                <span>数据源管理</span>
              </template>
              <el-menu-item index="data-source/list">数据源列表</el-menu-item>
              <el-menu-item index="data-source/create">创建数据源</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="task">
              <template #title>
                <el-icon><Timer /></el-icon>
                <span>任务管理</span>
              </template>
              <el-menu-item index="task/list">任务列表</el-menu-item>
              <el-menu-item index="task/create">创建任务</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="monitor">
              <template #title>
                <el-icon><VideoPlay /></el-icon>
                <span>监控中心</span>
              </template>
              <el-menu-item index="monitor/dashboard">监控面板</el-menu-item>
              <el-menu-item index="monitor/topology">同步拓扑</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="milvus">
              <template #title>
                <el-icon><Grid /></el-icon>
                <span>Milvus管理</span>
              </template>
              <el-menu-item index="milvus/collections">集合管理</el-menu-item>
              <el-menu-item index="milvus/indexes">索引管理</el-menu-item>
              <el-menu-item index="milvus/health">健康状态</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="system">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="system/users">用户管理</el-menu-item>
              <el-menu-item index="system/roles">角色管理</el-menu-item>
              <el-menu-item index="system/logs">审计日志</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, DataAnalysis, Timer, VideoPlay, Grid, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const activeMenu = ref('dashboard')

const handleMenuSelect = (key: string) => {
  activeMenu.value = key
  router.push(`/${key}`)
}

onMounted(() => {
  if (route.path !== '/') {
    activeMenu.value = route.path.substring(1)
  }
})
</script>

<style scoped>
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.header-content h1 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.el-menu-vertical-demo {
  height: 100%;
  border-right: none;
}

.el-main {
  padding: 20px;
  background-color: #f9fafc;
}
</style>