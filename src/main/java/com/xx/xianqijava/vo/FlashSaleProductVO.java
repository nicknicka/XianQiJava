package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀商品 VO（优化版）
 */
@Data
@Schema(description = "秒杀商品信息")
public class FlashSaleProductVO {

    // 商品基础信息
    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品图片")
    private String image;

    @Schema(description = "商品封面图")
    private String coverImage;

    @Schema(description = "商品图片列表")
    private String images;

    // 价格信息
    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "正常价格")
    private BigDecimal price;

    @Schema(description = "折扣（如8表示8折）")
    private Integer discount;

    // 库存信息
    @Schema(description = "已抢百分比")
    private Integer soldPercent;

    @Schema(description = "秒杀库存")
    private Integer stock;

    @Schema(description = "已售数量")
    private Integer soldCount;

    // 状态信息
    @Schema(description = "状态：ongoing-进行中，upcoming-即将开始，ended-已结束")
    private String status;

    @Schema(description = "结束时间")
    private String endTime;

    // 商品详细信息
    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "成色")
    private String condition;

    @Schema(description = "位置")
    private String location;

    // 卖家信息
    @Schema(description = "卖家ID")
    private Long userId;

    @Schema(description = "卖家昵称")
    private String userName;

    @Schema(description = "卖家头像")
    private String userAvatar;

    @Schema(description = "信用等级")
    private String creditLevel;

    // 统计信息
    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "收藏次数")
    private Integer favoriteCount;

    // 限制信息
    @Schema(description = "每人限购数量")
    private Integer limitPerUser;
}
