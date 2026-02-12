package com.data.rsync.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 简化的任务实体类，用于测试编译问题
 */
@Data
@TableName("task")
public class SimpleTaskEntity {

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    @TableField(value = "name", exist = true)
    private String name;

    /**
     * 任务类型
     */
    @TableField(value = "type", exist = true)
    private String type;

    /**
     * 数据源ID
     */
    @TableField(value = "data_source_id", exist = true)
    private Long dataSourceId;

    /**
     * 任务状态
     */
    @TableField(value = "status", exist = true)
    private String status;

    /**
     * 启用状态
     */
    @TableField(value = "enabled", exist = true)
    private Boolean enabled;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", exist = true)
    private LocalDateTime updateTime;

    /**
     * 构造方法
     */
    public SimpleTaskEntity() {
        this.enabled = true;
        this.status = "PENDING";
        this.dataSourceId = 1L;
    }
}