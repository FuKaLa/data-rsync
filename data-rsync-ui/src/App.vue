<template>
  <div id="app">
    <!-- 登录页面不需要主布局 -->
    <template v-if="route.path === '/login'">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </template>
    
    <!-- 其他页面显示主布局 -->
    <template v-else>
      <el-container style="height: 100vh">
        <!-- 顶部导航栏 -->
        <el-header class="app-header">
          <div class="header-left">
            <div class="logo" @click="navigateToDashboard">
              <el-icon class="logo-icon"><Grid /></el-icon>
              <span class="logo-text">DataRsync</span>
            </div>
            <div class="header-nav">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">
                  {{ item.label }}
                </el-breadcrumb-item>
              </el-breadcrumb>
            </div>
          </div>
          <div class="header-right">
            <div class="header-actions">
              <el-dropdown trigger="click" class="notification-dropdown">
                <el-badge :value="3" class="notification-badge">
                  <el-icon class="header-icon"><Bell /></el-icon>
                </el-badge>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item class="notification-item">
                      <div class="notification-content">
                        <el-badge type="success" class="notification-type" />
                        <div>
                          <p class="notification-title">任务完成</p>
                          <p class="notification-desc">数据同步任务 #123 已成功完成</p>
                        </div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item class="notification-item">
                      <div class="notification-content">
                        <el-badge type="warning" class="notification-type" />
                        <div>
                          <p class="notification-title">系统警告</p>
                          <p class="notification-desc">数据源 MySQL-01 连接状态异常</p>
                        </div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item class="notification-item">
                      <div class="notification-content">
                        <el-badge type="danger" class="notification-type" />
                        <div>
                          <p class="notification-title">任务失败</p>
                          <p class="notification-desc">数据同步任务 #456 执行失败</p>
                        </div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item divided class="notification-more">查看全部</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              
              <el-dropdown trigger="click" class="user-dropdown">
                <div class="user-info">
                  <el-avatar size="small" class="user-avatar">
                    <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20user%20avatar%20portrait%20minimalist%20style&image_size=square" alt="用户头像" />
                  </el-avatar>
                  <span class="user-name">管理员</span>
                  <el-icon class="user-arrow"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>
                      <el-icon><UserFilled /></el-icon>
                      <span>个人中心</span>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-icon><Setting /></el-icon>
                      <span>账号设置</span>
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">
                      <el-icon><SwitchButton /></el-icon>
                      <span>退出登录</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        
        <el-container class="app-body">
          <!-- 左侧菜单栏 -->
          <el-aside width="240px" class="app-sidebar">
            <el-menu 
              :default-active="activeMenu" 
              class="sidebar-menu" 
              @select="handleMenuSelect"
              background-color="#0f172a"
              text-color="rgba(255, 255, 255, 0.7)"
              active-text-color="#ffffff"
            >

              
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
              </el-sub-menu>
              
              <el-sub-menu index="system">
                <template #title>
                  <el-icon><Setting /></el-icon>
                  <span>系统管理</span>
                </template>
                <el-menu-item index="system/logs">审计日志</el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-aside>
          
          <!-- 主内容区域 -->
          <el-main class="app-main">
            <div class="page-container">
              <router-view v-slot="{ Component }">
                <transition name="fade" mode="in-out">
                  <component :is="Component" />
                </transition>
              </router-view>
            </div>
          </el-main>
        </el-container>
      </el-container>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  HomeFilled, DataAnalysis, Timer, VideoPlay, Setting, 
  Bell, UserFilled, ArrowDown, SwitchButton, Grid 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const activeMenu = ref('dashboard')

// 面包屑导航
const breadcrumbItems = ref([{ label: '控制台' }])

// 更新面包屑导航
const updateBreadcrumb = (path: string) => {
  const pathParts = path.substring(1).split('/')
  const items = []
  
  if (pathParts.length === 0 || (pathParts.length === 1 && pathParts[0] === '')) {
    items.push({ label: '控制台' })
  } else {
    switch (pathParts[0]) {
      case 'dashboard':
        items.push({ label: '控制台' })
        break
      case 'data-source':
        items.push({ label: '数据源管理' })
        if (pathParts[1] === 'list') {
          items.push({ label: '数据源列表' })
        } else if (pathParts[1] === 'create') {
          items.push({ label: '创建数据源' })
        }
        break
      case 'task':
        items.push({ label: '任务管理' })
        if (pathParts[1] === 'list') {
          items.push({ label: '任务列表' })
        } else if (pathParts[1] === 'create') {
          items.push({ label: '创建任务' })
        }
        break
      case 'monitor':
        items.push({ label: '监控中心' })
        if (pathParts[1] === 'dashboard') {
          items.push({ label: '监控面板' })
        }
        break
      case 'system':
        items.push({ label: '系统管理' })
        if (pathParts[1] === 'logs') {
          items.push({ label: '审计日志' })
        }
        break
    }
  }
  
  breadcrumbItems.value = items
}

// 监听路由变化，更新面包屑和活动菜单
watch(() => route.path, (newPath) => {
  updateBreadcrumb(newPath)
  activeMenu.value = newPath.substring(1) || 'dashboard'
}, { immediate: true })

// 菜单选择处理
const handleMenuSelect = (key: string) => {
  // 立即更新活动菜单状态，提高响应速度
  activeMenu.value = key
  // 使用 replace 方法进行路由跳转，减少浏览器历史记录
  router.replace(`/${key}`)
}

// 导航到仪表盘
const navigateToDashboard = () => {
  router.push('/dashboard')
}

// 退出登录
const handleLogout = () => {
  // 清除token
  localStorage.removeItem('token')
  // 跳转到登录页面
  router.push('/login')
}

onMounted(() => {
  // 初始化
  updateBreadcrumb(route.path)
})
</script>

<style scoped>
/* 全局样式 */
#app {
  height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  position: relative;
  overflow: hidden;
}

/* 背景动画效果 */
#app::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(168, 85, 247, 0.1) 0%, transparent 50%),
    linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.2) 100%);
  animation: backgroundPulse 8s ease-in-out infinite;
  z-index: 0;
}

@keyframes backgroundPulse {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 0.8;
  }
}

/* 网格背景 */
#app::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(59, 130, 246, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  animation: gridMove 20s linear infinite;
  z-index: 0;
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(20px, 20px);
  }
}

/* 顶部导航栏 */
.app-header {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 100;
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
  position: relative;
  z-index: 1;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.logo:hover {
  transform: translateY(-2px);
}

.logo::before {
  content: '';
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: linear-gradient(45deg, #3b82f6, #a855f7);
  border-radius: 12px;
  opacity: 0;
  transition: all 0.3s ease;
  z-index: -1;
  filter: blur(10px);
}

.logo:hover::before {
  opacity: 0.3;
}

.logo-icon {
  font-size: 24px;
  color: #3b82f6;
  margin-right: 8px;
  text-shadow: 0 0 10px rgba(59, 130, 246, 0.5);
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #ffffff;
  text-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
}

/* 面包屑导航 */
.header-nav {
  flex: 1;
  position: relative;
  z-index: 1;
}

/* 右侧操作区 */
.header-right {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 通知图标 */
.notification-dropdown {
  position: relative;
}

.header-icon {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.3s ease;
  text-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
}

.header-icon:hover {
  color: #3b82f6;
  transform: scale(1.1);
  text-shadow: 0 0 20px rgba(59, 130, 246, 0.6);
}

.notification-badge {
  --el-badge-size: 16px;
  transform: translate(50%, -50%);
  background: linear-gradient(45deg, #ef4444, #f97316);
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.4);
}

/* 通知下拉菜单 */
.notification-item {
  min-width: 300px;
  padding: 12px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.notification-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.2);
}

.notification-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.notification-type {
  margin-top: 4px;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 4px 0;
  color: #ffffff;
}

.notification-desc {
  font-size: 12px;
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.4;
}

.notification-more {
  text-align: center;
  font-size: 12px;
  color: #3b82f6;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 8px;
  margin-top: 8px;
  padding: 8px;
  transition: all 0.3s ease;
}

.notification-more:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: #3b82f6;
}

/* 用户信息 */
.user-dropdown {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.user-info:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: #3b82f6;
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.2);
}

.user-avatar {
  --el-avatar-size: 32px;
  border: 2px solid rgba(59, 130, 246, 0.5);
}

.user-name {
  font-size: 14px;
  color: #ffffff;
  font-weight: 500;
}

.user-arrow {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

/* 主内容区域 */
.app-body {
  flex: 1;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 侧边栏 */
.app-sidebar {
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(15px);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.3);
  overflow-y: auto;
  border-right: 1px solid rgba(59, 130, 246, 0.2);
  position: relative;
}

/* 侧边栏背景渐变 */
.app-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(168, 85, 247, 0.05) 100%);
  z-index: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 18px 20px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
  position: relative;
  z-index: 1;
}

.sidebar-header:hover {
  color: #ffffff;
  background: rgba(59, 130, 246, 0.15);
}

.sidebar-toggle-icon {
  margin-right: 12px;
  color: #3b82f6;
  transition: all 0.3s ease;
}

.sidebar-header:hover .sidebar-toggle-icon {
  transform: scale(1.1);
  text-shadow: 0 0 15px rgba(59, 130, 246, 0.6);
}

.sidebar-header-text {
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 0.5px;
}

/* 侧边栏菜单 */
.sidebar-menu {
  background-color: transparent;
  border-right: none;
  position: relative;
  z-index: 1;
}

.sidebar-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.7);
  height: 52px;
  line-height: 52px;
  margin: 6px 12px;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(59, 130, 246, 0.1);
  display: flex;
  align-items: center;
  padding-left: 20px !important;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(59, 130, 246, 0.15);
  color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
  transform: translateY(-2px);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.25), rgba(168, 85, 247, 0.25));
  color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px);
}

.sidebar-menu .el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 60%;
  background: linear-gradient(180deg, #3b82f6, #a855f7);
  border-radius: 0 4px 4px 0;
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.6);
}

.sidebar-menu .el-menu-item .el-icon {
  margin-right: 12px;
  font-size: 18px;
  transition: all 0.3s ease;
}

.sidebar-menu .el-menu-item:hover .el-icon {
  color: #3b82f6;
  transform: scale(1.1);
}

.sidebar-menu .el-menu-item.is-active .el-icon {
  color: #ffffff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.6);
}

.sidebar-menu .el-sub-menu__title {
  color: rgba(255, 255, 255, 0.7);
  height: 52px;
  line-height: 52px;
  margin: 6px 12px;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(59, 130, 246, 0.1);
  display: flex;
  align-items: center;
  padding-left: 20px !important;
  position: relative;
  z-index: 1;
}

.sidebar-menu .el-sub-menu__title:hover {
  background: rgba(59, 130, 246, 0.15);
  color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
  transform: translateY(-2px);
}

.sidebar-menu .el-sub-menu__title .el-icon {
  margin-right: 12px;
  font-size: 18px;
  transition: all 0.3s ease;
}

.sidebar-menu .el-sub-menu__title:hover .el-icon {
  color: #3b82f6;
  transform: scale(1.1);
}

.sidebar-menu .el-sub-menu .el-menu {
  background-color: transparent;
  padding: 4px 0;
}

.sidebar-menu .el-sub-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  padding-left: 48px !important;
  margin: 4px 12px;
  font-size: 13px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(59, 130, 246, 0.05);
}

.sidebar-menu .el-sub-menu .el-menu-item:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.5);
  transform: translateY(-1px);
}

.sidebar-menu .el-sub-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(168, 85, 247, 0.2));
  border-color: rgba(59, 130, 246, 0.8);
}

/* 侧边栏折叠动画 */
.app-sidebar.collapsed {
  width: 64px !important;
}

.app-sidebar.collapsed .sidebar-header-text {
  opacity: 0;
  transform: translateX(-10px);
}

.app-sidebar.collapsed .sidebar-menu .el-menu-item span {
  opacity: 0;
  transform: translateX(-10px);
}

.app-sidebar.collapsed .sidebar-menu .el-sub-menu__title span {
  opacity: 0;
  transform: translateX(-10px);
}

.app-sidebar.collapsed .sidebar-menu .el-menu-item {
  padding-left: 16px !important;
  justify-content: center;
}

.app-sidebar.collapsed .sidebar-menu .el-sub-menu__title {
  padding-left: 16px !important;
  justify-content: center;
}

.app-sidebar.collapsed .sidebar-menu .el-menu-item .el-icon {
  margin-right: 0;
}

.app-sidebar.collapsed .sidebar-menu .el-sub-menu__title .el-icon {
  margin-right: 0;
}

/* 主内容区域 */
.app-main {
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(10px);
  padding: 24px;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

.page-container {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  padding: 24px;
  min-height: calc(100vh - 184px);
  animation: fadeIn 0.5s ease-in-out;
  border: 1px solid rgba(59, 130, 246, 0.2);
  position: relative;
  z-index: 1;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(5px);
    box-shadow: 0 0 0 rgba(0, 0, 0, 0);
  }
  to {
    opacity: 1;
    transform: translateY(0);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  }
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(5px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 16px;
  }
  
  .logo-text {
    display: none;
  }
  
  .header-nav {
    display: none;
  }
  
  .app-sidebar {
    position: fixed;
    left: 0;
    top: 64px;
    height: calc(100vh - 64px);
    z-index: 999;
  }
  
  .app-main {
    padding: 16px;
  }
  
  .page-container {
    padding: 16px;
    border-radius: 8px;
  }
}
</style>