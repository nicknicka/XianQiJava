package com.xx.xianqijava;

import com.xx.xianqijava.service.IdGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 快速验证ID生成功能
 */
@SpringBootTest
public class QuickIdGeneratorTest {

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Test
    public void testQuickGeneration() {
        System.out.println("========================================");
        System.out.println("ID生成功能快速验证");
        System.out.println("========================================");

        // 测试雪花ID生成
        long uid = idGeneratorService.generateUid();
        System.out.println("✅ 雪花ID: " + uid);
        assertTrue(uid > 0, "ID应该大于0");

        // 测试订单号生成
        String orderNo = idGeneratorService.generateOrderNo();
        System.out.println("✅ 订单号: " + orderNo);
        assertTrue(orderNo.startsWith("XD"), "订单号应该以XD开头");

        // 测试退款单号生成
        String refundNo = idGeneratorService.generateRefundNo();
        System.out.println("✅ 退款单号: " + refundNo);
        assertTrue(refundNo.startsWith("RF"), "退款单号应该以RF开头");

        // 测试支付流水号生成
        String paymentNo = idGeneratorService.generatePaymentNo();
        System.out.println("✅ 支付流水号: " + paymentNo);
        assertTrue(paymentNo.startsWith("PAY"), "支付流水号应该以PAY开头");

        // 测试优惠券码生成
        String couponCode = idGeneratorService.generateCouponCode();
        System.out.println("✅ 优惠券码: " + couponCode);
        assertEquals(8, couponCode.length(), "优惠券码应该为8位");

        System.out.println("========================================");
        System.out.println("✅ 所有ID生成功能正常！");
        System.out.println("========================================");
    }
}
