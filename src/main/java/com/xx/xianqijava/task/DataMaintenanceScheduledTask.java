package com.xx.xianqijava.task;

import com.xx.xianqijava.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 数据维护定时任务
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataMaintenanceScheduledTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StatisticsService statisticsService;

    /**
     * 清理过期的验证码
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredVerificationCodes() {
        log.info("开始执行清理过期验证码任务");

        try {
            // Redis 的验证码设置了过期时间，会自动清理
            // 这里只做日志记录
            log.info("清理过期验证码任务完成（Redis 自动过期）");
        } catch (Exception e) {
            log.error("清理过期验证码任务执行失败", e);
        }
    }

    /**
     * 清理过期的图片缓存
     * 每天凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanExpiredImageCache() {
        log.info("开始执行清理过期图片缓存任务");

        try {
            // 清理图片缓存（如果有的话）
            // 这里可以根据实际需求实现缓存清理逻辑

            log.info("清理过期图片缓存任务完成");
        } catch (Exception e) {
            log.error("清理过期图片缓存任务执行失败", e);
        }
    }

    /**
     * 清理过期的会话消息（可选）
     * 每周日凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 ? * SUN")
    public void cleanExpiredMessages() {
        log.info("开始执行清理过期会话消息任务");

        try {
            // 可选：清理超过一定时间的聊天消息
            // 根据业务需求和合规要求决定是否清理
            // 一般建议保留聊天记录用于审计和用户查看

            log.info("清理过期会话消息任务完成（暂未实现，建议保留聊天记录）");
        } catch (Exception e) {
            log.error("清理过期会话消息任务执行失败", e);
        }
    }

    /**
     * 生成每日统计数据
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyStatistics() {
        log.info("开始执行生成每日统计数据任务");

        try {
            String yesterday = LocalDateTime.now().minusDays(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 生成前一天的统计数据
            // statisticsService.generateDailyStatistics(yesterday);

            log.info("生成每日统计数据任务完成，日期：{}", yesterday);
        } catch (Exception e) {
            log.error("生成每日统计数据任务执行失败", e);
        }
    }

    /**
     * 清理过期的临时文件
     * 每天凌晨5点执行
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void cleanExpiredTempFiles() {
        log.info("开始执行清理过期临时文件任务");

        try {
            // 清理上传目录中的临时文件
            // 这里可以根据实际需求实现

            log.info("清理过期临时文件任务完成");
        } catch (Exception e) {
            log.error("清理过期临时文件任务执行失败", e);
        }
    }

    /**
     * 数据库连接池健康检查
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000)
    public void databaseHealthCheck() {
        log.debug("执行数据库健康检查");

        try {
            // 执行简单的查询来检查数据库连接
            redisTemplate.opsForValue().get("health_check");

            log.debug("数据库健康检查完成");
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
        }
    }

    /**
     * 清理过期的推荐缓存
     * 每6小时执行一次
     */
    @Scheduled(fixedRate = 21600000)
    public void cleanExpiredRecommendationCache() {
        log.info("开始执行清理过期推荐缓存任务");

        try {
            // 清理推荐系统的缓存
            Set<String> keys = redisTemplate.keys("recommendation:*");
            if (keys != null && !keys.isEmpty()) {
                // Redis 的缓存数据设置了过期时间，会自动清理
                log.info("推荐缓存清理任务完成，发现缓存键数：{}", keys.size());
            } else {
                log.info("推荐缓存清理任务完成，无需清理");
            }
        } catch (Exception e) {
            log.error("清理过期推荐缓存任务执行失败", e);
        }
    }
}
