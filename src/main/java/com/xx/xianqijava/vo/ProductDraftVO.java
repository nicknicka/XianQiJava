package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品草稿 VO
 */
@Data
@Schema(description = "商品草稿信息")
public class ProductDraftVO {

    @Schema(description = "草稿ID")
    private String draftId;

    @Schema(description = "商品ID（同draftId）")
    private String productId;

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

    @Schema(description = "成色等级：1-10")
    private Integer conditionLevel;

    @Schema(description = "成色字符串（new, almost_new, lightly_used, obviously_used, has_flaws）")
    private String condition;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "图片URL列表")
    private String[] images;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "交易地点")
    private String location;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "是否支持邮寄")
    private Boolean canDelivery;

    @Schema(description = "是否包邮")
    private Boolean freeShipping;

    @Schema(description = "是否参与秒杀")
    private Boolean isFlashSale;

    @Schema(description = "秒杀价格")
    private BigDecimal flashPrice;

    @Schema(description = "秒杀库存")
    private Integer flashSaleStock;

    @Schema(description = "保存时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "完成度（0-100）")
    private Integer completion;

    @Schema(description = "缺少的必填字段列表")
    private String[] missingFields;
}
