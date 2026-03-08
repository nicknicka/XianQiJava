package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {
    PENDING(0, "待支付"),
    PENDING_CONFIRM(1, "待确认"),
    PAID(2, "已支付"),
    SHIPPED(3, "已发货"),
    DELIVERED(4, "已送达"),
    IN_PROGRESS(5, "进行中"),
    COMPLETED(6, "已完成"),
    CANCELLED(7, "已取消"),
    REFUNDING(8, "退款中"),
    REFUNDED(9, "已退款");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatus getByCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
