package com.data.rsync.common.dict.service.impl;

import com.data.rsync.common.dict.entity.DictItem;
import com.data.rsync.common.dict.mapper.DictItemMapper;
import com.data.rsync.common.dict.service.DictItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemService {

    private static final Logger log = LoggerFactory.getLogger(DictItemServiceImpl.class);

    @Override
    public List<DictItem> getByTypeCode(String typeCode) {
        try {
            return baseMapper.selectByTypeCode(typeCode);
        } catch (Exception e) {
            log.error("根据字典类型编码获取字典项列表失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean checkItemCodeExist(Long typeId, String itemCode, Long excludeId) {
        try {
            return baseMapper.checkItemCodeExist(typeId, itemCode, excludeId) > 0;
        } catch (Exception e) {
            log.error("检查字典项编码是否存在失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean enableDictItem(Long id) {
        try {
            DictItem dictItem = baseMapper.selectById(id);
            if (dictItem == null) {
                log.warn("字典项不存在: {}", id);
                return false;
            }
            dictItem.setStatus(1);
            return updateById(dictItem);
        } catch (Exception e) {
            log.error("启用字典项失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean disableDictItem(Long id) {
        try {
            DictItem dictItem = baseMapper.selectById(id);
            if (dictItem == null) {
                log.warn("字典项不存在: {}", id);
                return false;
            }
            dictItem.setStatus(0);
            return updateById(dictItem);
        } catch (Exception e) {
            log.error("禁用字典项失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public DictItem getByTypeCodeAndItemCode(String typeCode, String itemCode) {
        try {
            return baseMapper.selectByTypeCodeAndItemCode(typeCode, itemCode);
        } catch (Exception e) {
            log.error("根据字典类型编码和字典项编码获取字典项失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
