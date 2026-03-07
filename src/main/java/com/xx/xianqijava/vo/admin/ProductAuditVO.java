package com.xx.xianqijava.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品审核VO - 管理端
 */
@Data
@Schema(description = "商品审核VO")
public class ProductAuditVO {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "成色：1-全新，2-九成新，3-八成新，4-七成新，5-六成新及以下")
    private Integer condition;

    @Schema(description = "商品状态：0-下架，1-在售，2-已售")
    private Integer status;

    @Schema(description = "审核状态：0-待审核，1-已通过，2-已拒绝")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    @Schema(description = "审核人ID")
    private Long auditorId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "卖家昵称")
    private String sellerNickname;

    @Schema(description = "卖家手机号")
    private String sellerPhone;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "收藏次数")
    private Integer favoriteCount;

    @Schema(description = "审核状态描述")
    public String getAuditStatusDesc() {
        switch (auditStatus) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
            default:
                return "未知";
        }
    }

    @Schema(description = "商品状态描述")
    public String getStatusDesc() {
        switch (status) {
            case 0:
                return "下架";
            case 1:
                return "在售";
            case 2:
                return "已售";
            default:
                return "未知";
        }
    }

    @Schema(description = "成色描述")
    public String getConditionDesc() {
        switch (condition) {
            case 1:
                return "全新";
            case 2:
                return "九成新";
            case 3:
                return "八成新";
            case 4:
                return "七成新";
            case 5:
                return "六成新及以下";
            default:
                return "未知";
        }
    }
}
