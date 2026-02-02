import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
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
    }
  ]
})

export default router