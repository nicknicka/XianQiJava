package com.xx.xianqijava.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.xianqijava.agent.*;
import com.xx.xianqijava.agent.service.IntentClassifierService;
import com.xx.xianqijava.service.AIChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 处理器
 */
@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储用户ID和WebSocket会话的映射
     */
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // AI 相关依赖
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
     * 连接建立后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            log.info("WebSocket 连接建立成功: userId={}, sessionId={}", userId, session.getId());

            // 发送连接成功消息
            sendMessageToSession(session, createMessage("connected", Map.<String, Object>of(
                    "userId", userId,
                    "message", "连接成功"
            )));
        }
    }

    /**
     * 接收到客户端消息时调用
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserIdFromSession(session);
        String payload = message.getPayload();
        log.debug("收到 WebSocket 消息: userId={}, message={}", userId, payload);

        try {
            // 解析消息
            Map<String, Object> messageData = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});

            // 兼容两种消息格式：{event: "..."} 和 {type: "..."}
            String event = (String) messageData.get("event");
            if (event == null) {
                event = (String) messageData.get("type");
            }

            Object data = messageData.get("data");

            // 检查事件类型是否为空
            if (event == null) {
                log.warn("消息格式错误，缺少 event 或 type 字段: {}", payload);
                return;
            }

            // 根据事件类型处理消息
            switch (event) {
                case "ping":
                    // 心跳检测
                    sendMessageToSession(session, createMessage("pong", null));
                    break;
                case "typing":
                    // 输入状态提示
                    handleTypingEvent(userId, data);
                    break;
                case "ai_chat":
                    // AI 聊天
                    handleAIChatEvent(userId, data, session);
                    break;
                default:
                    log.warn("未知的事件类型: {}", event);
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("WebSocket 连接关闭: userId={}, status={}", userId, status);
        }
    }

    /**
     * 传输错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.error("WebSocket 传输错误: userId={}, error={}", userId, exception.getMessage(), exception);

        if (session.isOpen()) {
            session.close();
        }

        if (userId != null) {
            USER_SESSIONS.remove(userId);
        }
    }

    /**
     * 向指定用户发送消息
     */
    public boolean sendMessageToUser(Long userId, String event, Object data) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            return sendMessageToSession(session, createMessage(event, data));
        }
        return false;
    }

    /**
     * 向会话发送消息
     */
    private boolean sendMessageToSession(WebSocketSession session, String message) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(message));
            }
            return true;
        } catch (IOException e) {
            log.error("发送 WebSocket 消息失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建消息
     */
    private String createMessage(String event, Object data) {
        try {
            Map<String, Object> message = Map.<String, Object>of(
                    "event", event,
                    "data", data != null ? data : Map.<String, Object>of(),
                    "timestamp", System.currentTimeMillis()
            );
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("创建消息失败: {}", e.getMessage(), e);
            return "{}";
        }
    }

    /**
     * 处理输入状态事件
     */
    private void handleTypingEvent(Long fromUserId, Object data) {
        // 这里可以实现输入状态提示逻辑
        // 例如：通知聊天对方当前用户正在输入
    }

    /**
     * 处理 AI 聊天事件
     */
    private void handleAIChatEvent(Long userId, Object data, WebSocketSession session) {
        try {
            // 解析请求数据
            Map<String, Object> requestData;
            if (data instanceof Map) {
                requestData = (Map<String, Object>) data;
            } else {
                sendMessageToSession(session, createMessage("error", Map.of(
                        "code", "INVALID_REQUEST",
                        "message", "请求格式错误"
                )));
                return;
            }

            // 获取消息内容
            String message = (String) requestData.get("message");
            if (message == null || message.trim().isEmpty()) {
                sendMessageToSession(session, createMessage("error", Map.of(
                        "code", "EMPTY_MESSAGE",
                        "message", "消息不能为空"
                )));
                return;
            }

            log.info("收到 AI 聊天请求: userId={}, message={}", userId, message);

            // 异步处理 AI 请求，避免阻塞 WebSocket 线程
            CompletableFuture.runAsync(() -> {
                try {
                    // 1. 构建带用户ID的消息
                    String userMessage = String.format("[当前用户ID: %d] %s", userId, message);

                    // 2. 意图分类
                    String intent = intentClassifierService.classifyIntent(userMessage);
                    log.info("AI 意图分类: userId={}, intent={}", userId, intent);

                    // 3. 路由到对应 Agent 获取响应
                    String response = getAIResponse(intent, userMessage);

                    // 4. 流式发送响应（分块发送）
                    int chunkSize = 10; // 每次发送10个字符
                    int sequenceId = 0;

                    for (int i = 0; i < response.length(); i += chunkSize) {
                        int end = Math.min(i + chunkSize, response.length());
                        String chunk = response.substring(i, end);

                        sendMessageToSession(session, createMessage("ai_chunk", Map.of(
                                "content", chunk,
                                "sequenceId", ++sequenceId
                        )));

                        // 模拟打字延迟（每块50ms）
                        Thread.sleep(50);
                    }

                    // 5. 保存聊天历史
                    try {
                        chatHistoryService.saveHistory(userId, message, response, intent);
                    } catch (Exception e) {
                        log.error("保存聊天历史失败: userId={}", userId, e);
                    }

                    // 6. 发送完成事件
                    sendMessageToSession(session, createMessage("ai_complete", Map.of(
                            "messageId", System.currentTimeMillis(),
                            "intentType", intent
                    )));

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("AI 聊天处理被中断: userId={}", userId, e);
                    sendMessageToSession(session, createMessage("error", Map.of(
                            "code", "PROCESS_INTERRUPTED",
                            "message", "AI 处理被中断"
                    )));
                } catch (Exception e) {
                    log.error("AI 聊天处理失败: userId={}", userId, e);
                    sendMessageToSession(session, createMessage("error", Map.of(
                            "code", "AI_SERVICE_ERROR",
                            "message", "抱歉，AI助手暂时无法回复，请稍后再试。"
                    )));
                }
            });

        } catch (Exception e) {
            log.error("处理 AI 聊天事件失败: userId={}", userId, e);
            try {
                sendMessageToSession(session, createMessage("error", Map.of(
                        "code", "EVENT_HANDLER_ERROR",
                        "message", "事件处理失败"
                )));
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }

    /**
     * 根据意图获取 AI 响应
     */
    private String getAIResponse(String intent, String userMessage) {
        return switch (intent) {
            case "CUSTOMER_SERVICE" -> customerServiceAgent.chat(userMessage);
            case "RECOMMENDATION" -> recommendationAgent.recommend(userMessage);
            case "DESCRIPTION" -> descriptionAgent.optimizeDescription(userMessage);
            case "PRICING" -> pricingAdvisorAgent.advisePrice(userMessage);
            case "SAFETY" -> safetyAgent.checkSafety(userMessage);
            case "GREETING" -> """
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
            default -> """
                    抱歉，我没有理解你的问题。

                    你可以尝试：
                    - "推荐一些手机"（商品推荐）
                    - "帮我写个商品描述"（描述优化）
                    - "这个卖多少钱合适"（定价建议）
                    - "交易安全吗"（安全咨询）
                    - "如何实名认证"（客服问题）

                    请用更具体的方式描述你的需求。
                    """;
        };
    }

    /**
     * 从会话中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            Object userId = session.getAttributes().get("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            } else if (userId instanceof String) {
                return Long.parseLong((String) userId);
            }
        } catch (Exception e) {
            log.error("获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return (int) USER_SESSIONS.values().stream()
                .filter(WebSocketSession::isOpen)
                .count();
    }

    /**
     * 获取所有在线用户ID
     */
    public Set<Long> getOnlineUserIds() {
        return USER_SESSIONS.keySet();
    }
}
