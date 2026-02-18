package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
@Schema(description = "商品")
public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "成色：1-10（10全新）")
    private Integer conditionLevel;

    @Schema(description = "封面图片ID")
    private Long coverImageId;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "交易地点")
    private String location;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "状态：0-下架，1-在售，2-已售，3-预订")
    private Integer status;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "收藏次数")
    private Integer favoriteCount;
}
