package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀商品 VO
 */
@Data
@Schema(description = "秒杀商品信息")
public class FlashSaleProductVO {

    @Schema(description = "秒杀商品ID")
    private Long flashSaleId;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "商品封面图")
    private String productCoverImage;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "秒杀价格")
    private BigDecimal flashPrice;

    @Schema(description = "折扣")
    private String discount;

    @Schema(description = "库存数量")
    private Integer stockCount;

    @Schema(description = "已售数量")
    private Integer soldCount;

    @Schema(description = "活动结束时间")
    private String activityEndTime;

    @Schema(description = "排序权重")
    private Integer sortOrder;
}
