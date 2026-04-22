package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.entity.SystemNotification;
import com.xx.xianqijava.service.BusinessNotificationService;
import com.xx.xianqijava.service.SystemNotificationService;
import com.xx.xianqijava.service.WebSocketMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 业务通知服务实现类
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessNotificationServiceImpl implements BusinessNotificationService {

    private final SystemNotificationService systemNotificationService;
    private final WebSocketMessageService webSocketMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendOrderStatusNotification(Long userId, Long orderId, String orderNo, String statusDesc) {
        log.info("发送订单状态变更通知, userId={}, orderId={}, status={}", userId, orderId, statusDesc);

        String title = "订单状态更新";
        String content = String.format("您的订单 %s 状态已变更为：%s", orderNo, statusDesc);

        SystemNotification notification = new SystemNotification();
        notification.setType(4); // 交易提醒
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(2); // 指定用户
        notification.setTargetUsers("[" + userId + "]");
        notification.setPriority(2); // 中等优先级
        notification.setStatus(1); // 已发布
        notification.setPublishTime(LocalDateTime.now());
        notification.setCreateTime(LocalDateTime.now());

        boolean saved = systemNotificationService.save(notification);
        if (saved) {
            // 通过 WebSocket 推送通知
            webSocketMessageService.sendSystemNotification(userId, title, content);
            log.info("订单状态变更通知发送成功, notificationId={}", notification.getNotificationId());
        } else {
            log.error("订单状态变更通知发送失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendTransferNotification(Long toUserId, Long fromUserId, Long transferId,
                                        String shareItemTitle, Integer notificationType) {
        log.info("发送转赠通知, toUserId={}, fromUserId={}, type={}", toUserId, fromUserId, notificationType);

        String title;
        String content;

        switch (notificationType) {
            case 1: // 接收转赠请求
                title = "转赠请求";
                content = String.format("您收到一个新的转赠请求：物品「%s」", shareItemTitle);
                break;
            case 2: // 转赠完成
                title = "转赠完成";
                content = String.format("物品「%s」已成功转赠给您", shareItemTitle);
                break;
            case 3: // 转赠拒绝
                title = "转赠已拒绝";
                content = String.format("对方拒绝了您的转赠请求：物品「%s」", shareItemTitle);
                break;
            case 4: // 转赠取消
                title = "转赠已取消";
                content = String.format("物品「%s」的转赠已被取消", shareItemTitle);
                break;
            default:
                log.warn("未知的转赠通知类型: {}", notificationType);
                return;
        }

        SystemNotification notification = new SystemNotification();
        notification.setType(4); // 交易提醒
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(2); // 指定用户
        notification.setTargetUsers("[" + toUserId + "]");
        notification.setPriority(2); // 中等优先级
        notification.setStatus(1); // 已发布
        notification.setPublishTime(LocalDateTime.now());
        notification.setCreateTime(LocalDateTime.now());

        boolean saved = systemNotificationService.save(notification);
        if (saved) {
            // 通过 WebSocket 推送通知
            webSocketMessageService.sendSystemNotification(toUserId, title, content);
            log.info("转赠通知发送成功, notificationId={}", notification.getNotificationId());
        } else {
            log.error("转赠通知发送失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEvaluationReminderNotification(Long userId, Long orderId, String orderNo) {
        log.info("发送评价提醒通知, userId={}, orderId={}", userId, orderId);

        String title = "待评价提醒";
        String content = String.format("您的订单 %s 交易已完成，快去评价吧！", orderNo);

        SystemNotification notification = new SystemNotification();
        notification.setType(4); // 交易提醒
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(2); // 指定用户
        notification.setTargetUsers("[" + userId + "]");
        notification.setPriority(1); // 低优先级
        notification.setStatus(1); // 已发布
        notification.setPublishTime(LocalDateTime.now());
        notification.setCreateTime(LocalDateTime.now());

        boolean saved = systemNotificationService.save(notification);
        if (saved) {
            // 通过 WebSocket 推送通知
            webSocketMessageService.sendSystemNotification(userId, title, content);
            log.info("评价提醒通知发送成功, notificationId={}", notification.getNotificationId());
        } else {
            log.error("评价提醒通知发送失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemAnnouncement(String title, String content, Integer targetType,
                                       String targetUsers, Integer priority) {
        log.info("发送系统公告, title={}, targetType={}", title, targetType);

        SystemNotification notification = new SystemNotification();
        notification.setType(1); // 系统公告
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetUsers(targetUsers);
        notification.setPriority(priority != null ? priority : 2); // 默认中等优先级
        notification.setStatus(1); // 已发布
        notification.setPublishTime(LocalDateTime.now());
        notification.setCreateTime(LocalDateTime.now());

        boolean saved = systemNotificationService.save(notification);
        if (saved) {
            log.info("系统公告发送成功, notificationId={}", notification.getNotificationId());

            // 如果是全部用户，通过 WebSocket 广播
            if (targetType == 1) {
                webSocketMessageService.broadcastToAll("system_notification", java.util.Map.of(
                        "title", title,
                        "message", content,
                        "type", "announcement",
                        "priority", notification.getPriority(),
                        "notificationId", notification.getNotificationId()
                ));
            }
        } else {
            log.error("系统公告发送失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendTradeReminder(Long userId, String title, String content) {
        log.info("发送交易提醒, userId={}, title={}", userId, title);

        SystemNotification notification = new SystemNotification();
        notification.setType(4); // 交易提醒
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(2); // 指定用户
        notification.setTargetUsers("[" + userId + "]");
        notification.setPriority(2); // 中等优先级
        notification.setStatus(1); // 已发布
        notification.setPublishTime(LocalDateTime.now());
        notification.setCreateTime(LocalDateTime.now());

        boolean saved = systemNotificationService.save(notification);
        if (saved) {
            // 通过 WebSocket 推送通知
            webSocketMessageService.sendSystemNotification(userId, title, content);
            log.info("交易提醒发送成功, notificationId={}", notification.getNotificationId());
        } else {
            log.error("交易提醒发送失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendAccountReminder(Long userId, String title, String content) {
        log.info("发送账户提醒, userId={}, title={}", userId, title);

        SystemNotification notification = new SystemNotification();
        notification.setType(3); // 账户提醒
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(2); // 指定用户
        notification.setTargetUsers("[" + userId + "]");
        notification.setPriority(3); // 高优先级
        notification.setStatus(1); // 已发布
        notification.setPublishTime(LocalDateTime.now());
        notification.setCreateTime(LocalDateTime.now());

        boolean saved = systemNotificationService.save(notification);
        if (saved) {
            // 通过 WebSocket 推送通知
            webSocketMessageService.sendSystemNotification(userId, title, content);
            log.info("账户提醒发送成功, notificationId={}", notification.getNotificationId());
        } else {
            log.error("账户提醒发送失败");
        }
    }
}
