package com.xx.xianqijava.agent.config;

import com.xx.xianqijava.agent.*;
import com.xx.xianqijava.agent.tools.SafetyTools;
import com.xx.xianqijava.agent.tools.SystemTools;
import com.xx.xianqijava.agent.tools.UserTools;
import com.xx.xianqijava.config.ZhipuAIConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.time.Duration;

/**
 * LangChain4j 配置类
 * 配置 ChatLanguageModel、ChatMemory 和所有 Agent
 *
 * @author Claude
 * @since 2026-03-23
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ZhipuAIConfig.class)
@ConditionalOnProperty(name = "ai.enabled", havingValue = "true", matchIfMissing = true)
public class LangChain4jConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jConfig.class);

    private final ZhipuAIConfig zhipuAIConfig;
    private final UserTools userTools;
    private final SystemTools systemTools;
    private final SafetyTools safetyTools;

    private ChatLanguageModel chatLanguageModel;

    public LangChain4jConfig(
            ZhipuAIConfig zhipuAIConfig,
            UserTools userTools,
            SystemTools systemTools,
            SafetyTools safetyTools
    ) {
        this.zhipuAIConfig = zhipuAIConfig;
        this.userTools = userTools;
        this.systemTools = systemTools;
        this.safetyTools = safetyTools;
    }

    /**
     * 配置 ChatLanguageModel（智谱 AI）
     */
    @Bean(destroyMethod = "")
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化 ChatLanguageModel，模型：{}", zhipuAIConfig.getModel());

        if (!zhipuAIConfig.isValid()) {
            throw new IllegalStateException("智谱 AI 配置无效，请检查 API Key 是否正确配置");
        }

        this.chatLanguageModel = ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())
                .temperature(0.7)
                .maxRetries(2)
                .callTimeout(Duration.ofSeconds(zhipuAIConfig.getTimeout() / 1000))
                .connectTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .build();

        log.info("ChatLanguageModel 初始化成功");
        return this.chatLanguageModel;
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("LangChain4j 资源清理开始...");

        try {
            if (chatLanguageModel instanceof ZhipuAiChatModel) {
                ZhipuAiChatModel zhipuModel = (ZhipuAiChatModel) chatLanguageModel;
                try {
                    java.lang.reflect.Field clientField = zhipuModel.getClass().getDeclaredField("client");
                    clientField.setAccessible(true);
                    Object client = clientField.get(zhipuModel);

                    if (client != null && client.getClass().getName().contains("okhttp3.OkHttpClient")) {
                        try {
                            java.lang.reflect.Method shutdownMethod = client.getClass().getMethod("shutdown");
                            shutdownMethod.invoke(client);
                            log.info("OkHttpClient 已成功关闭");
                        } catch (NoSuchMethodException e) {
                            try {
                                java.lang.reflect.Method dispatcherMethod = client.getClass().getMethod("dispatcher");
                                Object dispatcher = dispatcherMethod.invoke(client);
                                java.lang.reflect.Method executorServiceMethod = dispatcher.getClass().getMethod("executorService");
                                java.util.concurrent.ExecutorService executorService =
                                        (java.util.concurrent.ExecutorService) executorServiceMethod.invoke(dispatcher);
                                executorService.shutdown();
                                log.info("OkHttp ExecutorService 已成功关闭");
                            } catch (Exception ex) {
                                log.warn("无法关闭 OkHttp ExecutorService: {}", ex.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("反射关闭 OkHttpClient 失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("LangChain4j 资源清理失败", e);
        }

        log.info("LangChain4j 资源清理完成");
    }

    /**
     * 配置 ChatMemory（对话记忆）
     */
    @Bean
    public ChatMemory chatMemory() {
        log.info("初始化ChatMemory，消息窗口大小：20");
        return MessageWindowChatMemory.withMaxMessages(20);
    }

    /**
     * 构建客服助手 AI Agent
     */
    @Bean
    public CustomerServiceAgent customerServiceAgent(
            ChatLanguageModel chatLanguageModel,
            ChatMemory chatMemory
    ) {
        log.info("构建CustomerServiceAgent...");
        return AiServices.builder(CustomerServiceAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(systemTools)
                .build();
    }

    /**
     * 构建商品推荐 AI Agent
     */
    @Bean
    public ProductRecommendationAgent recommendationAgent(
            ChatLanguageModel chatLanguageModel,
            ChatMemory chatMemory
    ) {
        log.info("构建ProductRecommendationAgent...");
        return AiServices.builder(ProductRecommendationAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(userTools, systemTools)
                .build();
    }

    /**
     * 构建商品描述优化 AI Agent
     */
    @Bean
    public ProductDescriptionAgent descriptionAgent(
            ChatLanguageModel chatLanguageModel
    ) {
        log.info("构建ProductDescriptionAgent...");
        return AiServices.builder(ProductDescriptionAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    /**
     * 构建定价建议 AI Agent
     */
    @Bean
    public PricingAdvisorAgent pricingAdvisorAgent(
            ChatLanguageModel chatLanguageModel
    ) {
        log.info("构建PricingAdvisorAgent...");
        return AiServices.builder(PricingAdvisorAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    /**
     * 构建交易安全顾问 AI Agent
     */
    @Bean
    public TradeSafetyAgent safetyAgent(
            ChatLanguageModel chatLanguageModel,
            ChatMemory chatMemory
    ) {
        log.info("构建TradeSafetyAgent...");
        return AiServices.builder(TradeSafetyAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(safetyTools, userTools)
                .build();
    }
}
