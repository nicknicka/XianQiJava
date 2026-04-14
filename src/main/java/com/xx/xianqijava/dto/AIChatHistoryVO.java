package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 聊天历史 VO
 *
 * @author Claude
 * @since 2026-03-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 聊天历史记录")
public class AIChatHistoryVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private String userId;

    @Schema(description = "用户消息", example = "推荐一些手机")
    private String userMessage;

    @Schema(description = "AI回复", example = "根据您的偏好，为您推荐以下手机...")
    private String aiResponse;

    @Schema(description = "意图类型", example = "RECOMMENDATION")
    private String intentType;

    @Schema(description = "Agent类型", example = "ProductRecommendationAgent")
    private String agentType;

    @Schema(description = "创建时间", example = "2026-03-23T10:30:00")
    private LocalDateTime createdAt;
}
