package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 共享物品草稿 VO
 */
@Data
@Schema(description = "共享物品草稿信息")
public class ShareItemDraftVO {

    @Schema(description = "草稿ID")
    private Long draftId;

    @Schema(description = "共享物品ID（同draftId）")
    private Long shareId;

    @Schema(description = "物品标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "日租金")
    private BigDecimal dailyRent;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "图片URL列表")
    private String[] images;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "可借用时间段（JSON格式）")
    private String availableTimes;

    @Schema(description = "保存时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "完成度（0-100）")
    private Integer completion;

    @Schema(description = "缺少的必填字段列表")
    private String[] missingFields;
}
