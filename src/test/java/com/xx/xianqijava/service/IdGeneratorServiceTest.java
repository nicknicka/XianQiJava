package com.xx.xianqijava.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ID生成服务测试类
 *
 * <p>测试各种ID生成功能是否正常工作
 *
 * @author Claude
 * @since 2025-03-07
 */
@SpringBootTest
public class IdGeneratorServiceTest {

    @Resource
    private IdGeneratorService idGeneratorService;

    /**
     * 测试生成雪花ID
     */
    @Test
    public void testGenerateUid() {
        long uid1 = idGeneratorService.generateUid();
        long uid2 = idGeneratorService.generateUid();

        // 验证ID是正数
        assertTrue(uid1 > 0, "生成的ID应该大于0");
        assertTrue(uid2 > 0, "生成的ID应该大于0");

        // 验证ID是唯一的
        assertNotEquals(uid1, uid2, "两次生成的ID应该不同");

        // 验证ID是递增的
        assertTrue(uid2 > uid1, "后生成的ID应该大于先生成的ID");

        System.out.println("生成的UID1: " + uid1);
        System.out.println("生成的UID2: " + uid2);
    }

    /**
     * 测试批量生成雪花ID
     */
    @Test
    public void testGenerateUids() {
        int count = 100;
        long[] uids = idGeneratorService.generateUids(count);

        // 验证数量
        assertEquals(count, uids.length, "生成的ID数量应该等于请求的数量");

        // 验证所有ID都是唯一的
        for (int i = 0; i < uids.length; i++) {
            for (int j = i + 1; j < uids.length; j++) {
                assertNotEquals(uids[i], uids[j], "批量生成的ID应该都是唯一的");
            }
        }

        // 验证ID是递增的
        for (int i = 1; i < uids.length; i++) {
            assertTrue(uids[i] > uids[i - 1], "批量生成的ID应该是递增的");
        }

        System.out.println("批量生成100个ID成功");
        System.out.println("第一个ID: " + uids[0]);
        System.out.println("最后一个ID: " + uids[uids.length - 1]);
    }

    /**
     * 测试生成订单号
     */
    @Test
    public void testGenerateOrderNo() {
        String orderNo1 = idGeneratorService.generateOrderNo();
        String orderNo2 = idGeneratorService.generateOrderNo();

        // 验证订单号格式：XD + 数字
        assertTrue(orderNo1.startsWith("XD"), "订单号应该以XD开头");
        assertTrue(orderNo2.startsWith("XD"), "订单号应该以XD开头");

        // 验证订单号长度：XD(2) + 18-19位数字 = 20-21位
        assertTrue(orderNo1.length() >= 20 && orderNo1.length() <= 21, "订单号长度应该为20-21位");
        assertTrue(orderNo2.length() >= 20 && orderNo2.length() <= 21, "订单号长度应该为20-21位");

        // 验证订单号是唯一的
        assertNotEquals(orderNo1, orderNo2, "两次生成的订单号应该不同");

        System.out.println("生成的订单号1: " + orderNo1);
        System.out.println("生成的订单号2: " + orderNo2);
    }

    /**
     * 测试生成退款单号
     */
    @Test
    public void testGenerateRefundNo() {
        String refundNo1 = idGeneratorService.generateRefundNo();
        String refundNo2 = idGeneratorService.generateRefundNo();

        // 验证退款单号格式：RF + yyMMdd + 6位随机数
        assertTrue(refundNo1.startsWith("RF"), "退款单号应该以RF开头");
        assertTrue(refundNo2.startsWith("RF"), "退款单号应该以RF开头");

        // 验证退款单号长度：RF(2) + yyMMdd(6) + 6位随机数 = 14位
        assertEquals(14, refundNo1.length(), "退款单号长度应该为14位");
        assertEquals(14, refundNo2.length(), "退款单号长度应该为14位");

        // 验证退款单号是唯一的
        assertNotEquals(refundNo1, refundNo2, "两次生成的退款单号应该不同");

        System.out.println("生成的退款单号1: " + refundNo1);
        System.out.println("生成的退款单号2: " + refundNo2);
    }

    /**
     * 测试生成支付流水号
     */
    @Test
    public void testGeneratePaymentNo() {
        String paymentNo1 = idGeneratorService.generatePaymentNo();
        String paymentNo2 = idGeneratorService.generatePaymentNo();

        // 验证支付流水号格式：PAY + 数字
        assertTrue(paymentNo1.startsWith("PAY"), "支付流水号应该以PAY开头");
        assertTrue(paymentNo2.startsWith("PAY"), "支付流水号应该以PAY开头");

        // 验证支付流水号长度：PAY(3) + 18-19位数字 = 21-22位
        assertTrue(paymentNo1.length() >= 21 && paymentNo1.length() <= 22, "支付流水号长度应该为21-22位");
        assertTrue(paymentNo2.length() >= 21 && paymentNo2.length() <= 22, "支付流水号长度应该为21-22位");

        // 验证支付流水号是唯一的
        assertNotEquals(paymentNo1, paymentNo2, "两次生成的支付流水号应该不同");

        System.out.println("生成的支付流水号1: " + paymentNo1);
        System.out.println("生成的支付流水号2: " + paymentNo2);
    }

    /**
     * 测试生成押金支付流水号
     */
    @Test
    public void testGenerateDepositNo() {
        String depositNo1 = idGeneratorService.generateDepositNo();
        String depositNo2 = idGeneratorService.generateDepositNo();

        // 验证押金流水号格式：DEP + 数字
        assertTrue(depositNo1.startsWith("DEP"), "押金流水号应该以DEP开头");
        assertTrue(depositNo2.startsWith("DEP"), "押金流水号应该以DEP开头");

        // 验证押金流水号长度：DEP(3) + 18-19位数字 = 21-22位
        assertTrue(depositNo1.length() >= 21 && depositNo1.length() <= 22, "押金流水号长度应该为21-22位");
        assertTrue(depositNo2.length() >= 21 && depositNo2.length() <= 22, "押金流水号长度应该为21-22位");

        // 验证押金流水号是唯一的
        assertNotEquals(depositNo1, depositNo2, "两次生成的押金流水号应该不同");

        System.out.println("生成的押金流水号1: " + depositNo1);
        System.out.println("生成的押金流水号2: " + depositNo2);
    }

    /**
     * 测试生成商品编号
     */
    @Test
    public void testGenerateProductNo() {
        String productNo1 = idGeneratorService.generateProductNo();
        String productNo2 = idGeneratorService.generateProductNo();

        // 验证商品编号格式：PROD + 数字
        assertTrue(productNo1.startsWith("PROD"), "商品编号应该以PROD开头");
        assertTrue(productNo2.startsWith("PROD"), "商品编号应该以PROD开头");

        // 验证商品编号是唯一的
        assertNotEquals(productNo1, productNo2, "两次生成的商品编号应该不同");

        System.out.println("生成的商品编号1: " + productNo1);
        System.out.println("生成的商品编号2: " + productNo2);
    }

    /**
     * 测试生成优惠券码
     */
    @Test
    public void testGenerateCouponCode() {
        String code1 = idGeneratorService.generateCouponCode();
        String code2 = idGeneratorService.generateCouponCode();

        // 验证优惠券码长度：8位
        assertEquals(8, code1.length(), "优惠券码长度应该为8位");
        assertEquals(8, code2.length(), "优惠券码长度应该为8位");

        // 验证优惠券码是大写字母和数字
        assertTrue(code1.matches("[A-Z0-9]+"), "优惠券码应该只包含大写字母和数字");
        assertTrue(code2.matches("[A-Z0-9]+"), "优惠券码应该只包含大写字母和数字");

        // 验证优惠券码是唯一的（大概率唯一，可能有碰撞）
        assertNotEquals(code1, code2, "两次生成的优惠券码应该不同");

        System.out.println("生成的优惠券码1: " + code1);
        System.out.println("生成的优惠券码2: " + code2);
    }

    /**
     * 测试生成分享码
     */
    @Test
    public void testGenerateShareCode() {
        String code1 = idGeneratorService.generateShareCode();
        String code2 = idGeneratorService.generateShareCode();

        // 验证分享码长度：6位
        assertEquals(6, code1.length(), "分享码长度应该为6位");
        assertEquals(6, code2.length(), "分享码长度应该为6位");

        // 验证分享码是数字
        assertTrue(code1.matches("\\d+"), "分享码应该只包含数字");
        assertTrue(code2.matches("\\d+"), "分享码应该只包含数字");

        System.out.println("生成的分享码1: " + code1);
        System.out.println("生成的分享码2: " + code2);
    }

    /**
     * 测试生成图片UUID
     */
    @Test
    public void testGenerateImageUuid() {
        String uuid1 = idGeneratorService.generateImageUuid();
        String uuid2 = idGeneratorService.generateImageUuid();

        // 验证UUID长度：32位（去掉横线）
        assertEquals(32, uuid1.length(), "图片UUID长度应该为32位");
        assertEquals(32, uuid2.length(), "图片UUID长度应该为32位");

        // 验证UUID是唯一的
        assertNotEquals(uuid1, uuid2, "两次生成的UUID应该不同");

        System.out.println("生成的图片UUID1: " + uuid1);
        System.out.println("生成的图片UUID2: " + uuid2);
    }

    /**
     * 测试解析雪花ID的时间戳
     */
    @Test
    public void testParseUidTimestamp() {
        long uid = idGeneratorService.generateUid();
        long timestamp = idGeneratorService.parseUidTimestamp(uid);

        // 验证时间戳是合理的（应该在2024-01-01到当前时间之间）
        long currentTimestamp = System.currentTimeMillis();
        long minTimestamp = 1704067200000L; // 2024-01-01 00:00:00 (毫秒)

        assertTrue(timestamp >= minTimestamp, "解析的时间戳应该不小于2024-01-01");
        assertTrue(timestamp <= currentTimestamp, "解析的时间戳应该不大于当前时间");

        System.out.println("UID: " + uid);
        System.out.println("解析的时间戳(毫秒): " + timestamp);
    }

    /**
     * 测试生成会话ID
     */
    @Test
    public void testGenerateConversationId() {
        String convId1 = idGeneratorService.generateConversationId();
        String convId2 = idGeneratorService.generateConversationId();

        // 验证会话ID格式：CONV + 数字
        assertTrue(convId1.startsWith("CONV"), "会话ID应该以CONV开头");
        assertTrue(convId2.startsWith("CONV"), "会话ID应该以CONV开头");

        // 验证会话ID是唯一的
        assertNotEquals(convId1, convId2, "两次生成的会话ID应该不同");

        System.out.println("生成的会话ID1: " + convId1);
        System.out.println("生成的会话ID2: " + convId2);
    }

    /**
     * 性能测试 - 生成10000个ID
     */
    @Test
    public void testPerformance() {
        int count = 10000;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            idGeneratorService.generateUid();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("生成" + count + "个ID耗时: " + duration + "ms");
        System.out.println("平均每个ID耗时: " + (duration * 1000000.0 / count) + "ns");
        System.out.println("QPS: " + (count * 1000.0 / duration));

        // 验证性能应该大于100万/秒（即每个ID耗时<1ms）
        assertTrue(duration < count, "生成10000个ID的时间应该小于10000ms");
    }
}
