package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品关联表实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("flash_sale_product_relation")
@Schema(description = "秒杀商品关联")
public class FlashSaleProductRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "场次ID")
    private Long sessionId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "活动专属秒杀价")
    private BigDecimal flashPrice;

    @Schema(description = "活动专属库存")
    private Integer stockCount;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
