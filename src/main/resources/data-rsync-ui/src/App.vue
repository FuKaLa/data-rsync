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
      <el-container style="height: 100vh; display: flex; flex-direction: column;">
        <!-- 顶部导航栏 -->
        <Header :breadcrumb-items="breadcrumbItems" @logout="handleLogout" />
        
        <el-container class="app-body" style="flex: 1; display: flex;">
          <!-- 左侧菜单栏 -->
          <Sidebar 
            :active-menu="activeMenu" 
            @menu-select="handleMenuSelect" 
            @toggle-collapse="handleSidebarToggle"
          />
          
          <!-- 主内容区域 -->
          <el-main :class="['app-main', { 'app-main-expanded': sidebarCollapsed }]" style="padding: 0;">
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
    
    <!-- 全局加载状态 -->
    <LoadingSpinner 
      v-if="appState.loading.isLoading" 
      :fullscreen="true" 
      :text="appState.loading.text" 
      size="large"
    />
    
    <!-- 全局错误信息 -->
    <div v-if="appState.error.show" class="global-error-overlay">
      <div class="global-error-container">
        <ErrorMessage
          :type="appState.error.type"
          :title="appState.error.title"
          :message="appState.error.message"
          :actions="[...appState.error.actions]"
          :closable="appState.error.closable"
          size="large"
          @close="hideError"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Header from './components/common/Header.vue'
import Sidebar from './components/common/Sidebar.vue'
import LoadingSpinner from './components/common/LoadingSpinner.vue'
import ErrorMessage from './components/common/ErrorMessage.vue'
import useAppState from './composables/useAppState'
import { removeToken } from './utils/tokenManager'
import themeManager from './utils/themeManager'

const router = useRouter()
const route = useRoute()
const activeMenu = ref('dashboard')
const isLowPerformanceDevice = ref(false)
const sidebarCollapsed = ref(false)

// 使用应用状态管理
const { appState, hideError } = useAppState()

// 面包屑导航
const breadcrumbItems = ref([{ label: '控制台' }])

// 检测设备性能
const detectPerformance = () => {
  // 简单的性能检测逻辑
  const hasPerformanceAPI = typeof window.performance !== 'undefined'
  const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
  const memory = (navigator as any).deviceMemory || 4 // 默认4GB
  const cores = navigator.hardwareConcurrency || 4 // 默认4核
  
  // 判定为低端设备的条件
  isLowPerformanceDevice.value = isMobile || memory < 4 || cores < 4
  
  // 根据性能状态添加CSS类
  if (isLowPerformanceDevice.value) {
    document.documentElement.classList.add('low-performance')
  } else {
    document.documentElement.classList.remove('low-performance')
  }
}

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

// 退出登录
const handleLogout = () => {
  // 清除token
  removeToken()
  // 跳转到登录页面
  router.push('/login')
}

// 处理侧边栏折叠
const handleSidebarToggle = (collapsed: boolean) => {
  sidebarCollapsed.value = collapsed
}

onMounted(() => {
  // 初始化
  updateBreadcrumb(route.path)
  // 检测设备性能
  detectPerformance()
})
</script>

<style scoped>
/* 全局样式 */
#app {
  height: 100vh;
  background: var(--background-color, #0f172a);
  position: relative;
  overflow: hidden;
  transition: background-color 0.3s ease;
}

/* 背景动画效果 - 仅在性能较好的设备上启用 */
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
  z-index: 0;
  will-change: opacity;
}

/* 仅在支持的设备上启用动画 */
@media (prefers-reduced-motion: no-preference) {
  #app::before {
    animation: backgroundPulse 8s ease-in-out infinite;
  }
}

@keyframes backgroundPulse {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 0.8;
  }
}

/* 网格背景 - 仅在性能较好的设备上启用 */
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
  z-index: 0;
  will-change: transform;
}

/* 仅在支持的设备上启用动画 */
@media (prefers-reduced-motion: no-preference) {
  #app::after {
    animation: gridMove 20s linear infinite;
  }
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(20px, 20px);
  }
}

/* 低端设备性能优化 */
.low-performance #app::before {
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(59, 130, 246, 0.05) 0%, transparent 50%),
    linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.2) 100%);
  animation: none;
  opacity: 0.3;
}

.low-performance #app::after {
  background-image: none;
  animation: none;
}

/* 浅色主题适配 */
.light-theme #app {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 50%, #f1f5f9 100%);
}

.light-theme #app::before {
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(59, 130, 246, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(168, 85, 247, 0.05) 0%, transparent 50%),
    linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.05) 100%);
}

.light-theme #app::after {
  background-image: 
    linear-gradient(rgba(59, 130, 246, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.05) 1px, transparent 1px);
}

/* 主内容区域 */
.app-body {
  flex: 1;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.app-main {
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(10px);
  overflow-y: auto;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

/* 浅色主题下的主内容区域 */
.light-theme .app-main {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.app-main-expanded {
  padding-left: 24px;
}

/* 侧边栏折叠时的主内容区域调整 */
.sidebar-collapsed ~ .app-main {
  padding-left: 24px;
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
  transition: all 0.3s ease;
}

/* 浅色主题下的页面容器 */
.light-theme .page-container {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.1);
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

/* 浅色主题下的动画效果 */
.light-theme .page-container {
  animation: fadeInLight 0.5s ease-in-out;
}

@keyframes fadeInLight {
  from {
    opacity: 0;
    transform: translateY(5px);
    box-shadow: 0 0 0 rgba(0, 0, 0, 0);
  }
  to {
    opacity: 1;
    transform: translateY(0);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
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
  .app-main {
    padding: 16px;
  }
  
  .page-container {
    padding: 16px;
  }
}

/* 键盘导航焦点样式 */
*:focus-visible {
  outline: 2px solid var(--primary-color, #3b82f6);
  outline-offset: 2px;
  border-radius: 4px;
}

/* 屏幕阅读器专用样式 */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

/* 全局错误信息样式 */
.global-error-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 9998;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 浅色主题下的错误信息 */
.light-theme .global-error-overlay {
  background: rgba(0, 0, 0, 0.3);
}

.global-error-container {
  max-width: 500px;
  width: 100%;
  animation: fadeInUp 0.3s ease forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>