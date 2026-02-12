import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/tokenManager'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue')
    },
    {
      path: '/data-source/list',
      name: 'DataSourceList',
      component: () => import('@/views/data-source/List.vue')
    },
    {
      path: '/data-source/create',
      name: 'DataSourceCreate',
      component: () => import('@/views/data-source/Create.vue')
    },
    {
      path: '/task/list',
      name: 'TaskList',
      component: () => import('@/views/task/List.vue')
    },
    {
      path: '/task/create',
      name: 'TaskCreate',
      component: () => import('@/views/task/Create.vue')
    },
    {
      path: '/task/error-data/:id',
      name: 'TaskErrorData',
      component: () => import('@/views/task/ErrorData.vue')
    },
    {
      path: '/task/detail/:id',
      name: 'TaskDetail',
      component: () => import('@/views/task/Detail.vue')
    },
    {
      path: '/monitor/dashboard',
      name: 'MonitorDashboard',
      component: () => import('@/views/monitor/Dashboard.vue')
    },
    {
      path: '/monitor/topology',
      name: 'MonitorTopology',
      component: () => import('@/views/monitor/Topology.vue')
    },
    {
      path: '/milvus/collections',
      name: 'MilvusCollections',
      component: () => import('@/views/milvus/Collections.vue')
    },
    {
      path: '/milvus/indexes',
      name: 'MilvusIndexes',
      component: () => import('@/views/milvus/Indexes.vue')
    },
    {
      path: '/milvus/health',
      name: 'MilvusHealth',
      component: () => import('@/views/milvus/Health.vue')
    },
    {
      path: '/system/users',
      name: 'SystemUsers',
      component: () => import('@/views/system/Users.vue')
    },
    {
      path: '/system/roles',
      name: 'SystemRoles',
      component: () => import('@/views/system/Roles.vue')
    },
    {
      path: '/system/logs',
      name: 'SystemLogs',
      component: () => import('@/views/system/Logs.vue')
    },
    {
      path: '/dict/type',
      name: 'DictTypeList',
      component: () => import('@/views/dict/DictTypeList.vue')
    },
    {
      path: '/dict/item',
      name: 'DictItemList',
      component: () => import('@/views/dict/DictItemList.vue')
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 不需要登录的路由
  const publicRoutes = ['/login']
  
  if (publicRoutes.includes(to.path)) {
    next()
    return
  }
  
  // 检查是否已登录
  const token = getToken()
  
  if (!token) {
    // 未登录，重定向到登录页面
    next('/login')
    return
  }
  
  // 已登录，允许访问
  next()
})

export default router