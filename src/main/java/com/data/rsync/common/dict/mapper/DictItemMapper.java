package com.data.rsync.common.dict.mapper;

import com.data.rsync.common.dict.entity.DictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 字典项Mapper接口
 */
@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {
    /**
     * 根据ID查询字典项
     * @param id 主键
     * @return 字典项
     */
    DictItem selectById(@Param("id") Long id);

    /**
     * 根据字典类型ID查询字典项
     * @param typeId 字典类型ID
     * @return 字典项列表
     */
    List<DictItem> selectByTypeId(@Param("typeId") Long typeId);

    /**
     * 根据字典类型编码查询字典项
     * @param typeCode 字典类型编码
     * @return 字典项列表
     */
    List<DictItem> selectByTypeCode(@Param("typeCode") String typeCode);

    /**
     * 根据字典类型编码和字典项编码查询字典项
     * @param typeCode 字典类型编码
     * @param itemCode 字典项编码
     * @return 字典项
     */
    DictItem selectByTypeCodeAndItemCode(@Param("typeCode") String typeCode, @Param("itemCode") String itemCode);

    /**
     * 查询所有字典项
     * @return 字典项列表
     */
    List<DictItem> selectAll();

    /**
     * 根据条件查询字典项
     * @param dictItem 查询条件
     * @return 字典项列表
     */
    List<DictItem> selectByCondition(DictItem dictItem);

    /**
     * 新增字典项
     * @param dictItem 字典项
     * @return 影响行数
     */
    int insert(DictItem dictItem);

    /**
     * 修改字典项
     * @param dictItem 字典项
     * @return 影响行数
     */
    int update(DictItem dictItem);

    /**
     * 删除字典项
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除字典项
     * @param ids 主键列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据字典类型ID删除字典项
     * @param typeId 字典类型ID
     * @return 影响行数
     */
    int deleteByTypeId(@Param("typeId") Long typeId);

    /**
     * 检查字典项编码是否存在
     * @param typeId 字典类型ID
     * @param itemCode 字典项编码
     * @param id 主键
     * @return 存在数量
     */
    int checkItemCodeExist(@Param("typeId") Long typeId, @Param("itemCode") String itemCode, @Param("id") Long id);
}
