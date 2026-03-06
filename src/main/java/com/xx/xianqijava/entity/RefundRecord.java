package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund_record")
@Schema(description = "退款记录")
public class RefundRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "退款记录ID")
    private Long refundId;

    @Schema(description = "退款单号")
    private String refundNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因")
    private String refundReason;

    @Schema(description = "退款类型：1-仅退款 2-退货退款")
    private Integer refundType;

    @Schema(description = "状态：0-待审核 1-已同意 2-已拒绝 3-退货中 4-已完成 5-已取消")
    private Integer status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "物流公司")
    private String logisticsCompany;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "发货时间")
    private LocalDateTime logisticsTime;

    @Schema(description = "退款凭证图片，JSON数组")
    private String evidenceImages;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;
}
