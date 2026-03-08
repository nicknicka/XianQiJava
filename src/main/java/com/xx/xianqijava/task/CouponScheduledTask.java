package com.xx.xianqijava.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.xianqijava.entity.Coupon;
import com.xx.xianqijava.entity.UserCoupon;
import com.xx.xianqijava.mapper.CouponMapper;
import com.xx.xianqijava.mapper.UserCouponMapper;
import com.xx.xianqijava.service.BusinessNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 优惠券定时任务
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponScheduledTask {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final BusinessNotificationService businessNotificationService;

    /**
     * 过期用户优惠券
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000)
    public void expireUserCoupons() {
        log.info("开始执行过期用户优惠券任务");

        try {
            LocalDateTime now = LocalDateTime.now();

            // 将过期时间小于当前时间且状态为未使用的用户优惠券标记为已过期
            LambdaUpdateWrapper<UserCoupon> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserCoupon::getStatus, 1) // 未使用
                    .lt(UserCoupon::getExpireTime, now) // 已过期
                    .set(UserCoupon::getStatus, 3); // 已过期

            int updatedCount = userCouponMapper.update(null, updateWrapper);

            log.info("过期用户优惠券任务完成，过期优惠券数：{}", updatedCount);
        } catch (Exception e) {
            log.error("过期用户优惠券任务执行失败", e);
        }
    }

    /**
     * 结束过期的优惠券活动
     * 每天凌晨0点执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void endExpiredCouponActivities() {
        log.info("开始执行结束过期优惠券活动任务");

        try {
            LocalDateTime now = LocalDateTime.now();

            // 将有效结束时间小于当前时间且状态为进行中的优惠券标记为已结束
            LambdaUpdateWrapper<Coupon> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Coupon::getStatus, 1) // 进行中
                    .lt(Coupon::getValidTo, now) // 已过期
                    .set(Coupon::getStatus, 2); // 已结束

            int updatedCount = couponMapper.update(null, updateWrapper);

            log.info("结束过期优惠券活动任务完成，结束活动数：{}", updatedCount);
        } catch (Exception e) {
            log.error("结束过期优惠券活动任务执行失败", e);
        }
    }

    /**
     * 发送即将过期的优惠券提醒通知
     * 每天上午9点和下午5点执行
     */
    @Scheduled(cron = "0 0 9,17 * * ?")
    public void sendExpiringCouponReminders() {
        log.info("开始执行发送即将过期优惠券提醒任务");

        try {
            // 查询即将过期（3天内）且状态为未使用的用户优惠券
            LocalDateTime threeDaysLater = LocalDateTime.now().plusDays(3);

            // TODO: 查询即将过期的用户优惠券并发送提醒通知
            // 注意：避免重复提醒，需要记录已提醒过的优惠券
            // 可以在 user_coupon 表中添加一个提醒字段

            log.info("发送即将过期优惠券提醒任务完成");
        } catch (Exception e) {
            log.error("发送即将过期优惠券提醒任务执行失败", e);
        }
    }

    /**
     * 清理长期未使用且已过期的用户优惠券记录
     * 每周日凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void cleanExpiredUserCoupons() {
        log.info("开始执行清理过期用户优惠券记录任务");

        try {
            // 删除过期时间超过90天的已过期用户优惠券记录
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);

            // TODO: 执行软删除或硬删除
            // 建议使用逻辑删除，保留记录用于数据分析

            log.info("清理过期用户优惠券记录任务完成");
        } catch (Exception e) {
            log.error("清理过期用户优惠券记录任务执行失败", e);
        }
    }
}
