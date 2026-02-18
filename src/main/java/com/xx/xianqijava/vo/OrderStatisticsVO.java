package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计VO
 */
@Data
@Schema(description = "订单统计")
public class OrderStatisticsVO {

    @Schema(description = "总订单数")
    private Long totalOrders;

    @Schema(description = "待确认订单数")
    private Long pendingOrders;

    @Schema(description = "进行中订单数")
    private Long inProgressOrders;

    @Schema(description = "已完成订单数")
    private Long completedOrders;

    @Schema(description = "已取消订单数")
    private Long cancelledOrders;

    @Schema(description = "退款中订单数")
    private Long refundingOrders;

    @Schema(description = "今日新增订单")
    private Long todayNewOrders;

    @Schema(description = "本周新增订单")
    private Long weekNewOrders;

    @Schema(description = "本月新增订单")
    private Long monthNewOrders;

    @Schema(description = "总交易金额")
    private BigDecimal totalAmount;

    @Schema(description = "今日交易金额")
    private BigDecimal todayAmount;

    @Schema(description = "本周交易金额")
    private BigDecimal weekAmount;

    @Schema(description = "本月交易金额")
    private BigDecimal monthAmount;

    @Schema(description = "订单趋势（最近30天）")
    private java.util.List<TrendDataVO> orderTrend;

    @Schema(description = "交易金额趋势（最近30天）")
    private java.util.List<TrendDataVO> amountTrend;
}
