package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 更新商品DTO
 */
@Data
@Schema(description = "更新商品请求")
public class ProductUpdateDTO {

    @NotNull(message = "商品标题不能为空")
    @Size(min = 1, max = 100, message = "商品标题长度必须在1-100之间")
    @Schema(description = "商品标题")
    private String title;

    @NotNull(message = "商品描述不能为空")
    @Size(min = 1, max = 2000, message = "商品描述长度必须在1-2000之间")
    @Schema(description = "商品描述")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    @Schema(description = "商品价格")
    private BigDecimal price;

    @NotNull(message = "成色不能为空")
    @Schema(description = "成色：1-全新，2-几乎全新，3-轻微使用，4-明显使用")
    private Integer condition;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "交易地点")
    private String location;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "商品图片URL列表")
    private List<String> imageUrls;
}
