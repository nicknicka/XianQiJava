package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI聊天历史表
 *
 * @author Claude
 * @since 2026-03-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_chat_history")
@Schema(description = "AI聊天历史")
public class AIChatHistory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "用户消息")
    private String userMessage;

    @Schema(description = "AI回复")
    private String aiResponse;

    @Schema(description = "意图类型：CUSTOMER_SERVICE-客服咨询, RECOMMENDATION-商品推荐, DESCRIPTION-描述优化, PRICING-定价建议, SAFETY-安全咨询, GREETING-问候语, GENERAL-一般咨询")
    private String intentType;

    @Schema(description = "Agent类型：CustomerServiceAgent, ProductRecommendationAgent, ProductDescriptionAgent, PricingAdvisorAgent, TradeSafetyAgent")
    private String agentType;

    @Schema(description = "会话ID（用于多轮对话）")
    private String sessionId;

    @Schema(description = "使用的模型名称")
    private String modelName;

    @Schema(description = "消耗的token数量")
    private Integer tokensUsed;

    @Schema(description = "响应时间（毫秒）")
    private Integer responseTimeMs;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
