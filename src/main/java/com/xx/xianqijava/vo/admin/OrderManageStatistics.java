package com.xx.xianqijava.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单管理统计信息VO - 管理端
 */
@Data
@Schema(description = "订单管理统计信息VO")
public class OrderManageStatistics {

    @Schema(description = "总订单数")
    private Long totalOrders;

    @Schema(description = "待确认订单数")
    private Long pendingConfirmCount;

    @Schema(description = "进行中订单数")
    private Long inProgressCount;

    @Schema(description = "已完成订单数")
    private Long completedCount;

    @Schema(description = "已取消订单数")
    private Long cancelledCount;

    @Schema(description = "退款中订单数")
    private Long refundingCount;

    @Schema(description = "今日新增订单数")
    private Long todayNewOrders;

    @Schema(description = "本周新增订单数")
    private Long weekNewOrders;

    @Schema(description = "本月新增订单数")
    private Long monthNewOrders;

    @Schema(description = "总交易金额")
    private BigDecimal totalAmount;

    @Schema(description = "今日交易金额")
    private BigDecimal todayAmount;

    @Schema(description = "本周交易金额")
    private BigDecimal weekAmount;

    @Schema(description = "本月交易金额")
    private BigDecimal monthAmount;
}
