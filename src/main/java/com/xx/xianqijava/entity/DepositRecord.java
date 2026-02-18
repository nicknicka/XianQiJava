package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 押金记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("deposit_record")
@Schema(description = "押金记录")
public class DepositRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "关联预约ID")
    private Long bookingId;

    @Schema(description = "关联共享物品ID")
    private Long shareId;

    @Schema(description = "支付用户ID")
    private Long userId;

    @Schema(description = "押金金额")
    private BigDecimal amount;

    @Schema(description = "支付方式：1-余额，2-支付宝，3-微信")
    private Integer paymentMethod;

    @Schema(description = "交易流水号")
    private String transactionNo;

    @Schema(description = "押金状态：0-待支付，1-已支付，2-已退还，3-已扣除")
    private Integer status;

    @Schema(description = "退还时间")
    private java.time.LocalDateTime refundTime;

    @Schema(description = "退还备注")
    private String refundRemark;

    @Schema(description = "扣除原因")
    private String deductReason;
}
