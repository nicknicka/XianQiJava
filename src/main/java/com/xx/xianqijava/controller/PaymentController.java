package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付控制器
 * 处理支付、退款等支付相关功能
 *
 * @author Claude Code
 * @since 2026-03-08
 */
@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "支付相关接口")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建支付订单", description = "创建支付订单，返回支付URL或二维码")
    public Result<Map<String, Object>> createPayment(
            @Parameter(description = "订单ID", required = true)
            @RequestParam Long orderId,
            @Parameter(description = "支付金额", required = true)
            @RequestParam BigDecimal amount,
            @Parameter(description = "支付标题", required = true)
            @RequestParam String subject,
            @Parameter(description = "支付描述", required = false)
            @RequestParam(defaultValue = "") String body) {

        log.info("创建支付订单 - orderId={}, amount={}, subject={}", orderId, amount, subject);

        Map<String, Object> result = paymentService.createPayment(orderId, amount, subject, body);
        if ((Boolean) result.getOrDefault("success", false)) {
            return Result.success("支付订单创建成功", result);
        } else {
            return Result.error((String) result.getOrDefault("message", "创建支付订单失败"));
        }
    }

    /**
     * 创建押金支付
     */
    @PostMapping("/deposit/create")
    @Operation(summary = "创建押金支付", description = "为共享物品预约创建押金支付")
    public Result<Map<String, Object>> createDepositPayment(
            @Parameter(description = "预约ID", required = true)
            @RequestParam Long bookingId,
            @Parameter(description = "押金金额", required = true)
            @RequestParam BigDecimal amount,
            @Parameter(description = "共享物品ID", required = true)
            @RequestParam Long shareId) {

        log.info("创建押金支付 - bookingId={}, amount={}, shareId={}", bookingId, amount, shareId);

        Map<String, Object> result = paymentService.createDepositPayment(bookingId, amount, shareId);
        if ((Boolean) result.getOrDefault("success", false)) {
            return Result.success("押金支付创建成功", result);
        } else {
            return Result.error((String) result.getOrDefault("message", "创建押金支付失败"));
        }
    }

    /**
     * 支付回调接口
     */
    @PostMapping("/callback")
    @Operation(summary = "支付回调", description = "接收支付平台的异步通知回调")
    public String handlePaymentCallback(@RequestBody Map<String, String> params) {
        log.info("接收支付回调 - params={}", params);

        boolean success = paymentService.handlePaymentCallback(params);
        if (success) {
            return "success";
        } else {
            return "fail";
        }
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/query/{outTradeNo}")
    @Operation(summary = "查询支付状态", description = "查询订单的支付状态")
    public Result<Map<String, Object>> queryPaymentStatus(
            @Parameter(description = "商户订单号", required = true)
            @PathVariable String outTradeNo) {

        log.info("查询支付状态 - outTradeNo={}", outTradeNo);

        String status = paymentService.queryPaymentStatus(outTradeNo);
        Map<String, Object> result = Map.of(
                "outTradeNo", outTradeNo,
                "status", status,
                "statusDesc", getStatusDesc(status)
        );

        return Result.success(result);
    }

    /**
     * 申请退款
     */
    @PostMapping("/refund")
    @Operation(summary = "申请退款", description = "申请订单退款")
    public Result<Map<String, Object>> refund(
            @Parameter(description = "商户订单号", required = true)
            @RequestParam String outTradeNo,
            @Parameter(description = "退款金额", required = true)
            @RequestParam BigDecimal refundAmount,
            @Parameter(description = "退款原因", required = true)
            @RequestParam String refundReason) {

        log.info("申请退款 - outTradeNo={}, amount={}, reason={}", outTradeNo, refundAmount, refundReason);

        Map<String, Object> result = paymentService.refund(outTradeNo, refundAmount, refundReason);
        if ((Boolean) result.getOrDefault("success", false)) {
            return Result.success("退款申请成功", result);
        } else {
            return Result.error((String) result.getOrDefault("message", "退款申请失败"));
        }
    }

    /**
     * 查询退款状态
     */
    @GetMapping("/refund/query/{refundNo}")
    @Operation(summary = "查询退款状态", description = "查询退款单的状态")
    public Result<Map<String, Object>> queryRefundStatus(
            @Parameter(description = "退款单号", required = true)
            @PathVariable String refundNo) {

        log.info("查询退款状态 - refundNo={}", refundNo);

        String status = paymentService.queryRefundStatus(refundNo);
        Map<String, Object> result = Map.of(
                "refundNo", refundNo,
                "status", status,
                "statusDesc", getRefundStatusDesc(status)
        );

        return Result.success(result);
    }

    /**
     * 关闭订单
     */
    @PostMapping("/close/{outTradeNo}")
    @Operation(summary = "关闭订单", description = "关闭未支付的订单")
    public Result<Boolean> closeOrder(
            @Parameter(description = "商户订单号", required = true)
            @PathVariable String outTradeNo) {

        log.info("关闭订单 - outTradeNo={}", outTradeNo);

        boolean success = paymentService.closeOrder(outTradeNo);
        if (success) {
            return Result.success("订单关闭成功", true);
        } else {
            return Result.error("订单关闭失败");
        }
    }

    /**
     * 获取支付状态描述
     */
    private String getStatusDesc(String status) {
        return switch (status) {
            case "SUCCESS" -> "支付成功";
            case "PROCESSING" -> "处理中";
            case "FAILED" -> "支付失败";
            case "UNKNOWN" -> "未知状态";
            default -> "未定义状态";
        };
    }

    /**
     * 获取退款状态描述
     */
    private String getRefundStatusDesc(String status) {
        return switch (status) {
            case "SUCCESS" -> "退款成功";
            case "PROCESSING" -> "退款处理中";
            case "FAILED" -> "退款失败";
            case "UNKNOWN" -> "未知状态";
            default -> "未定义状态";
        };
    }
}
