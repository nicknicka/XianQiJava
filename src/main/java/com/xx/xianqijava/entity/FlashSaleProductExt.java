package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品扩展表实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("flash_sale_product_ext")
@Schema(description = "秒杀商品扩展")
public class FlashSaleProductExt implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "秒杀价格")
    private BigDecimal flashPrice;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "秒杀库存数量")
    private Integer stockCount;

    @Schema(description = "已售数量")
    private Integer soldCount;

    @Schema(description = "每人限购数量")
    private Integer limitPerUser;

    @Schema(description = "秒杀开始时间")
    private LocalDateTime startTime;

    @Schema(description = "秒杀结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：0-未开始 1-进行中 2-已结束 3-已取消")
    private Integer status;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
