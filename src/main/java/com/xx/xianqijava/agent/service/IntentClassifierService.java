package com.xx.xianqijava.agent.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI驱动的意图分类服务
 *
 * 使用大语言模型进行意图识别，替代基于关键词的规则引擎
 *
 * @author Claude
 * @since 2026-03-23
 */
@Slf4j
@Service
public class IntentClassifierService {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    /**
     * 意图分类缓存（提升性能）
     */
    private final ConcurrentHashMap<String, String> intentCache = new ConcurrentHashMap<>();

    /**
     * 意图分类提示词模板
     */
    private static final String INTENT_CLASSIFICATION_PROMPT = """
            你是一个专业的意图分类助手。请分析用户消息，判断其意图类别。

            **支持的意图类型：**

            1. **CUSTOMER_SERVICE** - 客服咨询
               - 注册、登录、实名认证问题
               - 交易规则、售后政策
               - 商品发布流程
               - 支付、配送问题
               - 示例："如何实名认证？"、"交易规则是什么"

            2. **RECOMMENDATION** - 商品推荐
               - 个性化商品推荐
               - 商品搜索
               - 热门商品查询
               - 附近商品查找
               - 示例："推荐一些手机"、"有什么二手书"

            3. **DESCRIPTION** - 描述优化
               - 生成商品描述
               - 优化商品文案
               - 示例："帮我写个商品描述"、"优化这个描述"

            4. **PRICING** - 定价建议
               - 商品估价
               - 定价建议
               - 示例："这个卖多少钱合适"、"帮我估价"

            5. **SAFETY** - 安全咨询
               - 交易安全检查
               - 防骗建议
               - 示例："这个交易安全吗"、"怎么防骗"

            6. **GREETING** - 问候语
               - 打招呼、感谢等
               - 示例："你好"、"谢谢"、"在吗"

            7. **GENERAL** - 一般咨询
               - 其他未分类咨询

            **分类规则：**
            - 理解用户的核心诉求，而不是简单匹配关键词
            - 考虑上下文和语义
            - 当用户意图不明确时，选择最可能的类别

            **输出格式：**
            只返回意图类型代码（如：CUSTOMER_SERVICE），不要添加任何其他内容。

            **用户消息：**
            %s
            """;

    /**
     * 使用AI进行意图分类
     *
     * @param userMessage 用户消息
     * @return 意图类型
     */
    public String classifyIntent(String userMessage) {
        // 检查缓存
        String cached = intentCache.get(userMessage);
        if (cached != null) {
            log.debug("意图分类命中缓存：{} -> {}", userMessage, cached);
            return cached;
        }

        try {
            // 构建提示词
            String prompt = String.format(INTENT_CLASSIFICATION_PROMPT, userMessage);

            // 调用AI模型
            String aiResponse = chatLanguageModel.generate(prompt);

            // 解析AI响应
            String intent = parseIntent(aiResponse);

            // 缓存结果（最多缓存1000条）
            if (intentCache.size() < 1000) {
                intentCache.put(userMessage, intent);
            }

            log.info("AI意图分类：{} -> {}", userMessage, intent);
            return intent;

        } catch (Exception e) {
            log.error("AI意图分类失败，降级到规则引擎", e);
            // 降级：使用规则引擎
            return classifyIntentByRules(userMessage);
        }
    }

    /**
     * 解析AI响应，提取意图类型
     */
    private String parseIntent(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "GENERAL";
        }

        String response = aiResponse.trim().toUpperCase();

        // 移除可能的markdown格式
        response = response.replaceAll("`", "").trim();

        // 验证是否为有效的意图类型
        if (response.matches("CUSTOMER_SERVICE|RECOMMENDATION|DESCRIPTION|PRICING|SAFETY|GREETING|GENERAL")) {
            return response;
        }

        // 如果AI返回了无效的意图，尝试从文本中提取
        if (response.contains("CUSTOMER_SERVICE") || response.contains("客服")) {
            return "CUSTOMER_SERVICE";
        } else if (response.contains("RECOMMENDATION") || response.contains("推荐")) {
            return "RECOMMENDATION";
        } else if (response.contains("DESCRIPTION") || response.contains("描述")) {
            return "DESCRIPTION";
        } else if (response.contains("PRICING") || response.contains("定价") || response.contains("价格")) {
            return "PRICING";
        } else if (response.contains("SAFETY") || response.contains("安全")) {
            return "SAFETY";
        } else if (response.contains("GREETING") || response.contains("问候")) {
            return "GREETING";
        }

        // 默认返回一般咨询
        return "GENERAL";
    }

    /**
     * 规则引擎降级方案（基于关键词）
     * 当AI服务不可用时使用
     */
    private String classifyIntentByRules(String message) {
        String lowerMessage = message.toLowerCase();

        // 问候语
        if (lowerMessage.matches(".*(你好|嗨|hello|hi|您好|在吗|帮忙|协助|谢谢|感谢|感谢).*")) {
            return "GREETING";
        }

        // 安全相关（优先级高）
        if (lowerMessage.contains("安全") || lowerMessage.contains("防骗") ||
            lowerMessage.contains("风险") || lowerMessage.contains("可信") ||
            lowerMessage.contains("诈骗") || lowerMessage.contains("欺诈")) {
            return "SAFETY";
        }

        // 推荐相关
        if (lowerMessage.contains("推荐") || lowerMessage.contains("搜索") ||
            lowerMessage.contains("有什么") || lowerMessage.contains("附近") ||
            lowerMessage.contains("热门") || lowerMessage.contains("想要")) {
            return "RECOMMENDATION";
        }

        // 描述优化相关
        if (lowerMessage.contains("描述") || lowerMessage.contains("文案") ||
            lowerMessage.contains("写个") && lowerMessage.contains("描述")) {
            return "DESCRIPTION";
        }

        // 定价相关
        if (lowerMessage.contains("价格") || lowerMessage.contains("多少钱") ||
            lowerMessage.contains("估价") || lowerMessage.contains("定价") ||
            lowerMessage.contains("卖多少钱") || lowerMessage.contains("值得")) {
            return "PRICING";
        }

        // 客服相关
        if (lowerMessage.contains("如何") || lowerMessage.contains("怎么") ||
            lowerMessage.contains("流程") || lowerMessage.contains("规则") ||
            lowerMessage.contains("认证") || lowerMessage.contains("注册") ||
            lowerMessage.contains("登录") || lowerMessage.contains("发布")) {
            return "CUSTOMER_SERVICE";
        }

        return "GENERAL";
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        intentCache.clear();
        log.info("意图分类缓存已清除");
    }

    /**
     * 获取缓存统计
     */
    public int getCacheSize() {
        return intentCache.size();
    }
}
