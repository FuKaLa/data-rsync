<template>
  <el-aside :class="['sidebar', { 'sidebar-collapsed': collapsed }]">
    <div class="sidebar-content">
      <!-- 折叠按钮 -->
      <div class="sidebar-toggle" @click="toggleCollapse">
        <el-icon :class="['toggle-icon', { 'rotate': collapsed }]">
          <component :is="collapsed ? ArrowRight : ArrowLeft" />
        </el-icon>
      </div>
      
      <!-- 菜单列表 -->
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        @select="$emit('menu-select', $event, [])"
        background-color="transparent"
        text-color="#94a3b8"
        active-text-color="#409eff"
        :collapse="collapsed"
        :collapse-transition="true"
      >
        <!-- 仪表盘 -->
        <el-menu-item index="dashboard" class="menu-item">
          <el-icon><PieChart /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        
        <!-- 数据源管理 -->
        <el-sub-menu index="data-source" class="menu-item">
          <template #title>
            <el-icon><Files /></el-icon>
            <span>数据源管理</span>
          </template>
          <el-menu-item index="data-source/list">
            <el-icon><List /></el-icon>
            <span>数据源列表</span>
          </el-menu-item>
          <el-menu-item index="data-source/create">
            <el-icon><Plus /></el-icon>
            <span>创建数据源</span>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 任务管理 -->
        <el-sub-menu index="task" class="menu-item">
          <template #title>
            <el-icon><Timer /></el-icon>
            <span>任务管理</span>
          </template>
          <el-menu-item index="task/list">
            <el-icon><List /></el-icon>
            <span>任务列表</span>
          </el-menu-item>
          <el-menu-item index="task/create">
            <el-icon><Plus /></el-icon>
            <span>创建任务</span>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 监控中心 -->
        <el-sub-menu index="monitor" class="menu-item">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>监控中心</span>
          </template>
          <el-menu-item index="monitor/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>监控面板</span>
          </el-menu-item>
          <el-menu-item index="monitor/topology">
            <el-icon><Connection /></el-icon>
            <span>系统拓扑</span>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- Milvus管理 -->
        <el-sub-menu index="milvus" class="menu-item">
          <template #title>
            <el-icon><Grid /></el-icon>
            <span>Milvus管理</span>
          </template>
          <el-menu-item index="milvus/collections">
            <el-icon><CollectionTag /></el-icon>
            <span>集合管理</span>
          </el-menu-item>
          <el-menu-item index="milvus/indexes">
            <el-icon><Filter /></el-icon>
            <span>索引管理</span>
          </el-menu-item>
          <el-menu-item index="milvus/health">
            <el-icon><Check /></el-icon>
            <span>健康检查</span>
          </el-menu-item>
          <el-menu-item index="milvus/sync">
            <el-icon><DataMigration /></el-icon>
            <span>数据同步</span>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 字典管理 -->
        <el-sub-menu index="dict" class="menu-item">
          <template #title>
            <el-icon><Menu /></el-icon>
            <span>字典管理</span>
          </template>
          <el-menu-item index="dict/type">
            <el-icon><Collection /></el-icon>
            <span>字典类型管理</span>
          </el-menu-item>
          <el-menu-item index="dict/item">
            <el-icon><CollectionTag /></el-icon>
            <span>字典项管理</span>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 系统管理 -->
        <el-sub-menu index="system" class="menu-item">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="system/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="system/roles">
            <el-icon><Position /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="system/logs">
            <el-icon><Reading /></el-icon>
            <span>审计日志</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
      
      <!-- 底部信息 -->
      <div class="sidebar-footer" v-show="!collapsed">
        <div class="version">
          <span>版本 1.0.0</span>
        </div>
        <div class="status">
          <div class="status-indicator online"></div>
          <span>系统运行正常</span>
        </div>
      </div>
    </div>
  </el-aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  PieChart,
  DataAnalysis,
  List,
  Plus,
  Timer,
  Monitor,
  Connection,
  Grid,
  CollectionTag,
  Filter,
  Check,
  Setting,
  User,
  Position,
  Reading,
  Menu,
  Collection,
  ArrowLeft,
  ArrowRight,
  Files,
  DataMigration
} from '@element-plus/icons-vue'

const props = defineProps<{
  activeMenu: string
}>()

const emit = defineEmits<{
  (e: 'menu-select', key: string, keyPath: string[]): void
  (e: 'toggle-collapse', collapsed: boolean): void
}>()

const collapsed = ref(false)

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
  emit('toggle-collapse', collapsed.value)
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(59, 130, 246, 0.2);
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  overflow: hidden;
}

.sidebar-collapsed {
  width: 64px;
}

.sidebar-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 24px 0;
  position: relative;
}

/* 折叠按钮 */
.sidebar-toggle {
  position: absolute;
  top: 20px;
  right: -8px;
  width: 16px;
  height: 40px;
  background: linear-gradient(135deg, #409eff, #667eea);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  z-index: 10;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.toggle-icon {
  color: white;
  font-size: 12px;
  transition: transform 0.3s ease;
}

.toggle-icon.rotate {
  transform: rotate(180deg);
}

/* 菜单样式 */
.sidebar-menu {
  flex: 1;
  padding: 0 12px;
  overflow-y: auto;
  margin-top: 60px;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  margin: 6px 8px;
  border-radius: 10px;
  padding: 12px 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
  color: #409eff;
  transform: translateX(4px);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.2), rgba(102, 126, 234, 0.1));
  border-color: rgba(64, 158, 255, 0.5);
  color: #409eff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.sidebar-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, #409eff, #667eea);
  border-radius: 0 2px 2px 0;
}

.sidebar-menu :deep(.el-menu-item .el-icon),
.sidebar-menu :deep(.el-sub-menu__title .el-icon) {
  margin-right: 12px;
  font-size: 18px;
  transition: all 0.3s ease;
}

.sidebar-menu :deep(.el-menu-item:hover .el-icon),
.sidebar-menu :deep(.el-sub-menu__title:hover .el-icon),
.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  transform: scale(1.1) rotate(5deg);
  color: #409eff;
}

/* 子菜单样式 */
.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: transparent;
  padding: 4px 0;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 48px !important;
  margin: 2px 8px;
  border-radius: 8px;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item:hover) {
  transform: translateX(8px);
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(64, 158, 255, 0.15);
  border-color: rgba(64, 158, 255, 0.4);
}

/* 底部信息 */
.sidebar-footer {
  padding: 20px 24px;
  border-top: 1px solid rgba(59, 130, 246, 0.2);
  margin-top: 20px;
  transition: all 0.3s ease;
}

.version {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 12px;
}

.status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-indicator.online {
  background: #10b981;
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.5);
}

/* 滚动条样式 */
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: rgba(15, 23, 42, 0.6);
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: rgba(59, 130, 246, 0.3);
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(59, 130, 246, 0.5);
}

/* 折叠状态样式 */
.sidebar-collapsed {
  width: 64px;
}

.sidebar-collapsed .sidebar-toggle {
  right: -8px;
}

.sidebar-collapsed .sidebar-menu {
  padding: 0 4px;
}

.sidebar-collapsed .sidebar-menu :deep(.el-menu-item),
.sidebar-collapsed .sidebar-menu :deep(.el-sub-menu__title) {
  padding: 12px 8px;
  justify-content: center;
}

.sidebar-collapsed .sidebar-menu :deep(.el-menu-item .el-icon),
.sidebar-collapsed .sidebar-menu :deep(.el-sub-menu__title .el-icon) {
  margin-right: 0;
  font-size: 20px;
}

.sidebar-collapsed .sidebar-menu :deep(.el-menu-item:hover),
.sidebar-collapsed .sidebar-menu :deep(.el-sub-menu__title:hover) {
  transform: none;
  background: rgba(59, 130, 246, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 200px;
  }
  
  .sidebar-collapsed {
    width: 64px;
  }
  
  .sidebar-menu :deep(.el-menu-item),
  .sidebar-menu :deep(.el-sub-menu__title) {
    padding: 10px 14px;
  }
  
  .sidebar-menu :deep(.el-menu-item .el-icon),
  .sidebar-menu :deep(.el-sub-menu__title .el-icon) {
    margin-right: 10px;
    font-size: 16px;
  }
  
  .sidebar-footer {
    padding: 16px 20px;
  }
}

@media (max-width: 480px) {
  .sidebar {
    width: 180px;
  }
  
  .sidebar-collapsed {
    width: 64px;
  }
  
  .sidebar-menu :deep(.el-menu-item span),
  .sidebar-menu :deep(.el-sub-menu__title span) {
    font-size: 13px;
  }
  
  .sidebar-toggle {
    top: 16px;
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 64px;
    bottom: 0;
    z-index: 1000;
    transform: translateX(0);
    transition: transform 0.3s ease;
  }
  
  .sidebar.sidebar-collapsed {
    transform: translateX(-100%);
  }
}
</style>