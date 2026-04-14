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
import java.time.LocalDateTime;

/**
 * 商品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
@Schema(description = "商品")
public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "商品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;

    @Schema(description = "卖家ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sellerId;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "分类ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "成色：1-10（10全新）")
    private Integer conditionLevel;

    @Schema(description = "封面图片ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long coverImageId;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "交易地点")
    private String location;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "是否支持邮寄：0-仅自提，1-支持邮寄")
    private Boolean canDelivery;

    @Schema(description = "配送费（元），NULL或0表示包邮，>0表示具体运费")
    private BigDecimal deliveryFee;

    @Schema(description = "状态：0-下架，1-在售，2-已售，3-预订，4-草稿")
    private Integer status;

    // 状态常量
    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_ON_SALE = 1;
    public static final int STATUS_SOLD_OUT = 2;
    public static final int STATUS_RESERVED = 3;
    public static final int STATUS_DRAFT = 4;

    /**
     * 判断是否为草稿状态
     */
    public boolean isDraft() {
        return this.status != null && this.status == STATUS_DRAFT;
    }

    /**
     * 设置为草稿状态
     */
    public void setAsDraft() {
        this.status = STATUS_DRAFT;
    }

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核人ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditorId;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    // 注意：收藏次数不再维护缓存字段，改为实时查询 product_favorite 表统计
}
