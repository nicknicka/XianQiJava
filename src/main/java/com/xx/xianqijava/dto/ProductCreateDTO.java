package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建商品 DTO
 */
@Data
@Schema(description = "创建商品请求")
public class ProductCreateDTO {

    @Schema(description = "商品标题", required = true)
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 50, message = "商品标题长度不能超过50个字符")
    private String title;

    @Schema(description = "商品描述")
    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    @Schema(description = "分类ID", required = true)
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;

    @Schema(description = "价格（元）", required = true)
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格不能小于0.01元")
    private BigDecimal price;

    @Schema(description = "原价（元）")
    private BigDecimal originalPrice;

    @Schema(description = "成色：1-10，10为全新", required = true)
    @NotNull(message = "成色不能为空")
    @Min(value = 1, message = "成色范围1-10")
    @Max(value = 10, message = "成色范围1-10")
    private Integer conditionLevel;

    @Schema(description = "商品图片URL列表（最多9张）")
    private String[] imageUrls;

    @Schema(description = "交易地点")
    @Size(max = 200, message = "交易地点长度不能超过200个字符")
    private String location;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;
}
