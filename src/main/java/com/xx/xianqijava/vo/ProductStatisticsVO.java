package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品统计VO
 */
@Data
@Schema(description = "商品统计")
public class ProductStatisticsVO {

    @Schema(description = "总商品数")
    private Long totalProducts;

    @Schema(description = "在售商品数")
    private Long onSaleProducts;

    @Schema(description = "已售商品数")
    private Long soldProducts;

    @Schema(description = "下架商品数")
    private Long offlineProducts;

    @Schema(description = "今日新增商品")
    private Long todayNewProducts;

    @Schema(description = "本周新增商品")
    private Long weekNewProducts;

    @Schema(description = "本月新增商品")
    private Long monthNewProducts;

    @Schema(description = "待审核商品数")
    private Long pendingProducts;

    @Schema(description = "审核通过商品数")
    private Long approvedProducts;

    @Schema(description = "审核拒绝商品数")
    private Long rejectedProducts;

    @Schema(description = "商品发布趋势（最近30天）")
    private java.util.List<TrendDataVO> publishTrend;

    @Schema(description = "按分类统计")
    private java.util.List<CategoryStatisticsVO> categoryStatistics;
}
