<template>
  <div class="error-message" :class="[type, size]">
    <!-- 错误图标 -->
    <div class="error-icon">
      <el-icon v-if="type === 'error'" class="icon-error"><CircleCloseFilled /></el-icon>
      <el-icon v-else-if="type === 'warning'" class="icon-warning"><WarningFilled /></el-icon>
      <el-icon v-else-if="type === 'info'" class="icon-info"><InfoFilled /></el-icon>
      <el-icon v-else-if="type === 'success'" class="icon-success"><Check /></el-icon>
    </div>
    
    <!-- 错误内容 -->
    <div class="error-content">
      <h3 v-if="title" class="error-title">{{ title }}</h3>
      <p class="error-message-text">{{ message }}</p>
      
      <!-- 操作按钮 -->
      <div v-if="actions && actions.length > 0" class="error-actions">
        <el-button
          v-for="(action, index) in actions"
          :key="index"
          :type="action.type || 'primary'"
          :size="size === 'large' ? 'default' : 'small'"
          @click="action.callback && action.callback()"
        >
          {{ action.label }}
        </el-button>
      </div>
    </div>
    
    <!-- 关闭按钮 -->
    <button
      v-if="closable"
      class="error-close"
      @click="$emit('close')"
      type="button"
    >
      <el-icon><Close /></el-icon>
    </button>
  </div>
</template>

<script setup lang="ts">
import { CircleCloseFilled, WarningFilled, InfoFilled, Check, Close } from '@element-plus/icons-vue'

const props = defineProps<{
  type: 'error' | 'warning' | 'info' | 'success'
  title?: string
  message: string
  actions?: Array<{
    label: string
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    callback?: () => void
  }>
  closable?: boolean
  size?: 'small' | 'medium' | 'large'
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()
</script>

<style scoped>
.error-message {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid transparent;
  position: relative;
  animation: fadeInUp 0.3s ease forwards;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);
}

/* 类型变体 */
.error-message.error {
  border-color: rgba(239, 68, 68, 0.3);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.2),
    0 0 20px rgba(239, 68, 68, 0.2);
}

.error-message.warning {
  border-color: rgba(245, 158, 11, 0.3);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.2),
    0 0 20px rgba(245, 158, 11, 0.2);
}

.error-message.info {
  border-color: rgba(59, 130, 246, 0.3);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.2),
    0 0 20px rgba(59, 130, 246, 0.2);
}

.error-message.success {
  border-color: rgba(16, 185, 129, 0.3);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.2),
    0 0 20px rgba(16, 185, 129, 0.2);
}

/* 大小变体 */
.error-message.small {
  padding: 12px;
  gap: 12px;
}

.error-message.medium {
  padding: 20px;
  gap: 16px;
}

.error-message.large {
  padding: 28px;
  gap: 20px;
}

/* 图标 */
.error-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.error-icon :deep(.el-icon) {
  font-size: 24px;
}

.error-message.small .error-icon :deep(.el-icon) {
  font-size: 20px;
}

.error-message.large .error-icon :deep(.el-icon) {
  font-size: 32px;
}

/* 图标颜色 */
.icon-error {
  color: #ef4444;
}

.icon-warning {
  color: #f59e0b;
}

.icon-info {
  color: #3b82f6;
}

.icon-success {
  color: #10b981;
}

/* 内容 */
.error-content {
  flex: 1;
  min-width: 0;
}

.error-title {
  font-size: 16px;
  font-weight: 700;
  color: #f8fafc;
  margin: 0 0 8px 0;
}

.error-message.small .error-title {
  font-size: 14px;
  margin-bottom: 6px;
}

.error-message.large .error-title {
  font-size: 18px;
  margin-bottom: 12px;
}

.error-message-text {
  font-size: 14px;
  color: #94a3b8;
  line-height: 1.5;
  margin: 0 0 16px 0;
}

.error-message.small .error-message-text {
  font-size: 13px;
  margin-bottom: 12px;
}

.error-message.large .error-message-text {
  font-size: 16px;
  margin-bottom: 20px;
}

/* 操作按钮 */
.error-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.error-message.small .error-actions {
  gap: 8px;
}

.error-actions :deep(.el-button) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.error-actions :deep(.el-button:hover) {
  transform: translateY(-2px);
}

/* 关闭按钮 */
.error-close {
  flex-shrink: 0;
  background: transparent;
  border: none;
  outline: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: all 0.3s ease;
  color: #64748b;
}

.error-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #f8fafc;
  transform: scale(1.1);
}

.error-close :deep(.el-icon) {
  font-size: 18px;
}

.error-message.small .error-close :deep(.el-icon) {
  font-size: 16px;
}

.error-message.large .error-close :deep(.el-icon) {
  font-size: 20px;
}

/* 动画 */
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

/* 响应式设计 */
@media (max-width: 768px) {
  .error-message {
    padding: 16px;
    gap: 12px;
  }
  
  .error-message.large {
    padding: 20px;
  }
  
  .error-title {
    font-size: 14px;
  }
  
  .error-message-text {
    font-size: 13px;
  }
  
  .error-actions {
    gap: 8px;
  }
  
  .error-actions :deep(.el-button) {
    font-size: 12px;
    padding: 6px 12px;
  }
}

@media (max-width: 480px) {
  .error-message {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .error-icon {
    margin-bottom: 12px;
  }
  
  .error-actions {
    width: 100%;
  }
  
  .error-actions :deep(.el-button) {
    flex: 1;
    text-align: center;
  }
}
</style>