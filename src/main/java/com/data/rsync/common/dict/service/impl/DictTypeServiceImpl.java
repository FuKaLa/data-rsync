package com.data.rsync.common.dict.service.impl;

import com.data.rsync.common.dict.entity.DictType;
import com.data.rsync.common.dict.mapper.DictTypeMapper;
import com.data.rsync.common.dict.service.DictTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典类型服务实现类
 */
@Service
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    private static final Logger logger = LoggerFactory.getLogger(DictTypeServiceImpl.class);

    @Override
    public DictType getById(Long id) {
        return super.getById(id);
    }

    @Override
    public DictType getByTypeCode(String typeCode) {
        return baseMapper.selectByTypeCode(typeCode);
    }

    @Override
    public List<DictType> getAll() {
        return baseMapper.selectAll();
    }

    @Override
    public List<DictType> getByCondition(DictType dictType) {
        return baseMapper.selectByCondition(dictType);
    }

    @Override
    public boolean add(DictType dictType) {
        try {
            dictType.setCreateTime(LocalDateTime.now());
            dictType.setStatus(1); // 默认启用
            return baseMapper.insert(dictType) > 0;
        } catch (Exception e) {
            logger.error("Error adding dict type: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean update(DictType dictType) {
        try {
            dictType.setUpdateTime(LocalDateTime.now());
            return baseMapper.update(dictType) > 0;
        } catch (Exception e) {
            logger.error("Error updating dict type: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            return baseMapper.deleteById(id) > 0;
        } catch (Exception e) {
            logger.error("Error deleting dict type: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        try {
            return baseMapper.deleteByIds(ids) > 0;
        } catch (Exception e) {
            logger.error("Error batch deleting dict types: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean enable(Long id) {
        try {
            DictType dictType = baseMapper.selectById(id);
            if (dictType != null) {
                dictType.setStatus(1);
                dictType.setUpdateTime(LocalDateTime.now());
                return baseMapper.update(dictType) > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error enabling dict type: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean disable(Long id) {
        try {
            DictType dictType = baseMapper.selectById(id);
            if (dictType != null) {
                dictType.setStatus(0);
                dictType.setUpdateTime(LocalDateTime.now());
                return baseMapper.update(dictType) > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error disabling dict type: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean checkTypeCodeExist(String typeCode, Long id) {
        return baseMapper.checkTypeCodeExist(typeCode, id) > 0;
    }
}
