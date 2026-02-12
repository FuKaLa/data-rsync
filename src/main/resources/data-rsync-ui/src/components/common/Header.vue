<template>
  <el-header class="header">
    <div class="header-content">
      <!-- 左侧区域 -->
      <div class="header-left">
        <!-- 品牌标识 -->
        <div class="brand">
          <div class="logo">
            <svg width="32" height="32" viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="32" height="32" rx="8" fill="url(#gradient-logo)"/>
              <path d="M10 10L22 10L22 22L10 22L10 10Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M16 10L16 22" stroke="white" stroke-width="2" stroke-linecap="round"/>
              <path d="M10 16L22 16" stroke="white" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <defs>
              <linearGradient id="gradient-logo" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#409eff"/>
                <stop offset="100%" stop-color="#667eea"/>
              </linearGradient>
            </defs>
          </div>
          <h1 class="title">数据同步系统</h1>
        </div>
      </div>
      
      <!-- 中间区域 - 面包屑导航 -->
      <div class="header-center">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index">
            <span class="breadcrumb-item">{{ item.label }}</span>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 右侧区域 -->
      <div class="header-right">
        <!-- 全局搜索 -->
        <div class="search-container">
          <el-input
            v-model="searchQuery"
            placeholder="搜索任务、数据源..."
            class="search-input"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <!-- 通知 -->
        <el-dropdown trigger="click" class="notification-dropdown">
          <el-badge :value="notificationCount" class="notification-badge">
            <el-button 
              class="header-btn"
              icon="Bell"
              circle
            />
          </el-badge>
          <template #dropdown>
            <el-dropdown-menu class="notification-menu">
              <div class="notification-header">
                <span class="notification-title">通知中心</span>
                <el-button link size="small" @click="markAllAsRead">全部已读</el-button>
              </div>
              <div class="notification-list">
                <el-dropdown-item 
                  v-for="notification in notifications" 
                  :key="notification.id"
                  class="notification-item"
                >
                  <div class="notification-content">
                    <div class="notification-icon" :class="notification.type">
                      <el-icon><component :is="notification.icon" /></el-icon>
                    </div>
                    <div class="notification-info">
                      <div class="notification-message">{{ notification.message }}</div>
                      <div class="notification-time">{{ notification.time }}</div>
                    </div>
                    <div v-if="!notification.read" class="notification-dot"></div>
                  </div>
                </el-dropdown-item>
                <div v-if="notifications.length === 0" class="notification-empty">
                  <el-icon><Bell /></el-icon>
                  <span>暂无通知</span>
                </div>
              </div>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 主题切换 -->
        <el-button 
          class="header-btn"
          circle
          @click="toggleTheme"
        >
          <el-icon>
            <component :is="isDarkTheme ? Sunny : Moon" />
          </el-icon>
        </el-button>
        
        <!-- 系统状态 -->
        <div class="current-menu-name">
          <span class="menu-label">数据同步系统</span>
        </div>
        
        <!-- 用户菜单 -->
        <el-dropdown trigger="click" class="user-dropdown">
          <div class="user-info">
            <div class="user-avatar">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="user-name">{{ userName }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToProfile">
                <el-icon><User /></el-icon>
                <span>个人资料</span>
              </el-dropdown-item>
              <el-dropdown-item @click="goToSettings">
                <el-icon><Setting /></el-icon>
                <span>系统设置</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click="$emit('logout')">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, markRaw } from 'vue'
import { Bell, Moon, Sunny, User, Setting, SwitchButton, Search, Check, Warning, Close, InfoFilled } from '@element-plus/icons-vue'
import themeManager from '@/utils/themeManager'

const props = defineProps<{
  breadcrumbItems: Array<{
    label: string
    path?: string
  }>
}>()

const emit = defineEmits<{
  (e: 'logout'): void
}>()

// 搜索相关
const searchQuery = ref('')

// 通知相关
const notificationCount = ref(3)
const notifications = ref([
  {
    id: 1,
    message: '任务 "数据同步任务1" 执行成功',
    time: '10分钟前',
    type: 'success',
    icon: markRaw(Check),
    read: false
  },
  {
    id: 2,
    message: '数据源 "MySQL 主库" 连接异常',
    time: '30分钟前',
    type: 'error',
    icon: markRaw(Close),
    read: false
  },
  {
    id: 3,
    message: '系统将于今晚 23:00 进行维护',
    time: '2小时前',
    type: 'warning',
    icon: markRaw(Warning),
    read: true
  }
])

// 主题相关
const isDarkTheme = ref(true)

// 用户相关
const userName = ref('管理员')

// 初始化主题
onMounted(() => {
  isDarkTheme.value = themeManager.getTheme() === 'dark'
  // 监听主题变化
  themeManager.onThemeChange((theme) => {
    isDarkTheme.value = theme === 'dark'
  })
})

// 搜索处理
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    console.log('Searching for:', searchQuery.value)
    // 这里可以添加搜索逻辑，比如跳转到搜索结果页面
  }
}

// 通知处理
const markAllAsRead = () => {
  notifications.value.forEach(notification => {
    notification.read = true
  })
  notificationCount.value = 0
}

// 主题切换
const toggleTheme = () => {
  const newTheme = themeManager.toggleTheme()
  console.log('Theme toggled to:', newTheme)
}

// 导航处理
const goToProfile = () => {
  console.log('Go to profile')
}

const goToSettings = () => {
  console.log('Go to settings')
}
</script>

<style scoped>
.header {
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
  height: 64px;
  position: relative;
  z-index: 100;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 24px;
}

/* 左侧品牌区域 */
.header-left {
  display: flex;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.05) rotate(5deg);
}

.title {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(90deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  transition: all 0.3s ease;
}

/* 中间面包屑区域 */
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  order: 2;
}

/* 左侧品牌区域 */
.header-left {
  display: flex;
  align-items: center;
  order: 1;
}

/* 右侧用户区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  order: 3;
}

/* 确保header-content的flex布局正确 */
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 24px;
  flex-wrap: nowrap;
}

.breadcrumb {
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  margin: 0;
  padding: 0;
  min-height: 64px;
  width: 100%;
}

.breadcrumb :deep(.el-breadcrumb) {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  margin: 0;
  padding: 0;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  margin: 0;
  padding: 0;
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

/* 确保Element Plus默认样式不影响对齐 */
.breadcrumb :deep(.el-breadcrumb) {
  line-height: 1;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  line-height: 1;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  line-height: 1;
}

.breadcrumb-item {
  color: #94a3b8;
  transition: all 0.3s ease;
}

.breadcrumb-item:hover {
  color: #409eff;
}

/* 右侧用户区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 当前菜单名称 */
.current-menu-name {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background: rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 16px;
  transition: all 0.3s ease;
}

.current-menu-name:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.menu-label {
  font-size: 14px;
  font-weight: 500;
  color: #409eff;
  white-space: nowrap;
  font-feature-settings: "pnum";
  font-variant-numeric: proportional-nums;
}

/* 搜索容器 */
.search-container {
  position: relative;
}

.search-input {
  width: 240px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 20px;
  color: #f8fafc;
  transition: all 0.3s ease;
}

.search-input:hover {
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.search-input :deep(.el-input__wrapper) {
  background: transparent;
  border-radius: 20px;
}

.search-input :deep(.el-input__inner) {
  color: #f8fafc;
}

.search-input :deep(.el-input__prefix) {
  color: #94a3b8;
}

.search-input :deep(.el-input__suffix) {
  color: #94a3b8;
}

.search-icon {
  font-size: 14px;
}

/* 头部按钮 */
.header-btn {
  color: #94a3b8;
  transition: all 0.3s ease;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.header-btn:hover {
  color: #409eff;
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.5);
  transform: translateY(-2px);
}

/* 通知系统 */
.notification-dropdown {
  position: relative;
}

.notification-badge {
  position: relative;
}

.notification-badge :deep(.el-badge__content) {
  background: #ef4444;
  color: white;
  border: 2px solid rgba(15, 23, 42, 0.9);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(239, 68, 68, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0);
  }
}

.notification-menu {
  width: 320px;
  max-height: 400px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.notification-header {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #f8fafc;
}

.notification-list {
  padding: 8px 0;
  overflow-y: auto;
  flex: 1;
}

.notification-item {
  padding: 0;
  margin: 0;
}

.notification-content {
  padding: 12px 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  position: relative;
  transition: all 0.3s ease;
}

.notification-content:hover {
  background: rgba(59, 130, 246, 0.1);
}

.notification-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon.success {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.notification-icon.error {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.notification-icon.warning {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
}

.notification-icon.info {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
}

.notification-info {
  flex: 1;
  min-width: 0;
}

.notification-message {
  font-size: 13px;
  color: #f8fafc;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-time {
  font-size: 11px;
  color: #64748b;
}

.notification-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  position: absolute;
  top: 16px;
  right: 16px;
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.3);
}

.notification-empty {
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #64748b;
}

.notification-empty :deep(.el-icon) {
  font-size: 24px;
  opacity: 0.5;
}

/* 用户信息 */
.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.user-info:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.5);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #667eea);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.user-name {
  color: #f8fafc;
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
}

/* 下拉菜单 */
:deep(.el-dropdown-menu) {
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  padding: 8px 0;
}

:deep(.el-dropdown-item) {
  color: #f8fafc;
  padding: 10px 20px;
  border-radius: 8px;
  margin: 2px 8px;
  transition: all 0.3s ease;
}

:deep(.el-dropdown-item:hover) {
  background: rgba(59, 130, 246, 0.2);
  color: #409eff;
}

:deep(.el-dropdown-item .el-icon) {
  margin-right: 8px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .search-input {
    width: 200px;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .title {
    font-size: 16px;
  }
  
  .search-input {
    width: 180px;
  }
  
  .user-name {
    display: none;
  }
  
  .header-btn {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .brand {
    gap: 8px;
  }
  
  .title {
    font-size: 14px;
  }
  
  .header-center {
    display: none;
  }
  
  .header-right {
    gap: 8px;
  }
  
  .search-container {
    display: none;
  }
}
</style>