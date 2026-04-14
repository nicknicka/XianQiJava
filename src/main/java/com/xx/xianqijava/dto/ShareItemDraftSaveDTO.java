package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 共享物品草稿保存 DTO
 * 特点：所有字段都是可选的，支持部分保存
 */
@Data
@Schema(description = "共享物品草稿保存请求")
public class ShareItemDraftSaveDTO {

    @Schema(description = "草稿ID（更新时传入）")
    private String draftId;

    @Schema(description = "物品标题")
    @Size(max = 50, message = "物品标题长度不能超过50个字符")
    private String title;

    @Schema(description = "描述")
    @Size(max = 2000, message = "描述长度不能超过2000个字符")
    private String description;

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "押金")
    @DecimalMin(value = "0", message = "押金不能小于0")
    private BigDecimal deposit;

    @Schema(description = "日租金")
    @DecimalMin(value = "0", message = "日租金不能小于0")
    private BigDecimal dailyRent;

    @Schema(description = "图片URL列表（最多9张）")
    private List<String> imageUrls;

    @Schema(description = "可借用时间段（JSON格式）")
    private String availableTimes;
}
