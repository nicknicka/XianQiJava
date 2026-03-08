package com.xx.xianqijava.service;

import java.util.Map;

/**
 * WebSocket 消息推送服务接口
 *
 * @author Claude Code
 * @since 2026-03-08
 */
public interface WebSocketMessageService {

    /**
     * 向指定用户发送消息
     *
     * @param userId 用户ID
     * @param event  事件类型
     * @param data   消息数据
     * @return 是否发送成功
     */
    boolean sendMessageToUser(Long userId, String event, Object data);

    /**
     * 向指定用户发送消息（Map格式）
     *
     * @param userId 用户ID
     * @param event  事件类型
     * @param data   消息数据
     * @return 是否发送成功
     */
    boolean sendMessageToUser(Long userId, String event, Map<String, Object> data);

    /**
     * 向多个用户广播消息
     *
     * @param userIds 用户ID列表
     * @param event   事件类型
     * @param data    消息数据
     * @return 成功发送的数量
     */
    int broadcastMessage(Iterable<Long> userIds, String event, Object data);

    /**
     * 向所有在线用户广播消息
     *
     * @param event 事件类型
     * @param data  消息数据
     * @return 成功发送的数量
     */
    int broadcastToAll(String event, Object data);

    /**
     * 发送新消息通知
     *
     * @param toUserId   接收者用户ID
     * @param fromUserId 发送者用户ID
     * @param messageId  消息ID
     * @param content    消息内容
     * @return 是否发送成功
     */
    boolean sendNewMessage(Long toUserId, Long fromUserId, Long messageId, String content);

    /**
     * 发送消息已读回执
     *
     * @param toUserId   接收者用户ID（原消息发送者）
     * @param fromUserId 发送者用户ID（原消息接收者）
     * @param messageId  消息ID
     * @return 是否发送成功
     */
    boolean sendMessageReadReceipt(Long toUserId, Long fromUserId, Long messageId);

    /**
     * 发送输入状态提示
     *
     * @param toUserId   接收者用户ID
     * @param fromUserId 发送者用户ID
     * @param conversationId 会话ID
     * @return 是否发送成功
     */
    boolean sendTypingIndicator(Long toUserId, Long fromUserId, Long conversationId);

    /**
     * 发送系统通知
     *
     * @param userId 用户ID
     * @param title  通知标题
     * @param message 通知内容
     * @return 是否发送成功
     */
    boolean sendSystemNotification(Long userId, String title, String message);

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(Long userId);

    /**
     * 获取当前在线用户数
     *
     * @return 在线用户数
     */
    int getOnlineUserCount();
}
