package com.xx.xianqijava.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付服务接口
 *
 * @author Claude Code
 * @since 2026-03-08
 */
public interface PaymentService {

    /**
     * 创建支付订单
     *
     * @param orderId   订单ID（系统内部订单号）
     * @param amount    支付金额
     * @param subject   支付标题/商品名称
     * @param body      支付描述
     * @return 支付信息（包含支付URL或二维码）
     */
    Map<String, Object> createPayment(Long orderId, BigDecimal amount, String subject, String body);

    /**
     * 创建押金支付
     *
     * @param bookingId 预约ID
     * @param amount    押金金额
     * @param shareId   共享物品ID
     * @return 支付信息
     */
    Map<String, Object> createDepositPayment(Long bookingId, BigDecimal amount, Long shareId);

    /**
     * 处理支付回调
     *
     * @param params 支付平台回调参数
     * @return 处理结果
     */
    boolean handlePaymentCallback(Map<String, String> params);

    /**
     * 查询支付状态
     *
     * @param outTradeNo 商户订单号
     * @return 支付状态（SUCCESS-成功，PROCESSING-处理中，FAILED-失败）
     */
    String queryPaymentStatus(String outTradeNo);

    /**
     * 申请退款
     *
     * @param outTradeNo   原交易订单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果（包含退款ID）
     */
    Map<String, Object> refund(String outTradeNo, BigDecimal refundAmount, String refundReason);

    /**
     * 查询退款状态
     *
     * @param refundNo 退款单号
     * @return 退款状态（SUCCESS-成功，PROCESSING-处理中，FAILED-失败）
     */
    String queryRefundStatus(String refundNo);

    /**
     * 关闭订单
     *
     * @param outTradeNo 商户订单号
     * @return 是否成功
     */
    boolean closeOrder(String outTradeNo);
}
