package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 押金记录VO
 */
@Data
@Schema(description = "押金记录详情")
public class DepositRecordVO {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "共享物品标题")
    private String shareItemTitle;

    @Schema(description = "支付用户ID")
    private Long userId;

    @Schema(description = "支付用户昵称")
    private String userNickname;

    @Schema(description = "押金金额")
    private BigDecimal amount;

    @Schema(description = "支付方式：1-余额，2-支付宝，3-微信")
    private Integer paymentMethod;

    @Schema(description = "支付方式描述")
    private String paymentMethodDesc;

    @Schema(description = "交易流水号")
    private String transactionNo;

    @Schema(description = "押金状态：0-待支付，1-已支付，2-已退还，3-已扣除")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "退还时间")
    private String refundTime;

    @Schema(description = "退还备注")
    private String refundRemark;

    @Schema(description = "扣除原因")
    private String deductReason;

    @Schema(description = "创建时间")
    private String createTime;
}
