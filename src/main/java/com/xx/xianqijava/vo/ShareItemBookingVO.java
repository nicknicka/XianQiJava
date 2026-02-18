package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约借用VO
 */
@Data
@Schema(description = "预约借用详情")
public class ShareItemBookingVO {

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "物品标题")
    private String shareItemTitle;

    @Schema(description = "物品封面图")
    private String coverImageUrl;

    @Schema(description = "物品所有者ID")
    private Long ownerId;

    @Schema(description = "所有者昵称")
    private String ownerNickname;

    @Schema(description = "所有者头像")
    private String ownerAvatar;

    @Schema(description = "借用者ID")
    private Long borrowerId;

    @Schema(description = "借用者昵称")
    private String borrowerNickname;

    @Schema(description = "借用者头像")
    private String borrowerAvatar;

    @Schema(description = "借用开始日期")
    private LocalDate startDate;

    @Schema(description = "借用结束日期")
    private LocalDate endDate;

    @Schema(description = "借用天数")
    private Integer days;

    @Schema(description = "日租金")
    private BigDecimal dailyRent;

    @Schema(description = "总租金")
    private BigDecimal totalRent;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "借用说明/备注")
    private String remark;

    @Schema(description = "预约状态：0-待审批，1-已批准，2-已拒绝，3-已取消，4-借用中，5-已完成")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "审批备注")
    private String approveRemark;

    @Schema(description = "归还备注")
    private String returnRemark;

    @Schema(description = "是否已退还押金")
    private Integer depositReturned;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "审批时间")
    private String approveTime;

    @Schema(description = "归还时间")
    private String returnTime;

    @Schema(description = "押金退还时间")
    private String depositReturnTime;
}
