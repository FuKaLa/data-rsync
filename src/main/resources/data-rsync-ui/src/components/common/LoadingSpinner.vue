<template>
  <div class="loading-spinner" :class="{ 'fullscreen': fullscreen }">
    <div class="spinner-container" :class="size">
      <div class="spinner"></div>
      <div v-if="text" class="spinner-text">{{ text }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  fullscreen?: boolean
  text?: string
  size?: 'small' | 'medium' | 'large'
}>()
</script>

<style scoped>
/* 基础样式 */
.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 9999;
}

/* 全屏模式 */
.loading-spinner.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(4px);
}

/* 容器 */
.spinner-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(59, 130, 246, 0.2),
    0 0 20px rgba(59, 130, 246, 0.3);
  border: 1px solid rgba(59, 130, 246, 0.2);
  animation: fadeInUp 0.3s ease forwards;
}

/* 大小变体 */
.spinner-container.small {
  padding: 16px;
  gap: 12px;
}

.spinner-container.medium {
  padding: 24px;
  gap: 16px;
}

.spinner-container.large {
  padding: 32px;
  gap: 20px;
}

/* 加载动画 */
.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  position: relative;
}

/* 大小变体的动画尺寸 */
.spinner-container.small .spinner {
  width: 24px;
  height: 24px;
  border-width: 2px;
}

.spinner-container.medium .spinner {
  width: 32px;
  height: 32px;
  border-width: 2.5px;
}

.spinner-container.large .spinner {
  width: 48px;
  height: 48px;
  border-width: 3px;
}

/* 加载文本 */
.spinner-text {
  font-size: 14px;
  color: #94a3b8;
  font-weight: 500;
  text-align: center;
  line-height: 1.4;
}

/* 大小变体的文本尺寸 */
.spinner-container.small .spinner-text {
  font-size: 12px;
}

.spinner-container.medium .spinner-text {
  font-size: 14px;
}

.spinner-container.large .spinner-text {
  font-size: 16px;
}

/* 动画 */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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

/* 响应式设计 */
@media (max-width: 768px) {
  .spinner-container {
    margin: 0 20px;
  }
  
  .spinner-container.large {
    padding: 24px;
  }
  
  .spinner-container.large .spinner {
    width: 40px;
    height: 40px;
  }
}
</style>