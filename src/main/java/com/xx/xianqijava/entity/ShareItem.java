package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 共享物品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("share_item")
@Schema(description = "共享物品")
public class ShareItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "所有者ID")
    private Long ownerId;

    @Schema(description = "物品标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "日租金")
    private BigDecimal dailyRent;

    @Schema(description = "封面图片ID")
    private Long coverImageId;

    @Schema(description = "图片数量")
    private Integer imageCount;

    @Schema(description = "状态：0-下架，1-可借用，2-借用中，4-草稿")
    private Integer status;

    // 状态常量
    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_AVAILABLE = 1;
    public static final int STATUS_BORROWED = 2;
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

    @Schema(description = "可借用时间段（JSON）")
    private String availableTimes;
}
