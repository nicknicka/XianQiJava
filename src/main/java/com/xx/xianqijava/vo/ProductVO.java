package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品详情 VO
 */
@Data
@Schema(description = "商品信息")
public class ProductVO {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "卖家昵称")
    private String sellerNickname;

    @Schema(description = "卖家头像")
    private String sellerAvatar;

    @Schema(description = "卖家信用分数")
    private Integer sellerCreditScore;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "成色：1-10")
    private Integer conditionLevel;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "交易地点")
    private String location;

    @Schema(description = "状态：0-下架 1-在售 2-已售 3-预订")
    private Integer status;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "收藏次数")
    private Integer favoriteCount;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "是否已收藏")
    private Boolean isFavorite;

    // ========== 秒杀相关字段 ==========

    @Schema(description = "秒杀价格（seckillPrice）")
    private BigDecimal seckillPrice;

    @Schema(description = "秒杀价格（flashPrice - 别名）")
    private BigDecimal flashPrice;

    @Schema(description = "秒杀库存")
    private Integer flashSaleStock;

    @Schema(description = "已售数量")
    private Integer flashSaleSold;

    @Schema(description = "已抢百分比（0-100）")
    private Integer soldPercent;

    @Schema(description = "折扣（如8表示8折）")
    private Integer discount;

    @Schema(description = "每人限购数量")
    private Integer limitPerUser;

    @Schema(description = "秒杀活动结束时间")
    private String endTime;

    @Schema(description = "是否在秒杀中")
    private Boolean isFlashSale;

    @Schema(description = "秒杀活动ID")
    private Long activityId;
}
