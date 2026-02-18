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
 * 订单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`order`")
@Schema(description = "订单")
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "买家ID")
    private Long buyerId;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "共享物品ID")
    private Long shareId;

    @Schema(description = "类型：1-购买，2-共享")
    private Integer type;

    @Schema(description = "交易金额")
    private BigDecimal amount;

    @Schema(description = "状态：0-待确认，1-进行中，2-已完成，3-已取消，4-退款中")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;
}
