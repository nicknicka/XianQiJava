package com.xx.xianqijava.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.entity.TransferRecord;
import com.xx.xianqijava.enums.OrderStatus;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ShareItemMapper;
import com.xx.xianqijava.mapper.TransferRecordMapper;
import com.xx.xianqijava.service.BusinessNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单定时任务
 * <p>
 * 校园二手交易平台采用线下交易模式，订单状态简化为：
 * - 0: 待确认（订单创建后等待卖家确认）
 * - 1: 进行中（订单已确认，双方线下交易）
 * - 2: 已完成（交易完成）
 * - 3: 已取消（订单已取消）
 * - 4: 退款中（退款处理中）
 *
 * @author Claude Code
 * @since 2026-03-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduledTask {

    private final OrderMapper orderMapper;
    private final TransferRecordMapper transferRecordMapper;
    private final ShareItemMapper shareItemMapper;
    private final BusinessNotificationService businessNotificationService;

    /**
     * 待确认订单自动关闭时间（默认7天）
     * 订单创建后7天内卖家未确认，自动取消
     */
    @Value("${business.order.auto-close-time:604800}")
    private int autoCloseTimeSeconds;

    /**
     * 进行中订单自动完成时间（默认30天）
     * 订单确认后30天内买家未确认完成，自动完成
     */
    @Value("${business.order.auto-finish-time:2592000}")
    private int autoFinishTimeSeconds;

    @Value("${business.transfer.auto-close-time:604800}")
    private int transferAutoCloseTimeSeconds;

    /**
     * 自动关闭超时待确认的订单
     * 每10分钟执行一次
     * <p>
     * 逻辑：订单创建后超过指定时间卖家仍未确认，自动取消订单
     */
    @Scheduled(fixedRate = 600000)
    public void autoCloseUnconfirmedOrders() {
        log.info("开始执行自动关闭待确认订单任务");

        try {
            LocalDateTime expireTime = LocalDateTime.now().minusSeconds(autoCloseTimeSeconds);

            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getStatus, OrderStatus.PENDING_CONFIRM.getCode())
                    .le(Order::getCreateTime, expireTime)
                    .set(Order::getStatus, OrderStatus.CANCELLED.getCode())
                    .set(Order::getCancelReason, "超时未确认自动关闭");

            int updatedCount = orderMapper.update(null, updateWrapper);

            if (updatedCount > 0) {
                log.info("自动关闭待确认订单任务完成，关闭订单数：{}", updatedCount);
            } else {
                log.debug("自动关闭待确认订单任务完成，无需要关闭的订单");
            }
        } catch (Exception e) {
            log.error("自动关闭待确认订单任务执行失败", e);
        }
    }

    /**
     * 自动完成超时进行中的订单
     * 每1小时执行一次
     * <p>
     * 逻辑：订单确认后超过指定时间买家未确认收货，自动完成订单
     * 适用于线下交易场景：双方已线下完成交易但忘记在平台上确认
     */
    @Scheduled(fixedRate = 3600000)
    public void autoFinishInProgressOrders() {
        log.info("开始执行自动完成进行中订单任务");

        try {
            // 计算过期时间（当前时间减去配置的超时时间）
            LocalDateTime expireTime = LocalDateTime.now().minusSeconds(autoFinishTimeSeconds);

            // 查询所有进行中且超过超时时间的订单
            // 使用更新时间作为判断标准（卖家确认订单的时间）
            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getStatus, OrderStatus.IN_PROGRESS.getCode())
                    .le(Order::getUpdateTime, expireTime)
                    .set(Order::getStatus, OrderStatus.COMPLETED.getCode())
                    .set(Order::getFinishTime, LocalDateTime.now())
                    .set(Order::getConfirmTime, LocalDateTime.now()); // 设置自动确认收货时间

            int updatedCount = orderMapper.update(null, updateWrapper);

            if (updatedCount > 0) {
                log.info("自动完成进行中订单任务完成，自动确认收货订单数：{}", updatedCount);
            } else {
                log.debug("自动完成进行中订单任务完成，无需要自动确认的订单");
            }
        } catch (Exception e) {
            log.error("自动完成进行中订单任务执行失败", e);
        }
    }

    /**
     * 自动取消超时转赠请求（共享物品功能）
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000)
    public void autoCancelExpiredTransfers() {
        log.info("开始执行自动取消超时转赠请求任务");

        try {
            LocalDateTime expireTime = LocalDateTime.now().minusSeconds(transferAutoCloseTimeSeconds);
            LambdaQueryWrapper<TransferRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TransferRecord::getAcceptStatus, 0)
                    .le(TransferRecord::getCreateTime, expireTime);

            java.util.List<TransferRecord> expiredTransfers = transferRecordMapper.selectList(wrapper);
            if (expiredTransfers.isEmpty()) {
                log.debug("自动取消超时转赠请求任务完成，无需处理");
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            for (TransferRecord transfer : expiredTransfers) {
                transfer.setAcceptStatus(2);
                transfer.setRejectReason("超时未处理自动关闭");
                transfer.setConfirmTime(now);
                transferRecordMapper.updateById(transfer);

                ShareItem shareItem = shareItemMapper.selectById(transfer.getShareId());
                String itemTitle = shareItem != null ? shareItem.getTitle() : "共享物品";
                businessNotificationService.sendTransferNotification(
                        transfer.getFromUserId(),
                        transfer.getToUserId(),
                        transfer.getTransferId(),
                        itemTitle,
                        3
                );
                businessNotificationService.sendTransferNotification(
                        transfer.getToUserId(),
                        transfer.getFromUserId(),
                        transfer.getTransferId(),
                        itemTitle,
                        4
                );
            }
            log.info("自动取消超时转赠请求任务完成，处理数量：{}", expiredTransfers.size());
        } catch (Exception e) {
            log.error("自动取消超时转赠请求任务执行失败", e);
        }
    }
}
