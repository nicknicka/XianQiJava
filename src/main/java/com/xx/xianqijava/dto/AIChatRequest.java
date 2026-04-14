package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 聊天请求 DTO
 *
 * @author Claude
 * @since 2026-03-23
 */
@Data
@Schema(description = "AI 聊天请求")
public class AIChatRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true, example = "1")
    private String userId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容", required = true, example = "推荐一些手机")
    private String message;

    @Schema(description = "会话ID（可选，用于多轮对话）", example = "session_123")
    private String sessionId;

    @Schema(description = "意图类型（可选，手动指定意图）", example = "RECOMMENDATION")
    private String intent;
}
