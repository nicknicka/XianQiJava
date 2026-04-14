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
 * 登录设备表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("login_device")
@Schema(description = "登录设备")
public class LoginDevice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "设备ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deviceId;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型：ios/android/web/miniapp")
    private String deviceType;

    @Schema(description = "平台标识")
    private String platform;

    @Schema(description = "设备唯一标识")
    private String deviceIdentifier;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "是否为当前设备：0-否，1-是")
    private Integer isCurrent;

    @Schema(description = "状态：0-正常，1-已移除")
    private Integer status;
}
