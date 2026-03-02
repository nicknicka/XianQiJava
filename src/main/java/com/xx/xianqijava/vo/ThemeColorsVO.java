package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 主题颜色VO
 */
@Data
@Schema(description = "主题颜色")
public class ThemeColorsVO {

    @Schema(description = "主色")
    private String primary;

    @Schema(description = "成功色")
    private String success;

    @Schema(description = "警告色")
    private String warning;

    @Schema(description = "错误色")
    private String error;

    @Schema(description = "背景色")
    private String background;

    @Schema(description = "前景色")
    private String foreground;

    @Schema(description = "边框色")
    private String border;
}
