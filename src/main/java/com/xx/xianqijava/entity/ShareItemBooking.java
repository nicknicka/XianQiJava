package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 共享物品预约借用表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("share_item_booking")
@Schema(description = "共享物品预约借用")
public class ShareItemBooking extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "物品所有者ID")
    private Long ownerId;

    @Schema(description = "借用者ID")
    private Long borrowerId;

    @Schema(description = "预约借用开始日期")
    private LocalDate startDate;

    @Schema(description = "预约借用结束日期")
    private LocalDate endDate;

    @Schema(description = "借用天数")
    private Integer days;

    @Schema(description = "总租金（日租金×天数）")
    private BigDecimal totalRent;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "总金额（租金+押金）")
    private BigDecimal totalAmount;

    @Schema(description = "借用说明/备注")
    private String remark;

    @Schema(description = "预约状态：0-待审批，1-已批准，2-已拒绝，3-已取消，4-借用中，5-已完成")
    private Integer status;

    @Schema(description = "审批时间")
    private java.time.LocalDateTime approveTime;

    @Schema(description = "审批备注")
    private String approveRemark;

    @Schema(description = "实际归还时间")
    private java.time.LocalDateTime returnTime;

    @Schema(description = "归还备注")
    private String returnRemark;

    @Schema(description = "是否已归还押金：0-未退还，1-已退还")
    private Integer depositReturned;

    @Schema(description = "押金退还时间")
    private java.time.LocalDateTime depositReturnTime;
}
