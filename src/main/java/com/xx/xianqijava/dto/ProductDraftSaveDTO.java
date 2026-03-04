package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品草稿保存 DTO
 * 特点：所有字段都是可选的，支持部分保存
 */
@Data
@Schema(description = "商品草稿保存请求")
public class ProductDraftSaveDTO {

    @Schema(description = "草稿ID（更新时传入）")
    private Long draftId;

    @Schema(description = "商品标题")
    @Size(max = 50, message = "商品标题长度不能超过50个字符")
    private String title;

    @Schema(description = "商品描述")
    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "价格（元）")
    @DecimalMin(value = "0.01", message = "价格不能小于0.01元")
    private BigDecimal price;

    @Schema(description = "原价（元）")
    private BigDecimal originalPrice;

    @Schema(description = "成色：1-10，10为全新")
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

    @Schema(description = "是否支持邮寄")
    private Boolean canDelivery;

    @Schema(description = "是否包邮")
    private Boolean freeShipping;

    // 秒杀配置（草稿也需要保存）
    @Schema(description = "是否参与秒杀")
    private Boolean isFlashSale;

    @Schema(description = "秒杀场次ID")
    private Long sessionId;

    @Schema(description = "秒杀价格")
    private BigDecimal flashPrice;

    @Schema(description = "秒杀库存")
    private Integer flashSaleStock;

    @Schema(description = "每人限购数量")
    private Integer limitPerUser;

    @Schema(description = "重复类型：0-不重复（仅一次），1-每日重复")
    private Integer repeatType;

    @Schema(description = "参与秒杀的日期（repeatType=0时必填）")
    private LocalDate saleDate;
}
