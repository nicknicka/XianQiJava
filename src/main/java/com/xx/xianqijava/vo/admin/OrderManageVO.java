package com.xx.xianqijava.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单管理VO - 管理端
 */
@Data
@Schema(description = "订单管理VO")
public class OrderManageVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品标题")
    private String productTitle;

    @Schema(description = "商品封面图")
    private String productImage;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态：0-待确认，1-进行中，2-已完成，3-已取消，4-退款中")
    private Integer status;

    @Schema(description = "买家ID")
    private Long buyerId;

    @Schema(description = "买家昵称")
    private String buyerNickname;

    @Schema(description = "买家手机号")
    private String buyerPhone;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "卖家昵称")
    private String sellerNickname;

    @Schema(description = "卖家手机号")
    private String sellerPhone;

    @Schema(description = "交易地点")
    private String tradeLocation;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间（确认时间）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    // 注意：以下字段在Order实体中不存在，需要从其他表获取或添加字段
    // 目前暂时注释掉，等待完善
    // @Schema(description = "确认时间")
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private LocalDateTime confirmTime;
    //
    // @Schema(description = "取消时间")
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private LocalDateTime cancelTime;
    //
    // @Schema(description = "退款申请时间")
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private LocalDateTime refundRequestTime;
    //
    // @Schema(description = "退款处理时间")
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private LocalDateTime refundProcessTime;
    //
    // @Schema(description = "退款状态：0-无退款，1-退款中，2-退款成功，3-退款拒绝")
    // private Integer refundStatus;
    //
    // @Schema(description = "退款备注")
    // private String refundRemark;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "订单状态描述")
    public String getStatusDesc() {
        switch (status) {
            case 0:
                return "待确认";
            case 1:
                return "进行中";
            case 2:
                return "已完成";
            case 3:
                return "已取消";
            case 4:
                return "退款中";
            default:
                return "未知";
        }
    }

    @Schema(description = "退款状态描述")
    public String getRefundStatusDesc() {
        switch (refundStatus) {
            case 0:
                return "无退款";
            case 1:
                return "退款中";
            case 2:
                return "退款成功";
            case 3:
                return "退款拒绝";
            default:
                return "未知";
        }
    }
}
