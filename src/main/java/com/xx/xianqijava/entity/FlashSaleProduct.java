package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 秒杀商品表（已合并 Ext 表功能）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flash_sale_product")
@Schema(description = "秒杀商品")
public class FlashSaleProduct extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "秒杀商品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "场次ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;

    @Schema(description = "商品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;

    @Schema(description = "秒杀价格")
    private BigDecimal flashPrice;

    @Schema(description = "秒杀库存数量")
    private Integer stockCount;

    @Schema(description = "已售数量")
    private Integer soldCount;

    @Schema(description = "每人限购数量")
    private Integer limitPerUser;

    @Schema(description = "重复类型：0-不重复（卖完下架），1-每日重复")
    private Integer repeatType;

    @Schema(description = "参与秒杀的日期（NULL表示每日重复，有值表示指定日期）")
    private LocalDate saleDate;

    @Schema(description = "库存状态：0-在售，1-已售罄，2-手动下架")
    private Integer stockStatus;

    @Schema(description = "排序权重")
    private Integer sortOrder;
}