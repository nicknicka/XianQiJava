package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
public enum RefundStatus {
    PENDING(0, "待审核"),
    APPROVED(1, "已同意"),
    REJECTED(2, "已拒绝"),
    RETURNING(3, "退货中"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String desc;

    RefundStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RefundStatus getByCode(Integer code) {
        for (RefundStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
