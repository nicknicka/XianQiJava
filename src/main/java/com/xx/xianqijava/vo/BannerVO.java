package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图视图对象
 */
@Data
@Schema(description = "轮播图")
public class BannerVO {

    @Schema(description = "轮播图ID")
    private Long bannerId;

    @Schema(description = "轮播图标题")
    private String title;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "缩略图URL")
    private String imageThumbnailUrl;

    @Schema(description = "链接类型：1-无，2-外链，3-商品详情，4-功能页面")
    private Integer linkType;

    @Schema(description = "跳转URL")
    private String linkUrl;

    @Schema(description = "关联商品ID")
    private Long linkProductId;

    @Schema(description = "功能页面路径")
    private String linkPagePath;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "开始展示时间")
    private LocalDateTime startTime;

    @Schema(description = "结束展示时间")
    private LocalDateTime endTime;
}
