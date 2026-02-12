package com.data.rsync.common.dict.service;

import com.data.rsync.common.dict.entity.DictType;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 字典类型服务接口
 */
public interface DictTypeService extends IService<DictType> {
    /**
     * 根据ID查询字典类型
     * @param id 主键
     * @return 字典类型
     */
    DictType getById(Long id);

    /**
     * 根据类型编码查询字典类型
     * @param typeCode 类型编码
     * @return 字典类型
     */
    DictType getByTypeCode(String typeCode);

    /**
     * 查询所有字典类型
     * @return 字典类型列表
     */
    List<DictType> getAll();

    /**
     * 根据条件查询字典类型
     * @param dictType 查询条件
     * @return 字典类型列表
     */
    List<DictType> getByCondition(DictType dictType);

    /**
     * 新增字典类型
     * @param dictType 字典类型
     * @return 操作结果
     */
    boolean add(DictType dictType);

    /**
     * 修改字典类型
     * @param dictType 字典类型
     * @return 操作结果
     */
    boolean update(DictType dictType);

    /**
     * 删除字典类型
     * @param id 主键
     * @return 操作结果
     */
    boolean delete(Long id);

    /**
     * 批量删除字典类型
     * @param ids 主键列表
     * @return 操作结果
     */
    boolean batchDelete(List<Long> ids);

    /**
     * 启用字典类型
     * @param id 主键
     * @return 操作结果
     */
    boolean enable(Long id);

    /**
     * 禁用字典类型
     * @param id 主键
     * @return 操作结果
     */
    boolean disable(Long id);

    /**
     * 检查类型编码是否存在
     * @param typeCode 类型编码
     * @param id 主键
     * @return 是否存在
     */
    boolean checkTypeCodeExist(String typeCode, Long id);
}
