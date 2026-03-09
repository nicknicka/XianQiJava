package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 * <p>
 * 状态流转：
 * 0(待确认) → 1(进行中) → 2(已完成)
 * ↓           ↓
 * 3(已取消)   4(退款中) → 3(已取消/退款)
 */
@Getter
public enum OrderStatus {
    /**
     * 待确认 - 订单创建后，等待卖家确认
     */
    PENDING_CONFIRM(0, "待确认"),

    /**
     * 进行中 - 订单已确认，交易进行中
     */
    IN_PROGRESS(1, "进行中"),

    /**
     * 已完成 - 交易完成
     */
    COMPLETED(2, "已完成"),

    /**
     * 已取消 - 订单已取消
     */
    CANCELLED(3, "已取消"),

    /**
     * 退款中 - 退款处理中
     */
    REFUNDING(4, "退款中");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 订单状态枚举，找不到返回 null
     */
    public static OrderStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态（已完成/已取消）
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * 判断是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING_CONFIRM || this == IN_PROGRESS;
    }

    /**
     * 判断是否可以确认
     */
    public boolean canConfirm() {
        return this == PENDING_CONFIRM;
    }

    /**
     * 判断是否可以完成
     */
    public boolean canComplete() {
        return this == IN_PROGRESS;
    }

    /**
     * 判断是否可以申请退款
     */
    public boolean canRefund() {
        return this == IN_PROGRESS;
    }
}
