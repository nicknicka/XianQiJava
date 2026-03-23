package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.xianqijava.dto.AIChatHistoryVO;
import com.xx.xianqijava.entity.AIChatHistory;
import com.xx.xianqijava.mapper.AIChatHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI聊天历史服务
 *
 * @author Claude
 * @since 2026-03-23
 */
@Slf4j
@Service
public class AIChatHistoryService {

    @Resource
    private AIChatHistoryMapper aiChatHistoryMapper;

    /**
     * 保存聊天历史
     *
     * @param userId      用户ID
     * @param userMessage 用户消息
     * @param aiResponse  AI回复
     * @param intentType  意图类型
     */
    public void saveHistory(Long userId, String userMessage, String aiResponse, String intentType) {
        try {
            AIChatHistory history = new AIChatHistory();
            history.setUserId(userId);
            history.setUserMessage(userMessage);
            history.setAiResponse(aiResponse);
            history.setIntentType(intentType);
            history.setAgentType(determineAgentType(intentType));
            history.setModelName("glm-4-flash");
            history.setCreatedAt(LocalDateTime.now());

            aiChatHistoryMapper.insert(history);
            log.debug("保存AI聊天历史成功：userId={}, intent={}", userId, intentType);

        } catch (Exception e) {
            log.error("保存AI聊天历史失败：userId={}", userId, e);
        }
    }

    /**
     * 获取用户聊天历史
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 聊天历史列表
     */
    public List<AIChatHistoryVO> getHistory(Long userId, Integer limit) {
        try {
            QueryWrapper<AIChatHistory> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.orderByDesc("created_at");
            wrapper.last("LIMIT " + (limit != null && limit > 0 ? limit : 20));

            List<AIChatHistory> histories = aiChatHistoryMapper.selectList(wrapper);
            return histories.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取AI聊天历史失败：userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 清除用户聊天历史
     *
     * @param userId 用户ID
     */
    public void clearHistory(Long userId) {
        try {
            QueryWrapper<AIChatHistory> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            aiChatHistoryMapper.delete(wrapper);
            log.info("清除AI聊天历史成功：userId={}", userId);

        } catch (Exception e) {
            log.error("清除AI聊天历史失败：userId={}", userId, e);
        }
    }

    /**
     * 根据意图类型确定Agent类型
     */
    private String determineAgentType(String intentType) {
        return switch (intentType) {
            case "CUSTOMER_SERVICE" -> "CustomerServiceAgent";
            case "RECOMMENDATION" -> "ProductRecommendationAgent";
            case "DESCRIPTION" -> "ProductDescriptionAgent";
            case "PRICING" -> "PricingAdvisorAgent";
            case "SAFETY" -> "TradeSafetyAgent";
            default -> "UnknownAgent";
        };
    }

    /**
     * 转换为VO
     */
    private AIChatHistoryVO convertToVO(AIChatHistory history) {
        return AIChatHistoryVO.builder()
                .id(history.getId())
                .userId(history.getUserId())
                .userMessage(history.getUserMessage())
                .aiResponse(history.getAiResponse())
                .intentType(history.getIntentType())
                .agentType(history.getAgentType())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
