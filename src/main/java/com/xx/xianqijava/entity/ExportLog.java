package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据导出记录实体
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("export_log")
public class ExportLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 导出ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 管理员用户名
     */
    private String adminName;

    /**
     * 报表类型: user/order/product
     */
    private String reportType;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 数据行数
     */
    private Integer rowCount;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 导出耗时（毫秒）
     */
    private Long duration;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
