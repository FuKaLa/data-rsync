package com.data.rsync.task.manager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.manager.entity.TaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务仓库接口
 * 使用MyBatis Plus的BaseMapper特性，提供完整的CRUD操作
 */
@Mapper
public interface TaskRepository extends BaseMapper<TaskEntity> {

}

