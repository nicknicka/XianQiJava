package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据统计VO
 */
@Data
@Schema(description = "数据统计")
public class StatisticsVO {

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "今日新增用户")
    private Long todayNewUsers;

    @Schema(description = "活跃用户数（7天内登录）")
    private Long activeUsers;

    @Schema(description = "总商品数")
    private Long totalProducts;

    @Schema(description = "在售商品数")
    private Integer onSaleProducts;

    @Schema(description = "今日新增商品")
    private Long todayNewProducts;

    @Schema(description = "总订单数")
    private Long totalOrders;

    @Schema(description = "今日新增订单")
    private Long todayNewOrders;

    @Schema(description = "待处理订单数")
    private Long pendingOrders;

    @Schema(description = "已完成订单数")
    private Long completedOrders;

    @Schema(description = "总交易金额")
    private java.math.BigDecimal totalAmount;

    @Schema(description = "今日交易金额")
    private java.math.BigDecimal todayAmount;

    @Schema(description = "本月交易金额")
    private java.math.BigDecimal monthAmount;

    @Schema(description = "待审核商品数")
    private Long pendingProducts;

    @Schema(description = "待审核实名认证数")
    private Long pendingVerifications;

    @Schema(description = "系统通知数")
    private Long systemNotifications;

    @Schema(description = "用户反馈数")
    private Long userFeedbacks;

    @Schema(description = "举报处理数")
    private Long pendingReports;
}
