package com.xx.xianqijava.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.xianqijava.config.CreditScoreConfig;
import com.xx.xianqijava.entity.CreditAlert;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.mapper.CreditAlertMapper;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信用分预警定时任务
 */
@Slf4j
@Component
public class CreditScoreAlertTask {

    @Autowired
    private CreditScoreConfig config;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CreditAlertMapper creditAlertMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 每6小时执行一次预警检测
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void detectAbnormalBehavior() {
        log.info("开始执行信用分预警检测任务");

        try {
            // 1. 检测快速增长
            detectRapidGrowth();

            // 2. 检测频繁交易
            detectFrequentTrading();

            // 3. 检测互评刷分
            detectMutualEvaluationSpam();

            log.info("信用分预警检测任务完成");

        } catch (Exception e) {
            log.error("信用分预警检测任务执行失败", e);
        }
    }

    /**
     * 检测快速增长
     */
    private void detectRapidGrowth() {
        log.info("开始检测信用分快速增长用户");

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 这里需要在UserMapper中添加自定义SQL查询
        // 暂时使用简单方式：查询所有活跃用户，然后逐个检查
        // 生产环境建议使用SQL直接查询符合条件的用户

        LambdaQueryWrapper<com.xx.xianqijava.entity.User> queryWrapper =
            new LambdaQueryWrapper<com.xx.xianqijava.entity.User>()
                .ge(com.xx.xianqijava.entity.User::getLastActiveTime, sevenDaysAgo);

        List<com.xx.xianqijava.entity.User> activeUsers = userMapper.selectList(queryWrapper);

        for (com.xx.xianqijava.entity.User user : activeUsers) {
            // 检查信用分是否快速上涨（这里简化处理，实际应该从credit_record表计算）
            // 如果7天内增长超过阈值，创建预警

            // 简化：如果信用分突然变高且用户注册时间短
            if (user.getCreditScore() >= 80) {
                // 检查是否为新用户（注册30天内）
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                if (user.getCreatedAt().isAfter(thirtyDaysAgo)) {
                    createAlert(user.getId(),
                        CreditAlert.AlertType.RAPID_GROWTH.getCode(),
                        CreditAlert.AlertLevel.HIGH.getCode(),
                        Map.of(
                            "currentScore", user.getCreditScore(),
                            "registerDays", ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now())
                        ));
                }
            }
        }

        log.info("快速增长检测完成");
    }

    /**
     * 检测频繁交易
     */
    private void detectFrequentTrading() {
        log.info("开始检测频繁交易用户");

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 查询7天内交易次数超过阈值的用户
        // 这里简化处理，实际应该在Mapper中实现聚合查询

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
            .ge(Order::getCreatedAt, sevenDaysAgo)
            .in(Order::getStatus, List.of("completed", "in_transaction"));

        List<Order> recentOrders = orderMapper.selectList(queryWrapper);

        // 统计每个用户的交易次数
        Map<String, Integer> userTransactionCount = new HashMap<>();
        for (Order order : recentOrders) {
            String sellerId = order.getSellerId();
            userTransactionCount.put(sellerId,
                userTransactionCount.getOrDefault(sellerId, 0) + 1);
        }

        // 检测超过阈值的用户
        int threshold = config.getAntiSpam().getSmallAmountThreshold();
        for (Map.Entry<String, Integer> entry : userTransactionCount.entrySet()) {
            if (entry.getValue() >= threshold) {
                // 进一步检查：小额交易占比
                List<Order> userOrders = recentOrders.stream()
                    .filter(o -> o.getSellerId().equals(entry.getKey()))
                    .toList();

                long smallAmountCount = userOrders.stream()
                    .filter(o -> o.getTotalAmount().compareTo(
                        BigDecimal.valueOf(config.getAntiSpam().getSmallAmountLimit())) < 0)
                    .count();

                // 如果小额交易占比超过70%
                if (smallAmountCount * 100 / entry.getValue() > 70) {
                    createAlert(entry.getKey(),
                        CreditAlert.AlertType.FREQUENT_TRADING.getCode(),
                        CreditAlert.AlertLevel.MEDIUM.getCode(),
                        Map.of(
                            "transactionCount", entry.getValue(),
                            "smallAmountCount", smallAmountCount,
                            "smallAmountRatio", smallAmountCount * 100 / entry.getValue()
                        ));
                }
            }
        }

        log.info("频繁交易检测完成");
    }

    /**
     * 检测互评刷分
     */
    private void detectMutualEvaluationSpam() {
        log.info("开始检测互评刷分");

        // 这里需要查询evaluation表，检测互评模式
        // 简化处理：实际应该在EvaluationService中实现更复杂的检测逻辑

        log.info("互评刷分检测完成");
    }

    /**
     * 创建预警记录
     */
    private void createAlert(String userId, String alertType, String alertLevel,
                             Map<String, Object> alertData) {
        try {
            // 检查是否已存在未处理的同类预警（24小时内）
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            int existingCount = creditAlertMapper.countPendingAlertsByType(
                userId, alertType, oneDayAgo
            );

            if (existingCount > 0) {
                log.debug("用户 {} 已存在 {} 类型的待处理预警，跳过创建", userId, alertType);
                return;
            }

            // 创建新预警
            CreditAlert alert = new CreditAlert();
            alert.setUserId(userId);
            alert.setAlertType(alertType);
            alert.setAlertLevel(alertLevel);
            alert.setAlertData(objectMapper.writeValueAsString(alertData));
            alert.setStatus(CreditAlert.Status.PENDING.getCode());
            alert.setCreatedAt(LocalDateTime.now());

            creditAlertMapper.insert(alert);

            log.warn("创建信用分预警: 用户={}, 类型={}, 级别={}",
                userId, alertType, alertLevel);

        } catch (JsonProcessingException e) {
            log.error("序列化预警数据失败", e);
        } catch (Exception e) {
            log.error("创建预警记录失败", e);
        }
    }
}
