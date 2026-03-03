package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单扩展表实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("flash_sale_order_ext")
@Schema(description = "秒杀订单扩展")
public class FlashSaleOrderExt implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "秒杀活动ID")
    private Long activityId;

    @Schema(description = "秒杀场次ID")
    private Long sessionId;

    @Schema(description = "秒杀成交价")
    private BigDecimal flashPrice;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "抢购时间")
    private LocalDateTime seckillTime;

    @Schema(description = "客户端IP")
    private String clientIp;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
