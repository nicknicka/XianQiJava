package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 主题配置VO
 */
@Data
@Schema(description = "主题配置")
public class ThemeConfigVO {

    @Schema(description = "当前主题")
    private String theme;

    @Schema(description = "自动深色模式")
    private Boolean autoDarkMode;

    @Schema(description = "字体大小")
    private Integer fontSize;

    @Schema(description = "可用主题列表")
    private List<ThemeOptionVO> availableThemes;

    @Schema(description = "可用字体大小")
    private Map<String, Integer> availableFontSizes;

    @Schema(description = "主题颜色")
    private ThemeColorsVO themeColors;
}
