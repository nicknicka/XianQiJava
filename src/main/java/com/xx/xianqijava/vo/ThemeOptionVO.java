package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 主题选项VO
 */
@Data
@Schema(description = "主题选项")
public class ThemeOptionVO {

    @Schema(description = "主题值")
    private String value;

    @Schema(description = "主题标签")
    private String label;

    @Schema(description = "主题图标")
    private String icon;

    @Schema(description = "是否默认")
    private Boolean defaultValue;

    // 兼容旧代码的getter/setter方法
    public Boolean isDefault() {
        return defaultValue;
    }

    public void setDefault(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
}
