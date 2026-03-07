package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据统计缓存实体
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("statistics_cache")
public class StatisticsCache implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缓存ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 缓存键
     */
    private String cacheKey;

    /**
     * 缓存数据（JSON格式）
     */
    private String cacheData;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
