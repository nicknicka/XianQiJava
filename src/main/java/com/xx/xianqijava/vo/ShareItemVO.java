package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 共享物品VO
 */
@Data
@Schema(description = "共享物品信息")
public class ShareItemVO {

    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "所有者ID")
    private Long ownerId;

    @Schema(description = "所有者昵称")
    private String ownerNickname;

    @Schema(description = "所有者头像")
    private String ownerAvatar;

    @Schema(description = "所有者信用分")
    private Integer ownerCreditScore;

    @Schema(description = "物品标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "日租金")
    private BigDecimal dailyRent;

    @Schema(description = "封面图片URL")
    private String coverImageUrl;

    @Schema(description = "图片URL列表")
    private java.util.List<String> imageUrls;

    @Schema(description = "可借用时间段")
    private String availableTimes;

    @Schema(description = "状态：0-下架，1-可借用，2-借用中")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "是否可以借用")
    private Boolean canBorrow;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
