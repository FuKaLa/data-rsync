package com.data.rsync.common.dict.mapper;

import com.data.rsync.common.dict.entity.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 字典类型Mapper接口
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {
    /**
     * 根据ID查询字典类型
     * @param id 主键
     * @return 字典类型
     */
    DictType selectById(@Param("id") Long id);

    /**
     * 根据类型编码查询字典类型
     * @param typeCode 类型编码
     * @return 字典类型
     */
    DictType selectByTypeCode(@Param("typeCode") String typeCode);

    /**
     * 查询所有字典类型
     * @return 字典类型列表
     */
    List<DictType> selectAll();

    /**
     * 根据条件查询字典类型
     * @param dictType 查询条件
     * @return 字典类型列表
     */
    List<DictType> selectByCondition(DictType dictType);

    /**
     * 新增字典类型
     * @param dictType 字典类型
     * @return 影响行数
     */
    int insert(DictType dictType);

    /**
     * 修改字典类型
     * @param dictType 字典类型
     * @return 影响行数
     */
    int update(DictType dictType);

    /**
     * 删除字典类型
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除字典类型
     * @param ids 主键列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 检查类型编码是否存在
     * @param typeCode 类型编码
     * @param id 主键
     * @return 存在数量
     */
    int checkTypeCodeExist(@Param("typeCode") String typeCode, @Param("id") Long id);
}
