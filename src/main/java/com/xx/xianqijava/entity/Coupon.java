package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon")
@Schema(description = "优惠券")
public class Coupon extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "优惠券ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long couponId;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "优惠券描述")
    private String description;

    @Schema(description = "优惠券类型：1-满减券，2-折扣券，3-免邮券")
    private Integer type;

    @Schema(description = "门槛金额（满多少可用）")
    private BigDecimal minAmount;

    @Schema(description = "优惠金额/折扣值")
    private BigDecimal discountValue;

    @Schema(description = "最大优惠金额（折扣券用）")
    private BigDecimal maxDiscount;

    @Schema(description = "总发行数量")
    private Integer totalCount;

    @Schema(description = "已领取数量")
    private Integer receivedCount;

    @Schema(description = "已使用数量")
    private Integer usedCount;

    @Schema(description = "每人限领数量")
    private Integer limitPerUser;

    @Schema(description = "使用范围：1-全场，2-指定分类，3-指定商品")
    private Integer scope;

    @Schema(description = "适用分类ID（JSON数组）")
    private String categoryIds;

    @Schema(description = "适用商品ID（JSON数组）")
    private String productIds;

    @Schema(description = "有效开始时间")
    private LocalDateTime validFrom;

    @Schema(description = "有效结束时间")
    private LocalDateTime validTo;

    @Schema(description = "优惠券图片")
    private String imageUrl;

    @Schema(description = "状态：0-草稿，1-进行中，2-已结束，3-已作废")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;
}
