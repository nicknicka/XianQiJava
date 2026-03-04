package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知阅读记录表
 */
@Data
@EqualsAndHashCode
@TableName("notification_read_records")
@Schema(description = "通知阅读记录")
public class NotificationReadRecord {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "通知ID")
    private Long notificationId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;
}
