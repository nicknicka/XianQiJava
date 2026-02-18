package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建商品图片DTO
 */
@Data
@Schema(description = "创建商品图片请求")
public class ProductImageCreateDTO {

    @NotBlank(message = "图片URL不能为空")
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
}
