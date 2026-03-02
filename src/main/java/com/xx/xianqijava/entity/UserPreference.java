package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户偏好设置实体
 */
@Data
@TableName("user_preference")
@Schema(description = "用户偏好设置")
public class UserPreference {

    @Schema(description = "偏好设置ID")
    @TableId(type = IdType.AUTO)
    private Long preferenceId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "主题：light-浅色，dark-深色，auto-跟随系统")
    private String theme;

    @Schema(description = "自动深色模式：0-关闭，1-开启")
    private Integer autoDarkMode;

    @Schema(description = "字体大小（px）")
    private Integer fontSize;

    @Schema(description = "是否启用通知：0-关闭，1-开启")
    private Integer notificationEnabled;

    @Schema(description = "是否启用提示音：0-关闭，1-开启")
    private Integer soundEnabled;

    @Schema(description = "是否启用振动：0-关闭，1-开启")
    private Integer vibrationEnabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
