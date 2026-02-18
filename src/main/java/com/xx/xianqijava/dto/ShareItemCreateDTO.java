package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建共享物品DTO
 */
@Data
@Schema(description = "创建共享物品请求")
public class ShareItemCreateDTO {

    @NotBlank(message = "物品标题不能为空")
    @Schema(description = "物品标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID")
    private Long categoryId;

    @NotNull(message = "押金不能为空")
    @Schema(description = "押金")
    private BigDecimal deposit;

    @NotNull(message = "日租金不能为空")
    @Schema(description = "日租金")
    private BigDecimal dailyRent;

    @Schema(description = "封面图片URL")
    private String coverImageUrl;

    @Schema(description = "图片URL列表")
    private java.util.List<String> imageUrls;

    @Schema(description = "可借用时间段（JSON格式）")
    private String availableTimes;
}
