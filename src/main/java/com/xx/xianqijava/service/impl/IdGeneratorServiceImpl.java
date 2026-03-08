package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.service.IdGeneratorService;
import com.xx.xianqijava.service.RedisIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ID 生成器服务实现类
 * 基于 Redis 实现分布式唯一 ID 生成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdGeneratorServiceImpl implements IdGeneratorService, RedisIdGenerator {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ID_PREFIX = "id:generator:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public String generateId(String prefix) {
        // 使用当前日期作为业务键
        String dateKey = LocalDateTime.now().format(DATE_FORMATTER);
        return generateId(prefix, dateKey);
    }

    @Override
    public String generateId(String prefix, String businessKey) {
        String key = ID_PREFIX + prefix + ":" + businessKey;

        // 使用 RedisAtomicLong 保证原子性递增
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        long seq = counter.incrementAndGet();

        // 重置计数器（每天从1开始）
        if (seq == 1) {
            counter.expireAt(java.time.LocalDate.now().plusDays(1).atStartOfDay());
        }

        // 格式化序列号，补齐4位
        String seqStr = String.format("%04d", seq);

        String id = prefix.toUpperCase() + businessKey + seqStr;
        log.debug("生成ID: {}", id);

        return id;
    }

    @Override
    public String generateIncrementId(String prefix, String dateKey) {
        // 调用现有的 generateId 方法
        return generateId(prefix, dateKey);
    }

    @Override
    public String generateOrderNo() {
        // 订单号格式：ORDER + yyyyMMdd + 序号
        return generateId("ORDER");
    }

    @Override
    public String generateRefundNo() {
        // 退款单号格式：REFUND + yyyyMMdd + 序号
        return generateId("REFUND");
    }
}
