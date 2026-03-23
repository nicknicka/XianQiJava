package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 配置 DTO
 *
 * @author Claude
 * @since 2026-03-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 功能配置")
public class AIConfigDTO {

    @Schema(description = "是否启用AI功能", example = "true")
    private Boolean enabled;

    @Schema(description = "使用的模型", example = "glm-4-flash")
    private String model;

    @Schema(description = "最大回复长度", example = "2000")
    private Integer maxTokens;

    @Schema(description = "温度参数", example = "0.7")
    private Double temperature;

    @Schema(description = "对话记忆大小", example = "20")
    private Integer chatMemorySize;

    @Schema(description = "是否启用流式输出", example = "false")
    private Boolean streamEnabled;
}
