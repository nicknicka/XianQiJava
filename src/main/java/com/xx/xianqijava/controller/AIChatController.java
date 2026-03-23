package com.xx.xianqijava.controller;

import com.xx.xianqijava.agent.*;
import com.xx.xianqijava.agent.service.IntentClassifierService;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.AIChatHistoryVO;
import com.xx.xianqijava.dto.AIChatRequest;
import com.xx.xianqijava.service.AIChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 聊天控制器
 *
 * @author Claude
 * @since 2026-03-23
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@Tag(name = "AI聊天接口", description = "AI智能助手相关接口")
public class AIChatController {

    @Resource
    private CustomerServiceAgent customerServiceAgent;

    @Resource
    private ProductRecommendationAgent recommendationAgent;

    @Resource
    private ProductDescriptionAgent descriptionAgent;

    @Resource
    private PricingAdvisorAgent pricingAdvisorAgent;

    @Resource
    private TradeSafetyAgent safetyAgent;

    @Resource
    private IntentClassifierService intentClassifierService;

    @Resource
    private AIChatHistoryService chatHistoryService;

    /**
     * AI 统一聊天接口
     * 根据意图自动路由到对应的 Agent
     */
    @PostMapping("/chat")
    @Operation(summary = "AI聊天", description = "与AI助手对话，自动识别意图并路由到对应Agent")
    public Result<String> chat(@RequestBody AIChatRequest request) {
        try {
            // 1. 构建带用户ID的消息
            String userMessage = String.format("[当前用户ID: %d] %s",
                    request.getUserId(), request.getMessage());

            // 2. 意图分类
            String intent = intentClassifierService.classifyIntent(userMessage);
            log.info("用户ID: {}, 意图: {}, 消息: {}",
                    request.getUserId(), intent, request.getMessage());

            // 3. 路由到对应 Agent
            String response;
            switch (intent) {
                case "CUSTOMER_SERVICE":
                    response = customerServiceAgent.chat(userMessage);
                    break;
                case "RECOMMENDATION":
                    response = recommendationAgent.recommend(userMessage);
                    break;
                case "DESCRIPTION":
                    response = descriptionAgent.optimizeDescription(userMessage);
                    break;
                case "PRICING":
                    response = pricingAdvisorAgent.advisePrice(userMessage);
                    break;
                case "SAFETY":
                    response = safetyAgent.checkSafety(userMessage);
                    break;
                case "GREETING":
                    response = """
                            你好！我是闲齐的智能助手 🎓

                            我可以帮助你：

                            📦 **商品推荐**
                            根据你的需求推荐合适的二手商品

                            ✍️ **描述优化**
                            帮你写吸引人的商品描述

                            💰 **定价建议**
                            为你的商品提供合理的定价建议

                            🔒 **安全咨询**
                            交易安全建议和风险评估

                            ❓ **客服问题**
                            解答平台使用相关问题

                            请告诉我你需要什么帮助！
                            """;
                    break;
                default:
                    response = """
                            抱歉，我没有理解你的问题。

                            你可以尝试：
                            - "推荐一些手机"（商品推荐）
                            - "帮我写个商品描述"（描述优化）
                            - "这个卖多少钱合适"（定价建议）
                            - "交易安全吗"（安全咨询）
                            - "如何实名认证"（客服问题）

                            请用更具体的方式描述你的需求。
                            """;
            }

            // 4. 保存聊天历史
            chatHistoryService.saveHistory(
                    request.getUserId(),
                    request.getMessage(),
                    response,
                    intent
            );

            return Result.success("AI回复成功", response);

        } catch (Exception e) {
            log.error("AI聊天处理失败", e);
            return Result.error("AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 获取聊天历史
     */
    @GetMapping("/history/{userId}")
    @Operation(summary = "获取聊天历史", description = "获取用户的AI聊天历史记录")
    public Result<List<AIChatHistoryVO>> getHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        try {
            List<AIChatHistoryVO> history = chatHistoryService.getHistory(userId, limit);
            return Result.success(history);

        } catch (Exception e) {
            log.error("获取聊天历史失败：userId={}", userId, e);
            return Result.error("获取聊天历史失败");
        }
    }

    /**
     * 清除聊天历史
     */
    @DeleteMapping("/history/{userId}")
    @Operation(summary = "清除聊天历史", description = "清除用户的AI聊天历史记录")
    public Result<Void> clearHistory(@PathVariable Long userId) {
        try {
            chatHistoryService.clearHistory(userId);
            return Result.success();

        } catch (Exception e) {
            log.error("清除聊天历史失败：userId={}", userId, e);
            return Result.error("清除聊天历史失败");
        }
    }

    /**
     * 获取AI配置信息
     */
    @GetMapping("/config")
    @Operation(summary = "获取AI配置", description = "获取AI功能的配置信息")
    public Result<com.xx.xianqijava.dto.AIConfigDTO> getConfig() {
        try {
            com.xx.xianqijava.dto.AIConfigDTO config = com.xx.xianqijava.dto.AIConfigDTO.builder()
                    .enabled(true)
                    .model("glm-4-flash")
                    .maxTokens(2000)
                    .temperature(0.7)
                    .chatMemorySize(20)
                    .streamEnabled(true)
                    .build();

            return Result.success(config);

        } catch (Exception e) {
            log.error("获取AI配置失败", e);
            return Result.error("获取AI配置失败");
        }
    }

    /**
     * AI 流式聊天接口（SSE）
     * 返回 Server-Sent Events 流式响应
     * 使用模拟流式输出（将完整响应分块发送）
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI流式聊天", description = "与AI助手对话，使用SSE流式返回响应")
    public SseEmitter chatStream(@RequestBody AIChatRequest request) {
        // 创建 SSE 发射器，超时时间 5 分钟
        SseEmitter emitter = new SseEmitter(300000L);

        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 意图分类
                String userMessage = String.format("[当前用户ID: %d] %s",
                        request.getUserId(), request.getMessage());

                String intent = intentClassifierService.classifyIntent(userMessage);

                // 2. 调用对应的 Agent 获取完整响应
                String response;
                switch (intent) {
                    case "CUSTOMER_SERVICE":
                        response = customerServiceAgent.chat(userMessage);
                        break;
                    case "RECOMMENDATION":
                        response = recommendationAgent.recommend(userMessage);
                        break;
                    case "DESCRIPTION":
                        response = descriptionAgent.optimizeDescription(userMessage);
                        break;
                    case "PRICING":
                        response = pricingAdvisorAgent.advisePrice(userMessage);
                        break;
                    case "SAFETY":
                        response = safetyAgent.checkSafety(userMessage);
                        break;
                    default:
                        response = "抱歉，我没有理解您的问题。";
                }

                // 3. 模拟流式输出：将响应分块发送
                // 按字符或词语分块，模拟打字效果
                int chunkSize = 10; // 每次发送10个字符
                for (int i = 0; i < response.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, response.length());
                    String chunk = response.substring(i, end);

                    // 直接发送数据，不设置 event name，使用标准的 SSE data 格式
                    emitter.send(SseEmitter.event().data(chunk));

                    // 模拟打字延迟（每块50ms）
                    Thread.sleep(50);
                }

                // 4. 保存聊天历史
                chatHistoryService.saveHistory(
                        request.getUserId(),
                        request.getMessage(),
                        response,
                        intent
                );

                // 5. 发送完成事件
                emitter.send(SseEmitter.event().data("[DONE]"));

                emitter.complete();

            } catch (Exception e) {
                log.error("流式聊天处理失败", e);
                try {
                    // ❌ 不要把技术错误直接暴露给用户
                    // 只发送友好的提示信息
                    emitter.send(SseEmitter.event()
                            .data("抱歉，AI助手暂时无法回复，请稍后再试。"));
                } catch (IOException ex) {
                    log.error("发送错误消息失败", ex);
                }
                emitter.completeWithError(e);
            }
        });

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onError((ex) -> {
            log.error("SSE连接错误", ex);
            emitter.completeWithError(ex);
        });

        return emitter;
    }
}
