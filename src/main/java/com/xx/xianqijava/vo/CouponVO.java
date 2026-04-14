package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券视图对象
 */
@Data
@Schema(description = "优惠券")
public class CouponVO {

    @Schema(description = "优惠券ID")
    private String couponId;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "优惠券描述")
    private String description;

    @Schema(description = "优惠券类型：1-满减券，2-折扣券，3-免邮券")
    private Integer type;

    @Schema(description = "优惠券类型描述")
    private String typeDesc;

    @Schema(description = "门槛金额")
    private BigDecimal minAmount;

    @Schema(description = "优惠金额/折扣值")
    private BigDecimal discountValue;

    @Schema(description = "最大优惠金额")
    private BigDecimal maxDiscount;

    @Schema(description = "折扣百分比（如8.5折）")
    private String discountPercent;

    @Schema(description = "总发行数量")
    private Integer totalCount;

    @Schema(description = "已领取数量")
    private Integer receivedCount;

    @Schema(description = "已使用数量")
    private Integer usedCount;

    @Schema(description = "剩余数量")
    private Integer remainingCount;

    @Schema(description = "每人限领数量")
    private Integer limitPerUser;

    @Schema(description = "用户已领取数量")
    private Integer userReceivedCount;

    @Schema(description = "是否可领取")
    private Boolean canReceive;

    @Schema(description = "不可领取原因")
    private String cannotReceiveReason;

    @Schema(description = "优惠券图片")
    private String imageUrl;

    @Schema(description = "有效开始时间")
    private LocalDateTime validFrom;

    @Schema(description = "有效结束时间")
    private LocalDateTime validTo;

    @Schema(description = "状态：0-草稿，1-进行中，2-已结束，3-已作废")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;
}
