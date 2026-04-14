package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "通知ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long notificationId;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;
}
