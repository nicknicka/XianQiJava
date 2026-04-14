package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 黑名单表
 */
@Data
@TableName("blacklist")
@Schema(description = "黑名单")
public class Blacklist {

    @TableId(type = IdType.AUTO)
    @Schema(description = "黑名单ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long blacklistId;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "被拉黑的用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long blockedUserId;

    @Schema(description = "拉黑原因")
    private String reason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
