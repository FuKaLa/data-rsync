package com.data.rsync.common.utils;

import com.data.rsync.common.config.NacosConfig;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.collection.FieldType;
import io.milvus.grpc.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Milvus工具类
 */
public class MilvusUtils {

    /**
     * 创建Milvus客户端
     * @param host 主机地址
     * @param port 端口
     * @param token 令牌
     * @return Milvus客户端
     */
    public static MilvusClient createMilvusClient(String host, int port, String token) {
        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port);
        
        if (token != null && !token.isEmpty()) {
            builder.withToken(token);
        }
        
        return new MilvusServiceClient(builder.build());
    }

    /**
     * 从Nacos配置创建Milvus客户端
     * @return Milvus客户端
     */
    public static MilvusClient createMilvusClient() {
        NacosConfig.MilvusConfig milvusConfig = ConfigUtils.getMilvusConfig();
        if (milvusConfig == null) {
            throw new IllegalArgumentException("Milvus config not found in Nacos");
        }
        
        return createMilvusClient(
                milvusConfig.getHost(),
                milvusConfig.getPort(),
                milvusConfig.getPassword() // 使用password作为token
        );
    }

    /**
     * 关闭Milvus客户端
     * @param client Milvus客户端
     */
    public static void closeMilvusClient(MilvusClient client) {
        if (client != null && client instanceof MilvusServiceClient) {
            try {
                ((MilvusServiceClient) client).close();
            } catch (Exception e) {
                // 忽略关闭异常
            }
        }
    }

    /**
     * 创建默认的字段类型列表
     * @param vectorDimension 向量维度
     * @return 字段类型列表
     */
    public static List<FieldType> createDefaultFields(int vectorDimension) {
        List<FieldType> fields = new ArrayList<>();
        
        // 主键字段
        FieldType idField = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build();
        fields.add(idField);
        
        // 向量字段
        FieldType vectorField = FieldType.newBuilder()
                .withName("vector")
                .withDataType(DataType.FloatVector)
                .withDimension(vectorDimension)
                .build();
        fields.add(vectorField);
        
        // 标量字段
        FieldType textField = FieldType.newBuilder()
                .withName("text")
                .withDataType(DataType.VarChar)
                .withMaxLength(65535)
                .build();
        fields.add(textField);
        
        return fields;
    }

    /**
     * 从Nacos配置创建默认的字段类型列表
     * @return 字段类型列表
     */
    public static List<FieldType> createDefaultFields() {
        NacosConfig.MilvusConfig milvusConfig = ConfigUtils.getMilvusConfig();
        if (milvusConfig == null) {
            throw new IllegalArgumentException("Milvus config not found in Nacos");
        }
        return createDefaultFields(milvusConfig.getVectorDimension());
    }

    /**
     * 获取默认的索引类型
     * @return 索引类型
     */
    public static IndexType getDefaultIndexType() {
        return IndexType.IVF_FLAT;
    }

    /**
     * 获取默认的度量类型
     * @return 度量类型
     */
    public static MetricType getDefaultMetricType() {
        return MetricType.L2;
    }

    /**
     * 构建默认的索引参数
     * @param nlist IVF_FLAT索引参数
     * @return 索引参数
     */
    public static String buildDefaultIndexParams(int nlist) {
        return String.format("{\"nlist\": %d}", nlist);
    }

    /**
     * 构建默认的搜索参数
     * @param nprobe 搜索参数
     * @return 搜索参数
     */
    public static String buildDefaultSearchParams(int nprobe) {
        return String.format("{\"nprobe\": %d}", nprobe);
    }

} 
