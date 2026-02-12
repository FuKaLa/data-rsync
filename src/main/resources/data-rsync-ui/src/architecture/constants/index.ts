// 全局常量定义

// API路径常量
export const API_PATH = {
  // 认证相关
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    CURRENT_USER: '/auth/current-user'
  },
  
  // 数据源相关
  DATA_SOURCE: {
    LIST: '/data-source',
    DETAIL: (id: number) => `/data-source/${id}`,
    CREATE: '/data-source',
    UPDATE: (id: number) => `/data-source/${id}`,
    DELETE: (id: number) => `/data-source/${id}`,
    TEST_CONNECTION: (id: number) => `/data-source/${id}/test-connection`,
    ENABLE: (id: number) => `/data-source/${id}/enable`,
    HEALTH: (id: number) => `/data-source/${id}/health`,
    BATCH_HEALTH: '/data-source/batch-check-health',
    BY_TYPE: (type: string) => `/data-source/type/${type}`,
    BY_ENABLED: (enabled: boolean) => `/data-source/enabled/${enabled}`,
    
    // 模板相关
    TEMPLATES: '/data-source/templates',
    TEMPLATES_BY_TYPE: (type: string) => `/data-source/templates/type/${type}`,
    SYSTEM_TEMPLATES: '/data-source/templates/system',
    CREATE_TEMPLATE: '/data-source/templates',
    UPDATE_TEMPLATE: (id: number) => `/data-source/templates/${id}`,
    DELETE_TEMPLATE: (id: number) => `/data-source/templates/${id}`,
    INIT_SYSTEM_TEMPLATES: '/data-source/templates/init-system',
    
    // 诊断相关
    DIAGNOSE: (id: number) => `/data-source/${id}/diagnose`,
    DIAGNOSE_REPORTS: (id: number) => `/data-source/${id}/diagnose-reports`,
    LATEST_DIAGNOSE_REPORT: (id: number) => `/data-source/${id}/latest-diagnose-report`
  },
  
  // 任务相关
  TASK: {
    LIST: '/tasks',
    DETAIL: (id: number) => `/tasks/${id}`,
    CREATE: '/tasks',
    UPDATE: '/tasks',
    DELETE: (id: number) => `/tasks/${id}`,
    TRIGGER: (id: number) => `/tasks/${id}/trigger`,
    PAUSE: (id: number) => `/tasks/${id}/pause`,
    RESUME: (id: number) => `/tasks/${id}/resume`,
    ROLLBACK: (id: number) => `/tasks/${id}/rollback`,
    VERSIONS: (id: number) => `/tasks/${id}/versions`,
    ERROR_DATA: (id: number) => `/tasks/${id}/error-data`,
    
    // 节点相关
    NODES: (taskId: number) => `/tasks/${taskId}/nodes`,
    CONNECTIONS: (taskId: number) => `/tasks/${taskId}/connections`,
    DEPENDENCIES: (taskId: number) => `/tasks/${taskId}/dependencies`,
    
    // 流程相关
    VALIDATE_FLOW: (taskId: number) => `/tasks/${taskId}/validate-flow`,
    
    // 向量化相关
    VECTORIZATION_CONFIG: '/tasks/vectorization-config',
    VECTORIZATION_CONFIG_BY_TASK: (taskId: number) => `/tasks/${taskId}/vectorization-config`,
    VECTORIZATION_CONFIG_BY_ID: (id: number) => `/tasks/vectorization-config/${id}`,
    VECTORIZATION_PREVIEW: '/tasks/vectorization-config/preview'
  },
  
  // 监控相关
  MONITOR: {
    METRICS: '/monitor/metrics',
    JVM_METRICS: '/monitor/metrics/jvm',
    SYSTEM_METRICS: '/monitor/metrics/system',
    DATASOURCE_METRICS: '/monitor/metrics/datasource',
    TASK_METRICS: '/monitor/metrics/task',
    MILVUS_METRICS: '/monitor/metrics/milvus'
  },
  
  // Milvus相关
  MILVUS: {
    COLLECTIONS: '/api/milvus/collections',
    INDEXES: (collectionName: string) => `/api/milvus/indexes/${collectionName}`,
    CREATE_INDEX: (collectionName: string) => `/api/milvus/indexes/${collectionName}`,
    DELETE_INDEX: (collectionName: string, indexName: string) => `/api/milvus/indexes/${collectionName}/${indexName}`,
    HEALTH: '/api/milvus/health',
    OPTIMIZE: (collectionName: string) => `/api/milvus/optimize/${collectionName}`,
    COLLECTION_STATS: (collectionName: string) => `/api/milvus/collections/${collectionName}/stats`,
    CREATE_COLLECTION: '/api/milvus/collections',
    DELETE_COLLECTION: (collectionName: string) => `/api/milvus/collections/${collectionName}`,
    INSERT_DATA: (collectionName: string) => `/api/milvus/collections/${collectionName}/insert`,
    DELETE_DATA: (collectionName: string) => `/api/milvus/collections/${collectionName}/delete`,
    SEARCH_DATA: (collectionName: string) => `/api/milvus/collections/${collectionName}/search`,
    BATCH: '/api/milvus/batch',
    CONSISTENCY: (collectionName: string) => `/api/milvus/consistency/${collectionName}`
  },
  
  // 系统相关
  SYSTEM: {
    USERS: '/api/system/users',
    USER: (id: number) => `/api/system/users/${id}`,
    USER_ROLES: (userId: number) => `/api/system/users/${userId}/roles`,
    ROLES: '/api/system/roles',
    ROLE: (id: number) => `/api/system/roles/${id}`,
    ROLE_PERMISSIONS: (roleId: number) => `/api/system/roles/${roleId}/permissions`,
    LOGS: '/api/system/logs',
    CONFIG: '/api/system/config',
    CACHE_REFRESH: '/api/system/cache/refresh',
    LOGS_CLEAN: '/api/system/logs/clean',
    INFO: '/api/system/info'
  }
} as const

// 任务状态常量
export const TASK_STATUS = {
  PENDING: 'pending',
  RUNNING: 'running',
  SUCCESS: 'success',
  FAILED: 'failed',
  PAUSED: 'paused',
  STOPPED: 'stopped'
} as const

// 数据源类型常量
export const DATA_SOURCE_TYPE = {
  MYSQL: 'MySQL',
  POSTGRESQL: 'PostgreSQL',
  ORACLE: 'Oracle',
  SQL_SERVER: 'SQL Server',
  MONGODB: 'MongoDB',
  REDIS: 'Redis',
  ELasticSEARCH: 'Elasticsearch',
  KAFKA: 'Kafka',
  HBASE: 'HBase',
  HIVE: 'Hive'
} as const

// 日志监控类型常量
export const LOG_MONITOR_TYPE = {
  FILE: 'file',
  DATABASE: 'database',
  KAFKA: 'kafka',
  API: 'api'
} as const

// 向量化算法常量
export const VECTORIZATION_ALGORITHM = {
  BERT: 'bert',
  WORD2VEC: 'word2vec',
  GLOVE: 'glove',
  FASTTEXT: 'fasttext',
  SENTENCE_BERT: 'sentence-bert',
  CLIP: 'clip'
} as const

// Milvus索引类型常量
export const MILVUS_INDEX_TYPE = {
  FLAT: 'FLAT',
  IVF_FLAT: 'IVF_FLAT',
  IVF_SQ8: 'IVF_SQ8',
  IVF_PQ: 'IVF_PQ',
  HNSW: 'HNSW',
  ANNOY: 'ANNOY',
  RHNSW_FLAT: 'RHNSW_FLAT',
  RHNSW_SQ: 'RHNSW_SQ',
  RHNSW_PQ: 'RHNSW_PQ',
  DISKANN: 'DISKANN'
} as const

// Milvus度量类型常量
export const MILVUS_METRIC_TYPE = {
  L2: 'L2',
  IP: 'IP',
  COSINE: 'COSINE'
} as const

// 分页默认值
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_SIZE: 10,
  MAX_SIZE: 100
} as const

// 缓存键常量
export const CACHE_KEY = {
  USER_INFO: 'user_info',
  TOKEN: 'token',
  DATA_SOURCES: 'data_sources',
  TASKS: 'tasks',
  SYSTEM_CONFIG: 'system_config'
} as const

// 正则表达式常量
export const REGEX = {
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^1[3-9]\d{9}$/,
  PASSWORD: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
  URL: /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/
} as const

// 错误码常量
export const ERROR_CODE = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
  
  // 业务错误码
  BUSINESS_ERROR: 1000,
  VALIDATION_ERROR: 1001,
  DATABASE_ERROR: 1002,
  NETWORK_ERROR: 1003,
  AUTH_ERROR: 1004,
  PERMISSION_ERROR: 1005,
  RESOURCE_ERROR: 1006,
  TASK_ERROR: 1007,
  DATA_SOURCE_ERROR: 1008,
  MILVUS_ERROR: 1009
} as const

// 消息常量
export const MESSAGE = {
  // 成功消息
  SUCCESS: {
    CREATE: '创建成功',
    UPDATE: '更新成功',
    DELETE: '删除成功',
    SAVE: '保存成功',
    SUBMIT: '提交成功',
    IMPORT: '导入成功',
    EXPORT: '导出成功',
    UPLOAD: '上传成功',
    DOWNLOAD: '下载成功',
    LOGIN: '登录成功',
    LOGOUT: '登出成功',
    ENABLE: '启用成功',
    DISABLE: '禁用成功',
    TEST_CONNECTION: '连接测试成功',
    HEALTH_CHECK: '健康检查成功',
    TASK_TRIGGER: '任务触发成功',
    TASK_PAUSE: '任务暂停成功',
    TASK_RESUME: '任务恢复成功',
    TASK_ROLLBACK: '任务回滚成功',
    INDEX_CREATE: '索引创建成功',
    INDEX_DELETE: '索引删除成功',
    COLLECTION_CREATE: '集合创建成功',
    COLLECTION_DELETE: '集合删除成功',
    DATA_INSERT: '数据插入成功',
    DATA_DELETE: '数据删除成功',
    DATA_SEARCH: '数据搜索成功',
    OPTIMIZE: '优化成功'
  },
  
  // 错误消息
  ERROR: {
    CREATE: '创建失败',
    UPDATE: '更新失败',
    DELETE: '删除失败',
    SAVE: '保存失败',
    SUBMIT: '提交失败',
    IMPORT: '导入失败',
    EXPORT: '导出失败',
    UPLOAD: '上传失败',
    DOWNLOAD: '下载失败',
    LOGIN: '登录失败',
    LOGOUT: '登出失败',
    ENABLE: '启用失败',
    DISABLE: '禁用失败',
    TEST_CONNECTION: '连接测试失败',
    HEALTH_CHECK: '健康检查失败',
    TASK_TRIGGER: '任务触发失败',
    TASK_PAUSE: '任务暂停失败',
    TASK_RESUME: '任务恢复失败',
    TASK_ROLLBACK: '任务回滚失败',
    INDEX_CREATE: '索引创建失败',
    INDEX_DELETE: '索引删除失败',
    COLLECTION_CREATE: '集合创建失败',
    COLLECTION_DELETE: '集合删除失败',
    DATA_INSERT: '数据插入失败',
    DATA_DELETE: '数据删除失败',
    DATA_SEARCH: '数据搜索失败',
    OPTIMIZE: '优化失败',
    NETWORK: '网络请求失败',
    SERVER: '服务器内部错误',
    VALIDATION: '数据验证失败',
    PERMISSION: '权限不足',
    RESOURCE: '资源不存在',
    DATABASE: '数据库操作失败',
    AUTH: '认证失败',
    TASK: '任务执行失败',
    DATA_SOURCE: '数据源操作失败',
    MILVUS: 'Milvus操作失败'
  },
  
  // 提示消息
  TIP: {
    CONFIRM_DELETE: '确定要删除吗？',
    CONFIRM_SUBMIT: '确定要提交吗？',
    CONFIRM_CANCEL: '确定要取消吗？',
    CONFIRM_LOGOUT: '确定要登出吗？',
    CONFIRM_ENABLE: '确定要启用吗？',
    CONFIRM_DISABLE: '确定要禁用吗？',
    CONFIRM_TASK_TRIGGER: '确定要触发任务吗？',
    CONFIRM_TASK_PAUSE: '确定要暂停任务吗？',
    CONFIRM_TASK_RESUME: '确定要恢复任务吗？',
    CONFIRM_TASK_ROLLBACK: '确定要回滚任务吗？',
    CONFIRM_INDEX_CREATE: '确定要创建索引吗？',
    CONFIRM_INDEX_DELETE: '确定要删除索引吗？',
    CONFIRM_COLLECTION_CREATE: '确定要创建集合吗？',
    CONFIRM_COLLECTION_DELETE: '确定要删除集合吗？',
    CONFIRM_DATA_INSERT: '确定要插入数据吗？',
    CONFIRM_DATA_DELETE: '确定要删除数据吗？',
    LOADING: '加载中...',
    PROCESSING: '处理中...',
    SUCCESS: '操作成功',
    FAILED: '操作失败',
    PLEASE_WAIT: '请稍候...',
    PLEASE_CHECK: '请检查输入...',
    PLEASE_SELECT: '请选择...',
    PLEASE_ENTER: '请输入...'
  }
} as const
