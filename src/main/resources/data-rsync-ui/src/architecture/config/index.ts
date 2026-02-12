// 应用配置管理

// 环境变量配置
export const envConfig = {
  // API基础URL
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8889/data-rsync',
  
  // 应用标题
  appTitle: import.meta.env.VITE_APP_TITLE || 'Data Rsync System',
  
  // 应用环境
  appEnv: import.meta.env.VITE_APP_ENV || 'development',
  
  // 应用版本
  appVersion: import.meta.env.VITE_APP_VERSION || '1.0.0',
  
  // 端口
  port: import.meta.env.VITE_PORT || '3000'
} as const

// API配置
export const apiConfig = {
  // 请求超时时间（毫秒）
  timeout: 30000,
  
  // 重试次数
  retryCount: 3,
  
  // 重试间隔（毫秒）
  retryInterval: 1000,
  
  // 是否启用缓存
  enableCache: true,
  
  // 缓存过期时间（毫秒）
  cacheExpire: 5 * 60 * 1000,
  
  // 最大并发请求数
  maxConcurrentRequests: 10
} as const

// 路由配置
export const routerConfig = {
  // 是否启用路由守卫
  enableGuard: true,
  
  // 登录路由
  loginRoute: '/login',
  
  // 首页路由
  homeRoute: '/dashboard',
  
  // 不需要登录的路由
  publicRoutes: ['/login', '/404', '/500'],
  
  // 路由模式
  mode: 'history' as const
} as const

// 存储配置
export const storageConfig = {
  // 本地存储键前缀
  prefix: 'data-rsync-',
  
  // 本地存储过期时间（毫秒）
  expire: 7 * 24 * 60 * 60 * 1000,
  
  // 是否启用加密
  enableEncryption: true
} as const

// UI配置
export const uiConfig = {
  // 主题
  theme: 'default' as const,
  
  // 布局
  layout: 'side' as const,
  
  // 是否显示侧边栏
  showSidebar: true,
  
  // 是否显示顶部导航
  showHeader: true,
  
  // 是否显示面包屑
  showBreadcrumb: true,
  
  // 是否显示标签页
  showTabs: true,
  
  // 是否启用响应式
  responsive: true,
  
  // 动画效果
  animation: {
    enable: true,
    duration: 300
  },
  
  // 表格配置
  table: {
    // 默认页码
    defaultPage: 1,
    // 默认每页条数
    defaultSize: 10,
    // 分页选项
    pageSizes: [10, 20, 50, 100]
  },
  
  // 表单配置
  form: {
    // 是否启用自动保存
    autoSave: true,
    // 自动保存间隔（毫秒）
    autoSaveInterval: 3000
  }
} as const

// 国际化配置
export const i18nConfig = {
  // 默认语言
  defaultLocale: 'zh-CN',
  
  // 支持的语言
  locales: ['zh-CN', 'en-US'],
  
  // 是否启用自动检测语言
  autoDetect: true
} as const

// 日志配置
export const logConfig = {
  // 日志级别
  level: envConfig.appEnv === 'production' ? 'warn' : 'debug',
  
  // 是否启用控制台日志
  enableConsole: true,
  
  // 是否启用网络日志
  enableNetwork: true,
  
  // 是否启用错误日志
  enableError: true,
  
  // 是否启用性能日志
  enablePerformance: envConfig.appEnv !== 'production'
} as const

// 安全配置
export const securityConfig = {
  // 是否启用XSS防护
  enableXssProtection: true,
  
  // 是否启用CSRF防护
  enableCsrfProtection: true,
  
  // 是否启用请求签名
  enableRequestSign: true,
  
  // 是否启用敏感信息脱敏
  enableDesensitization: true,
  
  // 令牌过期时间（毫秒）
  tokenExpire: 24 * 60 * 60 * 1000
} as const

// 监控配置
export const monitorConfig = {
  // 是否启用性能监控
  enablePerformance: envConfig.appEnv !== 'production',
  
  // 是否启用错误监控
  enableError: true,
  
  // 是否启用用户行为监控
  enableUserBehavior: envConfig.appEnv !== 'production',
  
  // 采样率
  sampleRate: envConfig.appEnv === 'production' ? 0.1 : 1
} as const

// 功能配置
export const featureConfig = {
  // 是否启用数据源管理
  enableDataSource: true,
  
  // 是否启用任务管理
  enableTask: true,
  
  // 是否启用监控管理
  enableMonitor: true,
  
  // 是否启用Milvus管理
  enableMilvus: true,
  
  // 是否启用系统管理
  enableSystem: true,
  
  // 是否启用向量化功能
  enableVectorization: true,
  
  // 是否启用数据诊断
  enableDataDiagnose: true,
  
  // 是否启用批量操作
  enableBatchOperation: true
} as const

// 导出默认配置
export default {
  env: envConfig,
  api: apiConfig,
  router: routerConfig,
  storage: storageConfig,
  ui: uiConfig,
  i18n: i18nConfig,
  log: logConfig,
  security: securityConfig,
  monitor: monitorConfig,
  feature: featureConfig
}
