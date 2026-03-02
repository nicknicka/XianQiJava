package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户优惠券表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coupon")
@Schema(description = "用户优惠券")
public class UserCoupon extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户优惠券ID")
    private Long userCouponId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "优惠券状态：1-未使用，2-已使用，3-已过期")
    private Integer status;

    @Schema(description = "使用时间")
    private LocalDateTime usedTime;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
}
