package com.xx.xianqijava.service;

import com.xx.xianqijava.entity.SystemNotification;

/**
 * 业务通知服务接口
 * 用于发送各种业务相关的系统通知
 *
 * @author Claude Code
 * @since 2026-03-08
 */
public interface BusinessNotificationService {

    /**
     * 发送订单状态变更通知
     *
     * @param userId     接收通知的用户ID
     * @param orderId    订单ID
     * @param orderNo    订单号
     * @param statusDesc 状态描述
     */
    void sendOrderStatusNotification(Long userId, Long orderId, String orderNo, String statusDesc);

    /**
     * 发送转赠通知
     *
     * @param toUserId       接收用户ID
     * @param fromUserId     发送用户ID
     * @param transferId     转赠记录ID
     * @param shareItemTitle 共享物品标题
     * @param notificationType 通知类型：1-接收转赠请求，2-转赠完成，3-转赠拒绝，4-转赠取消
     */
    void sendTransferNotification(Long toUserId, Long fromUserId, Long transferId,
                                  String shareItemTitle, Integer notificationType);

    /**
     * 发送评价提醒通知
     *
     * @param userId  接收用户ID
     * @param orderId 订单ID
     * @param orderNo 订单号
     */
    void sendEvaluationReminderNotification(Long userId, Long orderId, String orderNo);

    /**
     * 发送系统公告
     *
     * @param title       公告标题
     * @param content     公告内容
     * @param targetType  目标类型：1-全部用户，2-指定用户
     * @param targetUsers 目标用户列表（JSON格式）
     * @param priority    优先级：1-低，2-中，3-高
     */
    void sendSystemAnnouncement(String title, String content, Integer targetType,
                                String targetUsers, Integer priority);

    /**
     * 发送交易提醒
     *
     * @param userId  接收用户ID
     * @param title   提醒标题
     * @param content 提醒内容
     */
    void sendTradeReminder(Long userId, String title, String content);

    /**
     * 发送账户提醒
     *
     * @param userId  接收用户ID
     * @param title   提醒标题
     * @param content 提醒内容
     */
    void sendAccountReminder(Long userId, String title, String content);
}
