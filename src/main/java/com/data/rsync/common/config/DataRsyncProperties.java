package com.data.rsync.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据同步系统配置类
 */
@Component
@ConfigurationProperties(prefix = "data-rsync")
public class DataRsyncProperties {

    private VectorDb vectorDb;
    private Data data;

    public VectorDb getVectorDb() {
        return vectorDb;
    }

    public void setVectorDb(VectorDb vectorDb) {
        this.vectorDb = vectorDb;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    /**
     * 向量数据库配置
     */
    public static class VectorDb {
        private Milvus milvus;

        public Milvus getMilvus() {
            return milvus;
        }

        public void setMilvus(Milvus milvus) {
            this.milvus = milvus;
        }

        /**
         * Milvus配置
         */
        public static class Milvus {
            private String host;
            private Integer port;
            private Collection collection;
            private Vector vector;
            private ThreadPool threadPool;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public Integer getPort() {
                return port;
            }

            public void setPort(Integer port) {
                this.port = port;
            }

            public Collection getCollection() {
                return collection;
            }

            public void setCollection(Collection collection) {
                this.collection = collection;
            }

            public Vector getVector() {
                return vector;
            }

            public void setVector(Vector vector) {
                this.vector = vector;
            }

            public ThreadPool getThreadPool() {
                return threadPool;
            }

            public void setThreadPool(ThreadPool threadPool) {
                this.threadPool = threadPool;
            }

            /**
             * 集合配置
             */
            public static class Collection {
                private String name;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }
            }

            /**
             * 向量配置
             */
            public static class Vector {
                private Integer dimension;

                public Integer getDimension() {
                    return dimension;
                }

                public void setDimension(Integer dimension) {
                    this.dimension = dimension;
                }
            }

            /**
             * 线程池配置
             */
            public static class ThreadPool {
                private Integer coreSize;
                private Integer maxSize;
                private Integer queueCapacity;
                private Integer keepAliveSeconds;

                public Integer getCoreSize() {
                    return coreSize;
                }

                public void setCoreSize(Integer coreSize) {
                    this.coreSize = coreSize;
                }

                public Integer getMaxSize() {
                    return maxSize;
                }

                public void setMaxSize(Integer maxSize) {
                    this.maxSize = maxSize;
                }

                public Integer getQueueCapacity() {
                    return queueCapacity;
                }

                public void setQueueCapacity(Integer queueCapacity) {
                    this.queueCapacity = queueCapacity;
                }

                public Integer getKeepAliveSeconds() {
                    return keepAliveSeconds;
                }

                public void setKeepAliveSeconds(Integer keepAliveSeconds) {
                    this.keepAliveSeconds = keepAliveSeconds;
                }
            }
        }
    }

    /**
     * 数据处理配置
     */
    public static class Data {
        private Process process;

        public Process getProcess() {
            return process;
        }

        public void setProcess(Process process) {
            this.process = process;
        }

        /**
         * 数据处理配置
         */
        public static class Process {
            private Integer batchSize;
            private Integer timeoutSeconds;
            private ThreadPool threadPool;

            public Integer getBatchSize() {
                return batchSize;
            }

            public void setBatchSize(Integer batchSize) {
                this.batchSize = batchSize;
            }

            public Integer getTimeoutSeconds() {
                return timeoutSeconds;
            }

            public void setTimeoutSeconds(Integer timeoutSeconds) {
                this.timeoutSeconds = timeoutSeconds;
            }

            public ThreadPool getThreadPool() {
                return threadPool;
            }

            public void setThreadPool(ThreadPool threadPool) {
                this.threadPool = threadPool;
            }

            /**
             * 线程池配置
             */
            public static class ThreadPool {
                private Integer coreSize;
                private Integer maxSize;
                private Integer queueCapacity;
                private Integer keepAliveSeconds;

                public Integer getCoreSize() {
                    return coreSize;
                }

                public void setCoreSize(Integer coreSize) {
                    this.coreSize = coreSize;
                }

                public Integer getMaxSize() {
                    return maxSize;
                }

                public void setMaxSize(Integer maxSize) {
                    this.maxSize = maxSize;
                }

                public Integer getQueueCapacity() {
                    return queueCapacity;
                }

                public void setQueueCapacity(Integer queueCapacity) {
                    this.queueCapacity = queueCapacity;
                }

                public Integer getKeepAliveSeconds() {
                    return keepAliveSeconds;
                }

                public void setKeepAliveSeconds(Integer keepAliveSeconds) {
                    this.keepAliveSeconds = keepAliveSeconds;
                }
            }
        }
    }
}
