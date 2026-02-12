package com.data.rsync.common.dict.service;

import com.data.rsync.common.dict.entity.DictItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DictItemService extends IService<DictItem> {
    /**
     * 根据字典类型编码获取字典项列表
     * @param typeCode 字典类型编码
     * @return 字典项列表
     */
    List<DictItem> getByTypeCode(String typeCode);

    /**
     * 检查字典项编码是否存在
     * @param typeId 字典类型ID
     * @param itemCode 字典项编码
     * @param excludeId 排除的ID（用于编辑时）
     * @return 是否存在
     */
    boolean checkItemCodeExist(Long typeId, String itemCode, Long excludeId);

    /**
     * 启用字典项
     * @param id 字典项ID
     * @return 是否成功
     */
    boolean enableDictItem(Long id);

    /**
     * 禁用字典项
     * @param id 字典项ID
     * @return 是否成功
     */
    boolean disableDictItem(Long id);

    /**
     * 根据字典类型编码和字典项编码获取字典项
     * @param typeCode 字典类型编码
     * @param itemCode 字典项编码
     * @return 字典项
     */
    DictItem getByTypeCodeAndItemCode(String typeCode, String itemCode);
}
