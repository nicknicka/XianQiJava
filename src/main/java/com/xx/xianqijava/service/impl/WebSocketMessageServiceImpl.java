package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.service.WebSocketMessageService;
import com.xx.xianqijava.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * WebSocket 消息推送服务实现类
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageServiceImpl implements WebSocketMessageService {

    private final WebSocketHandler webSocketHandler;

    @Override
    public boolean sendMessageToUser(Long userId, String event, Object data) {
        if (userId == null || event == null) {
            log.warn("发送消息失败：userId 或 event 为空");
            return false;
        }

        boolean success = webSocketHandler.sendMessageToUser(userId, event, data);
        if (!success) {
            log.debug("发送消息失败：用户不在线或连接已关闭 - userId={}, event={}", userId, event);
        }
        return success;
    }

    @Override
    public boolean sendMessageToUser(Long userId, String event, Map<String, Object> data) {
        return sendMessageToUser(userId, event, (Object) data);
    }

    @Override
    public int broadcastMessage(Iterable<Long> userIds, String event, Object data) {
        if (userIds == null || event == null) {
            log.warn("广播消息失败：userIds 或 event 为空");
            return 0;
        }

        int successCount = 0;
        for (Long userId : userIds) {
            if (sendMessageToUser(userId, event, data)) {
                successCount++;
            }
        }

        log.debug("广播消息完成：目标用户数={}, 成功发送数={}", userIds instanceof Iterable ? ((Iterable<?>) userIds).spliterator().getExactSizeIfKnown() : "未知", successCount);
        return successCount;
    }

    @Override
    public int broadcastToAll(String event, Object data) {
        Set<Long> onlineUserIds = webSocketHandler.getOnlineUserIds();
        return broadcastMessage(onlineUserIds, event, data);
    }

    @Override
    public boolean sendNewMessage(Long toUserId, Long fromUserId, Long messageId, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("fromUserId", fromUserId);
        data.put("messageId", messageId);
        data.put("content", content);
        data.put("timestamp", System.currentTimeMillis());

        return sendMessageToUser(toUserId, "new_message", data);
    }

    @Override
    public boolean sendMessageReadReceipt(Long toUserId, Long fromUserId, Long messageId) {
        Map<String, Object> data = new HashMap<>();
        data.put("fromUserId", fromUserId);
        data.put("messageId", messageId);
        data.put("timestamp", System.currentTimeMillis());

        return sendMessageToUser(toUserId, "message_read", data);
    }

    @Override
    public boolean sendTypingIndicator(Long toUserId, Long fromUserId, Long conversationId) {
        Map<String, Object> data = new HashMap<>();
        data.put("fromUserId", fromUserId);
        data.put("conversationId", conversationId);
        data.put("timestamp", System.currentTimeMillis());

        return sendMessageToUser(toUserId, "typing", data);
    }

    @Override
    public boolean sendSystemNotification(Long userId, String title, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("timestamp", System.currentTimeMillis());

        return sendMessageToUser(userId, "system_notification", data);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return webSocketHandler.isUserOnline(userId);
    }

    @Override
    public int getOnlineUserCount() {
        return webSocketHandler.getOnlineUserCount();
    }
}
