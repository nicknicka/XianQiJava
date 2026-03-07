package com.xx.xianqijava.service;

import com.xx.xianqijava.config.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * ID生成服务
 *
 * <p>提供统一的ID生成接口，支持：
 * <ul>
 *   <li>雪花算法ID（用于数据库主键）</li>
 *   <li>业务编号（用于对外展示，如订单号）</li>
 * </ul>
 *
 * @author Claude
 * @since 2025-03-07
 */
@Service
public class IdGeneratorService {

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private final Random random = new Random();

    // ==================== 雪花算法ID生成 ====================

    /**
     * 生成雪花ID
     *
     * <p>适用于普通业务场景（用户、商品、订单等）
     * <p>性能：单机 > 100万/秒
     *
     * @return 雪花ID
     */
    public long generateUid() {
        return snowflakeIdGenerator.nextId();
    }

    /**
     * 批量生成雪花ID
     *
     * @param count 生成数量
     * @return 雪花ID数组
     */
    public long[] generateUids(int count) {
        return snowflakeIdGenerator.nextIds(count);
    }

    /**
     * 解析雪花ID，获取时间戳信息
     *
     * @param uid 雪花ID
     * @return 时间戳（毫秒）
     */
    public long parseUidTimestamp(long uid) {
        return snowflakeIdGenerator.parseTimestamp(uid);
    }

    // ==================== 业务编号生成 ====================

    /**
     * 生成订单号
     *
     * <p>格式：XD + 19位雪花ID
     * <p>示例：XD1234567890123456789
     *
     * @return 订单号
     */
    public String generateOrderNo() {
        return "XD" + generateUid();
    }

    /**
     * 生成退款单号
     *
     * <p>格式：RF + yyMMdd + 6位随机数
     * <p>示例：RF250307123456
     *
     * @return 退款单号
     */
    public String generateRefundNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomStr = String.format("%06d", random.nextInt(1000000));
        return "RF" + timestamp + randomStr;
    }

    /**
     * 生成支付流水号
     *
     * <p>格式：PAY + 19位雪花ID
     * <p>示例：PAY1234567890123456789
     *
     * @return 支付流水号
     */
    public String generatePaymentNo() {
        return "PAY" + generateUid();
    }

    /**
     * 生成押金支付流水号
     *
     * <p>格式：DEP + 19位雪花ID
     * <p>示例：DEP1234567890123456789
     *
     * @return 押金支付流水号
     */
    public String generateDepositNo() {
        return "DEP" + generateUid();
    }

    /**
     * 生成商品编号
     *
     * <p>格式：PROD + 年份后2位 + 10位序号（从Redis获取）
     * <p>示例：PROD2500000000001
     *
     * @return 商品编号
     */
    public String generateProductNo() {
        // TODO: 使用Redis自增实现，这里暂时用雪花ID
        return "PROD" + generateUid();
    }

    /**
     * 生成优惠券码
     *
     * <p>格式：8位随机字母数字混合
     * <p>示例：AB12CD34
     *
     * @return 优惠券码
     */
    public String generateCouponCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去除易混淆字符
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 生成分享码
     *
     * <p>格式：6位随机数字
     * <p>示例：123456
     *
     * @return 分享码
     */
    public String generateShareCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 生成图片UUID
     *
     * <p>格式：32位随机字符串（不含横线）
     * <p>示例：a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
     *
     * @return 图片UUID
     */
    public String generateImageUuid() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成会话ID（用于WebSocket等场景）
     *
     * <p>格式：会话标识 + 雪花ID
     * <p>示例：CONV1234567890123456789
     *
     * @return 会话ID
     */
    public String generateConversationId() {
        return "CONV" + generateUid();
    }
}
