<template>
  <div class="login-container">
    <!-- 背景效果 -->
    <div class="login-bg">
      <div class="grid-lines"></div>
      <div class="gradient-overlay"></div>
      <div class="particle-container"></div>
    </div>
    
    <!-- 登录表单 -->
    <div class="login-form">
      <!-- 品牌标识 -->
      <div class="brand">
        <div class="logo">
          <svg width="60" height="60" viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="60" height="60" rx="12" fill="url(#gradient-logo)"/>
            <path d="M20 20L40 20L40 40L20 40L20 20Z" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M20 30L40 30" stroke="white" stroke-width="2" stroke-linecap="round"/>
            <path d="M30 20L30 40" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <defs>
            <linearGradient id="gradient-logo" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#409eff"/>
              <stop offset="100%" stop-color="#667eea"/>
            </linearGradient>
          </defs>
        </div>
        <h1 class="title">数据同步系统</h1>
        <p class="subtitle">DATA RSYNC SYSTEM</p>
      </div>
      
      <!-- 表单内容 -->
      <form @submit.prevent="handleLogin" class="form">
        <div class="form-group">
          <div class="input-wrapper">
            <svg class="icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input 
              type="text" 
              v-model="username" 
              placeholder="用户名" 
              class="input"
              autocomplete="off"
            />
          </div>
        </div>
        
        <div class="form-group">
          <div class="input-wrapper">
            <svg class="icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 15V17M12 7V9M5 12H7M17 12H19M19 12C19 16.9706 14.9706 21 10 21C5.02944 21 1 16.9706 1 12C1 7.02944 5.02944 3 10 3C14.9706 3 19 7.02944 19 12Z" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input 
              type="password" 
              v-model="password" 
              placeholder="密码" 
              class="input"
              autocomplete="current-password"
            />
            <button 
              type="button" 
              class="toggle-password"
              @click="showPassword = !showPassword"
            >
              <svg v-if="!showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M9.88 9.88C10.7462 8.30377 12.2538 8.30377 13.12 9.88C13.9862 11.4562 13.9862 13.5438 13.12 15.12C12.2538 16.6962 10.7462 16.6962 9.88 15.12C9.01377 13.5438 9.01377 11.4562 9.88 9.88Z" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9.88 9.88C10.7462 8.30377 12.2538 8.30377 13.12 9.88C13.9862 11.4562 13.9862 13.5438 13.12 15.12C12.2538 16.6962 10.7462 16.6962 9.88 15.12C9.01377 13.5438 9.01377 11.4562 9.88 9.88Z" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M3 3L21 21" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>
        
        <div class="form-actions">
          <button 
            type="submit" 
            class="btn-primary"
            :disabled="isLoading"
          >
            <span v-if="!isLoading">登录</span>
            <span v-else class="loading-spinner"></span>
          </button>
        </div>
        
        <div v-if="error" class="error-message">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="12" y1="16" x2="12.01" y2="16" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ error }}
        </div>
      </form>
      
      <!-- 底部信息 -->
      <div class="footer">
        <p>© 2026 数据同步系统 | 科技驱动未来</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'

const username = ref('')
const password = ref('')
const error = ref('')
const isLoading = ref(false)
const showPassword = ref(false)
const router = useRouter()

const handleLogin = async () => {
  error.value = ''
  isLoading.value = true
  
  try {
    const response = await authApi.login({
      username: username.value,
      password: password.value
    })
    
    if (response && response.code === 200 && response.data) {
      sessionStorage.setItem('token', response.data)
      router.push('/dashboard')
    } else {
      error.value = response?.message || '登录失败'
    }
  } catch (err) {
    console.error('Login error:', err)
    error.value = '登录失败，请检查网络连接'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
/* 登录容器 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

/* 背景效果 */
.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

/* 网格线条 */
.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(64, 158, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(64, 158, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

/* 渐变覆盖层 */
.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    rgba(15, 23, 42, 0.8) 0%,
    rgba(17, 24, 39, 0.9) 50%,
    rgba(15, 23, 42, 0.8) 100%
  );
  background-size: 200% 200%;
  animation: gradientShift 15s ease infinite;
}

/* 粒子容器 */
.particle-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

/* 登录表单 */
.login-form {
  max-width: 420px;
  width: 100%;
  padding: 3rem 2.5rem;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(64, 158, 255, 0.1),
    0 0 10px rgba(64, 158, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 10;
  animation: fadeInUp 0.8s ease forwards;
  overflow: hidden;
}

/* 表单装饰线 */
.login-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #409eff, #667eea);
  animation: shine 3s infinite;
}

/* 品牌标识 */
.brand {
  text-align: center;
  margin-bottom: 2.5rem;
  animation: fadeInUp 0.8s ease 0.2s forwards;
  opacity: 0;
}

.logo {
  display: inline-block;
  margin-bottom: 1rem;
  animation: pulse 2s infinite;
}

.title {
  color: #ffffff;
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #409eff, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #94a3b8;
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
}

/* 表单 */
.form {
  animation: fadeInUp 0.8s ease 0.4s forwards;
  opacity: 0;
}

.form-group {
  margin-bottom: 1.5rem;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 0 1.25rem;
  transition: all 0.3s ease;
}

.input-wrapper:hover {
  border-color: rgba(64, 158, 255, 0.5);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.05);
}

.input-wrapper:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.icon {
  margin-right: 0.75rem;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within .icon {
  stroke: #409eff;
}

.input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #f8fafc;
  font-size: 1rem;
  padding: 1rem 0;
  transition: all 0.3s ease;
}

.input::placeholder {
  color: #64748b;
  transition: all 0.3s ease;
}

.input:focus::placeholder {
  color: #94a3b8;
}

.toggle-password {
  background: transparent;
  border: none;
  outline: none;
  cursor: pointer;
  padding: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.toggle-password:hover {
  transform: scale(1.1);
}

.toggle-password svg {
  transition: all 0.3s ease;
}

.toggle-password:hover svg {
  stroke: #409eff;
}

/* 表单操作 */
.form-actions {
  margin-top: 2rem;
  animation: fadeInUp 0.8s ease 0.6s forwards;
  opacity: 0;
}

.btn-primary {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #409eff, #667eea);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #338aff, #5a67d8);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.4);
}

.btn-primary:active {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 加载动画 */
.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
}

/* 错误信息 */
.error-message {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #ef4444;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  animation: fadeIn 0.3s ease forwards;
}

/* 底部信息 */
.footer {
  margin-top: 2rem;
  text-align: center;
  animation: fadeInUp 0.8s ease 0.8s forwards;
  opacity: 0;
}

.footer p {
  color: #94a3b8;
  font-size: 0.85rem;
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

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 50px 50px;
  }
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

@keyframes shine {
  0% {
    background-position: -100% 0;
  }
  100% {
    background-position: 100% 0;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-form {
    margin: 0 1.5rem;
    padding: 2.5rem 2rem;
  }
  
  .title {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .login-form {
    margin: 0 1rem;
    padding: 2rem 1.5rem;
  }
  
  .title {
    font-size: 1.3rem;
  }
  
  .subtitle {
    font-size: 0.8rem;
  }
  
  .input {
    font-size: 0.9rem;
  }
  
  .btn-primary {
    padding: 0.9rem;
    font-size: 1rem;
  }
}
</style>