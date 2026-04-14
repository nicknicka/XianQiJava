package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建轮播图DTO - 管理端
 */
@Data
@Schema(description = "创建轮播图DTO")
public class BannerCreateDTO {

    @NotBlank(message = "轮播图标题不能为空")
    @Schema(description = "轮播图标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "图片URL不能为空")
    @Schema(description = "图片URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String imageThumbnailUrl;

    @NotNull(message = "链接类型不能为空")
    @Schema(description = "链接类型：1-无，2-外链，3-商品详情，4-功能页面", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer linkType;

    @Schema(description = "跳转URL（link_type=2时必填）")
    private String linkUrl;

    @Schema(description = "关联商品ID（link_type=3时必填）")
    private String linkProductId;

    @Schema(description = "功能页面路径（link_type=4时必填）")
    private String linkPagePath;

    @Schema(description = "排序", example = "0")
    private Integer sortOrder = 0;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用，1-启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "开始展示时间")
    private java.time.LocalDateTime startTime;

    @Schema(description = "结束展示时间")
    private java.time.LocalDateTime endTime;
}
