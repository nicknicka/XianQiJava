package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知设置响应 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知设置")
public class NotificationSettingsVO {

    @Schema(description = "是否启用消息通知")
    private Boolean notificationEnabled;

    @Schema(description = "是否启用提示音")
    private Boolean soundEnabled;

    @Schema(description = "是否启用振动")
    private Boolean vibrationEnabled;
}
