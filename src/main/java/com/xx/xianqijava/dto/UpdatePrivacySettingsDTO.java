package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新隐私设置 DTO
 */
@Data
@Schema(description = "更新隐私设置请求")
public class UpdatePrivacySettingsDTO {

    @Schema(description = "允许手机号搜索：null-不修改，0-关闭，1-开启")
    private Integer phoneSearchEnabled;

    @Schema(description = "显示位置信息：null-不修改，0-关闭，1-开启")
    private Integer locationEnabled;
}
