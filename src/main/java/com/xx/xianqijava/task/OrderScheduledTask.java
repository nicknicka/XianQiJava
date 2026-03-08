package com.xx.xianqijava.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.enums.OrderStatus;
import com.xx.xianqijava.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单定时任务
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduledTask {

    private final OrderMapper orderMapper;

    @Value("${business.order.auto-close-time:1800}")
    private int autoCloseTimeSeconds;

    @Value("${business.order.auto-finish-time:604800}")
    private int autoFinishTimeSeconds;

    /**
     * 自动关闭超时未支付的订单
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void autoClosePendingOrders() {
        log.info("开始执行自动关闭待支付订单任务");

        try {
            LocalDateTime expireTime = LocalDateTime.now().minusSeconds(autoCloseTimeSeconds);

            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getStatus, OrderStatus.PENDING.getCode())
                    .le(Order::getCreateTime, expireTime)
                    .set(Order::getStatus, OrderStatus.CANCELLED.getCode())
                    .set(Order::getCancelReason, "超时未支付自动关闭");

            int updatedCount = orderMapper.update(null, updateWrapper);

            log.info("自动关闭待支付订单任务完成，关闭订单数：{}", updatedCount);
        } catch (Exception e) {
            log.error("自动关闭待支付订单任务执行失败", e);
        }
    }

    /**
     * 自动关闭超时未确认的订单
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void autoCloseUnconfirmedOrders() {
        log.info("开始执行自动关闭待确认订单任务");

        try {
            // 待确认订单超时时间：支付后3天未确认
            LocalDateTime expireTime = LocalDateTime.now().minusDays(3);

            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getStatus, OrderStatus.PAID.getCode())
                    .le(Order::getPayTime, expireTime)
                    .set(Order::getStatus, OrderStatus.CANCELLED.getCode())
                    .set(Order::getCancelReason, "超时未确认自动关闭");

            int updatedCount = orderMapper.update(null, updateWrapper);

            log.info("自动关闭待确认订单任务完成，关闭订单数：{}", updatedCount);
        } catch (Exception e) {
            log.error("自动关闭待确认订单任务执行失败", e);
        }
    }

    /**
     * 自动完成已发货超时的订单
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000)
    public void autoFinishShippedOrders() {
        log.info("开始执行自动完成已发货订单任务");

        try {
            LocalDateTime expireTime = LocalDateTime.now().minusSeconds(autoFinishTimeSeconds);

            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getStatus, OrderStatus.SHIPPED.getCode())
                    .le(Order::getShipTime, expireTime)
                    .set(Order::getStatus, OrderStatus.COMPLETED.getCode())
                    .set(Order::getFinishTime, LocalDateTime.now());

            int updatedCount = orderMapper.update(null, updateWrapper);

            log.info("自动完成已发货订单任务完成，完成订单数：{}", updatedCount);
        } catch (Exception e) {
            log.error("自动完成已发货订单任务执行失败", e);
        }
    }

    /**
     * 自动完成待收货超时的订单
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000)
    public void autoFinishDeliveredOrders() {
        log.info("开始执行自动完成待收货订单任务");

        try {
            // 待收货订单超时时间：发货后15天自动完成
            LocalDateTime expireTime = LocalDateTime.now().minusDays(15);

            LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Order::getStatus, OrderStatus.DELIVERED.getCode())
                    .le(Order::getDeliverTime, expireTime)
                    .set(Order::getStatus, OrderStatus.COMPLETED.getCode())
                    .set(Order::getFinishTime, LocalDateTime.now());

            int updatedCount = orderMapper.update(null, updateWrapper);

            log.info("自动完成待收货订单任务完成，完成订单数：{}", updatedCount);
        } catch (Exception e) {
            log.error("自动完成待收货订单任务执行失败", e);
        }
    }

    /**
     * 自动取消超时未同意的转赠请求
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000)
    public void autoCancelExpiredTransfers() {
        log.info("开始执行自动取消超时转赠请求任务");

        try {
            // 转赠请求超时时间：7天未同意自动取消
            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);

            // TODO: 实现转赠记录的自动取消逻辑
            // 需要查询 share_item_transfer 表中状态为待处理且创建时间超过7天的记录

            log.info("自动取消超时转赠请求任务完成");
        } catch (Exception e) {
            log.error("自动取消超时转赠请求任务执行失败", e);
        }
    }
}
