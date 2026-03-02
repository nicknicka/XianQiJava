package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券视图对象
 */
@Data
@Schema(description = "用户优惠券")
public class UserCouponVO {

    @Schema(description = "用户优惠券ID")
    private Long userCouponId;

    @Schema(description = "优惠券ID")
    private Long couponId;

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

    @Schema(description = "折扣百分比")
    private String discountPercent;

    @Schema(description = "优惠券图片")
    private String imageUrl;

    @Schema(description = "优惠券状态：1-未使用，2-已使用，3-已过期")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "使用时间")
    private LocalDateTime usedTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "是否即将过期（3天内）")
    private Boolean expiringSoon;
}
