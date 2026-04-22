package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新通知设置 DTO
 */
@Data
@Schema(description = "更新通知设置请求")
public class UpdateNotificationSettingsDTO {

    @Schema(description = "是否启用消息通知")
    private Boolean notificationEnabled;

    @Schema(description = "是否启用提示音")
    private Boolean soundEnabled;

    @Schema(description = "是否启用振动")
    private Boolean vibrationEnabled;
}
