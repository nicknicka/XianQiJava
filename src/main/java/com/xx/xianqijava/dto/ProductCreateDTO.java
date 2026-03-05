package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建商品 DTO
 */
@Data
@Schema(description = "创建商品请求")
public class ProductCreateDTO {

    @Schema(description = "商品标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 50, message = "商品标题长度不能超过50个字符")
    private String title;

    @Schema(description = "商品描述")
    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    private Integer categoryId;

    @Schema(description = "价格（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格不能小于0.01元")
    private BigDecimal price;

    @Schema(description = "原价（元）")
    private BigDecimal originalPrice;

    @Schema(description = "成色（字符串格式）：new, almost_new, lightly_used, obviously_used, has_flaws", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "成色不能为空")
    @Pattern(regexp = "new|almost_new|lightly_used|obviously_used|has_flaws", message = "成色值无效")
    private String condition;

    @Schema(description = "成色等级（内部使用）：1-10，10为全新")
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

    @Schema(description = "是否支持邮寄")
    private Boolean canDelivery;

    @Schema(description = "是否包邮")
    private Boolean freeShipping;

    // ========== 秒杀配置 ==========

    @Schema(description = "是否参与秒杀")
    private Boolean isFlashSale;

    @Schema(description = "秒杀场次ID")
    private Long sessionId;

    @Schema(description = "秒杀价格")
    @DecimalMin(value = "0.01", message = "秒杀价格不能小于0.01元")
    private BigDecimal flashPrice;

    @Schema(description = "秒杀库存")
    @Min(value = 1, message = "秒杀库存不能小于1")
    private Integer flashSaleStock;

    @Schema(description = "每人限购数量")
    @Min(value = 1, message = "限购数量不能小于1")
    private Integer limitPerUser;

    @Schema(description = "重复类型：0-不重复（仅一次），1-每日重复")
    private Integer repeatType;

    @Schema(description = "参与秒杀的日期（repeatType=0时必填）")
    private LocalDate saleDate;
}
