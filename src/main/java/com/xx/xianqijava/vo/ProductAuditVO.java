package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商品审核VO
 */
@Data
@Schema(description = "商品审核信息")
public class ProductAuditVO {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "价格")
    private java.math.BigDecimal price;

    @Schema(description = "成色")
    private Integer conditionLevel;

    @Schema(description = "成色描述")
    private String conditionDesc;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "卖家昵称")
    private String sellerNickname;

    @Schema(description = "卖家手机号")
    private String sellerPhone;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @Schema(description = "审核状态描述")
    private String auditStatusDesc;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "审核时间")
    private String auditTime;

    @Schema(description = "审核人ID")
    private Long auditorId;

    @Schema(description = "商品图片列表")
    private List<String> images;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "商品状态：0-下架，1-在售，2-已售，3-预订")
    private Integer status;

    @Schema(description = "商品状态描述")
    private String statusDesc;
}
