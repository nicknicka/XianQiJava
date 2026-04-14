package com.xx.xianqijava.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 共享物品管理VO - 管理端
 */
@Data
@Schema(description = "共享物品管理VO")
public class ShareItemManageVO {

    @Schema(description = "共享物品ID")
    private String shareId;

    @Schema(description = "物品标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "日租金")
    private BigDecimal dailyRent;

    @Schema(description = "封面图片ID")
    private String coverImageId;

    @Schema(description = "封面图片URL")
    private String coverImageUrl;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "状态：0-下架，1-可借用，2-借用中，4-草稿")
    private Integer status;

    @Schema(description = "可借用时间段（JSON）")
    private String availableTimes;

    @Schema(description = "所有者ID")
    private String ownerId;

    @Schema(description = "所有者昵称")
    private String ownerNickname;

    @Schema(description = "所有者手机号")
    private String ownerPhone;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "借用次数")
    private Integer borrowCount;

    @Schema(description = "当前借用次数")
    private Integer currentBorrowCount;

    @Schema(description = "状态描述")
    public String getStatusDesc() {
        switch (status) {
            case 0:
                return "下架";
            case 1:
                return "可借用";
            case 2:
                return "借用中";
            case 4:
                return "草稿";
            default:
                return "未知";
        }
    }
}
