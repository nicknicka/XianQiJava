package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品统计表
 */
@Data
@TableName("product_statistics")
@Schema(description = "商品统计")
public class ProductStatistics {

    @TableId(type = IdType.INPUT)
    @Schema(description = "商品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;

    @Schema(description = "浏览次数")
    @TableField("view_count")
    private Integer viewCount;

    @Schema(description = "收藏次数")
    @TableField("favorite_count")
    private Integer favoriteCount;

    @Schema(description = "图片数量")
    @TableField("image_count")
    private Integer imageCount;

    @Schema(description = "封面图片ID")
    @TableField("cover_image_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long coverImageId;

    @Schema(description = "封面图片URL")
    @TableField("cover_image_url")
    private String coverImageUrl;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
