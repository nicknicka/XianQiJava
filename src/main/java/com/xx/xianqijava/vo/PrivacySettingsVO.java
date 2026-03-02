package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 隐私设置 VO
 */
@Data
@Schema(description = "隐私设置响应")
public class PrivacySettingsVO {

    @Schema(description = "允许手机号搜索：0-否，1-是")
    private Integer phoneSearchEnabled;

    @Schema(description = "显示位置信息：0-否，1-是")
    private Integer locationEnabled;

    public PrivacySettingsVO() {
    }

    public PrivacySettingsVO(Integer phoneSearchEnabled, Integer locationEnabled) {
        this.phoneSearchEnabled = phoneSearchEnabled != null ? phoneSearchEnabled : 1;
        this.locationEnabled = locationEnabled != null ? locationEnabled : 1;
    }
}
