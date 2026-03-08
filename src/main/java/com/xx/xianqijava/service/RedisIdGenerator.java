package com.xx.xianqijava.service;

/**
 * Redis 自增 ID 生成器服务接口
 */
public interface RedisIdGenerator {

    /**
     * 使用 Redis 生成指定前缀和日期的自增 ID
     *
     * @param prefix 前缀（如：ORDER、USER 等）
     * @param dateKey 日期键（如：20260308）
     * @return 唯一 ID（如：ORDER202603080001）
     */
    String generateIncrementId(String prefix, String dateKey);

    /**
     * 使用 Redis 自增生成订单号
     *
     * <p>格式：ORDER + yyyyMMdd + 4位序号
     * <p>示例：ORDER202603080001
     *
     * @return 订单号
     */
    String generateOrderNo();

    /**
     * 使用 Redis 自增生成退款单号
     *
     * <p>格式：REFUND + yyyyMMdd + 4位序号
     * <p>示例：REFUND202603080001
     *
     * @return 退款单号
     */
    String generateRefundNo();
}
