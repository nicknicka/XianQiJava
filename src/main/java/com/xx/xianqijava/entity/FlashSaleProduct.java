package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 秒杀商品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flash_sale_product")
@Schema(description = "秒杀商品")
public class FlashSaleProduct extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "秒杀商品ID")
    private Long flashSaleId;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "秒杀价格")
    private BigDecimal flashPrice;

    @Schema(description = "秒杀库存数量")
    private Integer stockCount;

    @Schema(description = "已售数量")
    private Integer soldCount;

    @Schema(description = "排序权重")
    private Integer sortOrder;
}
