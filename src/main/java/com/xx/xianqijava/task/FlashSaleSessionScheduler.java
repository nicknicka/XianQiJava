package com.xx.xianqijava.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xx.xianqijava.entity.FlashSaleProduct;
import com.xx.xianqijava.entity.FlashSaleSession;
import com.xx.xianqijava.entity.FlashSaleSessionTemplate;
import com.xx.xianqijava.mapper.FlashSaleProductMapper;
import com.xx.xianqijava.mapper.FlashSaleSessionMapper;
import com.xx.xianqijava.mapper.FlashSaleSessionTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 秒杀场次定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlashSaleSessionScheduler {

    private final FlashSaleSessionTemplateMapper templateMapper;
    private final FlashSaleSessionMapper sessionMapper;
    private final FlashSaleProductMapper productMapper;

    /**
     * 每天00:00更新固定场次的时间 + 重置每日重复商品库存
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateDailySessions() {
        LocalDate today = LocalDate.now();
        log.info("开始更新秒杀场次日期，当前日期：{}", today);

        // 更新4个固定场次的日期
        List<FlashSaleSession> sessions = sessionMapper.selectList(
            new LambdaQueryWrapper<FlashSaleSession>()
                .eq(FlashSaleSession::getSessionType, 1)
                .orderByAsc(FlashSaleSession::getSessionId)
        );

        for (FlashSaleSession session : sessions) {
            LocalTime time = LocalTime.parse(session.getSessionTime());
            LocalDateTime startTime = LocalDateTime.of(today, time);
            LocalDateTime endTime = startTime.plusHours(2);  // 固定2小时

            // 如果夜场跨天，endTime 需要调整
            if (time.isAfter(LocalTime.of(22, 0))) {
                endTime = startTime.plusHours(2);  // 22:00 + 2小时 = 00:00（跨天）
            }

            session.setStartTime(startTime);
            session.setEndTime(endTime);
            sessionMapper.updateById(session);

            log.info("更新场次：{} ({})", session.getName(), session.getSessionTime());
        }

        log.info("场次日期更新完成");

        // 重置每日重复商品的库存
        resetDailyRepeatProducts(today);
    }

    /**
     * 重置每日重复商品的库存
     */
    private void resetDailyRepeatProducts(LocalDate today) {
        log.info("开始重置每日重复商品库存...");

        LambdaUpdateWrapper<FlashSaleProduct> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FlashSaleProduct::getRepeatType, 1)  // 每日重复
                   .eq(FlashSaleProduct::getStockStatus, 1)   // 已售罄
                   .set(FlashSaleProduct::getSoldCount, 0)     // 重置已售数量
                   .set(FlashSaleProduct::getStockStatus, 0);  // 恢复在售状态

        int updated = productMapper.update(null, updateWrapper);
        log.info("重置了 {} 个每日重复商品的库存", updated);

        // 检查不重复商品，如果sale_date是昨天，标记为已结束
        LocalDate yesterday = today.minusDays(1);
        LambdaUpdateWrapper<FlashSaleProduct> expiredWrapper = new LambdaUpdateWrapper<>();
        expiredWrapper.eq(FlashSaleProduct::getRepeatType, 0)  // 不重复
                     .eq(FlashSaleProduct::getSaleDate, yesterday)  // 昨天的日期
                     .set(FlashSaleProduct::getStockStatus, 2);  // 标记为手动下架（已结束）

        int expired = productMapper.update(null, expiredWrapper);
        log.info("{} 个不重复商品已过期下架", expired);
    }

    /**
     * 手动触发更新（用于测试或补更新）
     */
    public void updateSessionsForDate(LocalDate date) {
        log.info("手动更新 {} 的秒杀场次日期...", date);

        List<FlashSaleSession> sessions = sessionMapper.selectList(
            new LambdaQueryWrapper<FlashSaleSession>()
                .eq(FlashSaleSession::getSessionType, 1)
                .orderByAsc(FlashSaleSession::getSessionId)
        );

        for (FlashSaleSession session : sessions) {
            LocalTime time = LocalTime.parse(session.getSessionTime());
            LocalDateTime startTime = LocalDateTime.of(date, time);
            LocalDateTime endTime = startTime.plusHours(2);

            if (time.isAfter(LocalTime.of(22, 0))) {
                endTime = startTime.plusHours(2);
            }

            session.setStartTime(startTime);
            session.setEndTime(endTime);
            sessionMapper.updateById(session);

            log.info("更新场次：{} ({})", session.getName(), date);
        }

        log.info("手动更新完成");
    }
}
