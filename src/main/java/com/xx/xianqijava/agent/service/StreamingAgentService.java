package com.xx.xianqijava.agent.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AI 流式响应服务
 * 使用 LangChain4j 的 StreamingChatLanguageModel 实现真正的流式输出
 *
 * @author Claude
 * @since 2026-03-24
 */
@Slf4j
@Service
public class StreamingAgentService {

    @Resource
    private StreamingChatLanguageModel streamingChatLanguageModel;

    @Resource
    private IntentClassifierService intentClassifierService;

    /**
     * 流式聊天接口
     * 返回 Flux<String> 用于响应式流式传输
     *
     * @param userMessage 用户消息
     * @param systemPrompt 系统提示词
     * @return 流式响应
     */
    public Flux<String> chatStream(String userMessage, String systemPrompt) {
        log.info("开始流式聊天：{}", userMessage);

        return Flux.create(emitter -> {
            try {
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(dev.langchain4j.data.message.SystemMessage.from(systemPrompt));
                messages.add(UserMessage.from(userMessage));

                // 使用流式模型生成响应 - LangChain4j 0.36.2 正确的 API
                streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        // 接收到每个 token 时调用
                        emitter.next(token);
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        // 响应完成时调用
                        log.info("流式响应完成");
                        emitter.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        // 发生错误时调用
                        log.error("流式响应出错", error);
                        emitter.error(error);
                    }
                });
            } catch (Exception e) {
                log.error("流式聊天失败", e);
                emitter.error(e);
            }
        });
    }

    /**
     * 带回调的流式聊天（用于 WebSocket）
     *
     * @param userMessage 用户消息
     * @param systemPrompt 系统提示词
     * @param onToken 接收 token 的回调
     * @param onComplete 完成回调
     * @param onError 错误回调
     */
    public void chatStreamWithCallback(
            String userMessage,
            String systemPrompt,
            Consumer<String> onToken,
            Runnable onComplete,
            Consumer<Throwable> onError
    ) {
        log.info("开始流式聊天（回调模式）：{}", userMessage);

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(dev.langchain4j.data.message.SystemMessage.from(systemPrompt));
            messages.add(UserMessage.from(userMessage));

            // 使用流式模型生成响应 - LangChain4j 0.36.2 正确的 API
            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    try {
                        onToken.accept(token);
                    } catch (Exception e) {
                        log.error("回调处理失败", e);
                    }
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    log.info("流式响应完成");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式响应出错", error);
                    if (onError != null) {
                        onError.accept(error);
                    }
                }
            });
        } catch (Exception e) {
            log.error("流式聊天失败", e);
            if (onError != null) {
                onError.accept(e);
            }
        }
    }

    /**
     * 客服助手的系统提示词
     */
    public static final String CUSTOMER_SERVICE_PROMPT = """
            你是"闲七"校园二手交易平台的智能客服助手。

            # 专业身份
            你熟悉平台的所有功能和服务流程，能够：
            1. 解答注册、登录、实名认证问题
            2. 指导商品发布流程
            3. 解释交易规则和售后政策
            4. 处理支付、配送相关问题
            5. 提供平台使用帮助

            # 回答风格
            - 友好耐心：像客服人员与用户对话
            - 清晰准确：用简单语言解释复杂流程
            - 引导操作：给出具体的操作步骤
            - 安全提示：提醒用户注意交易安全

            # 注意事项
            - 如果工具返回的信息不足，如实告知用户
            - 不要编造平台规则或政策
            - 对于超出客服范围的问题（如技术故障），建议联系技术支持
            - 保持客观中立，不偏袒交易任何一方
            """;

    /**
     * 商品推荐的系统提示词
     */
    public static final String RECOMMENDATION_PROMPT = """
            你是"闲七"校园二手交易平台的智能商品推荐助手。

            # 专业身份
            你能够根据用户需求推荐合适的二手商品，并提供专业的购买建议。

            # 工作方式
            - 理解用户的需求和偏好
            - 推荐符合条件的商品
            - 提供商品的关键信息（价格、成色、卖家信誉）
            - 给出购买建议和注意事项

            # 回答风格
            - 专业推荐：基于用户需求精准推荐
            - 信息全面：提供商品关键信息
            - 购买建议：给出专业的购买意见
            """;

    /**
     * 描述优化的系统提示词
     */
    public static final String DESCRIPTION_PROMPT = """
            你是"闲七"校园二手交易平台的商品描述优化助手。

            # 专业身份
            你擅长撰写吸引人的商品描述，帮助卖家更好地展示商品。

            # 工作方式
            - 根据用户提供的信息撰写描述
            - 突出商品的优点和特色
            - 使用吸引人的语言
            - 保持真实性和准确性

            # 回答风格
            - 吸引眼球：开头就能吸引买家
            - 信息丰富：包含关键参数和使用体验
            - 真实准确：不夸大其词
            """;

    /**
     * 定价建议的系统提示词
     */
    public static final String PRICING_PROMPT = """
            你是"闲七"校园二手交易平台的定价顾问助手。

            # 专业身份
            你熟悉二手市场的价格行情，能够为商品提供合理的定价建议。

            # 工作方式
            - 分析商品的成色、品牌、购买时间
            - 参考市场行情给出价格区间
            - 解释定价依据
            - 提供定价策略建议

            # 回答风格
            - 数据支撑：基于市场行情定价
            - 解释清晰：说明定价依据
            - 策略建议：给出最优定价方案
            """;

    /**
     * 交易安全顾问的系统提示词
     */
    public static final String SAFETY_PROMPT = """
            你是"闲七"校园二手交易平台的交易安全顾问。

            # 专业身份
            你专注于交易安全，能够识别风险并提供防骗建议。

            # 工作方式
            - 评估交易风险
            - 识别可能的诈骗手段
            - 提供安全交易建议
            - 推荐安全的交易方式

            # 回答风格
            - 风险意识：强调安全第一
            - 防骗指南：提供实用的防骗技巧
            - 安全建议：推荐安全的交易流程
            """;

    /**
     * 根据意图类型获取对应的系统提示词
     */
    public String getPromptByIntent(String intent) {
        return switch (intent) {
            case "CUSTOMER_SERVICE" -> CUSTOMER_SERVICE_PROMPT;
            case "RECOMMENDATION" -> RECOMMENDATION_PROMPT;
            case "DESCRIPTION" -> DESCRIPTION_PROMPT;
            case "PRICING" -> PRICING_PROMPT;
            case "SAFETY" -> SAFETY_PROMPT;
            default -> CUSTOMER_SERVICE_PROMPT;
        };
    }
}
