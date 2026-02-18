package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品图片视图对象
 */
@Data
@Schema(description = "商品图片信息")
public class ProductImageVO {

    @Schema(description = "图片ID")
    private Long imageId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String imageThumbnailUrl;

    @Schema(description = "中等尺寸图片URL")
    private String imageMediumUrl;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "图片宽度")
    private Integer width;

    @Schema(description = "图片高度")
    private Integer height;

    @Schema(description = "是否为封面：0-否，1-是")
    private Integer isCover;

    @Schema(description = "创建时间")
    private String createTime;
}
