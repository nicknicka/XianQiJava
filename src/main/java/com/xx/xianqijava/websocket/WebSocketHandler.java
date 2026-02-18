package com.xx.xianqijava.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
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
            sendMessageToSession(session, createMessage("connected", Map.of(
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
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String event = (String) messageData.get("event");
            Object data = messageData.get("data");

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
            Map<String, Object> message = Map.of(
                    "event", event,
                    "data", data != null ? data : Map.of(),
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
}
