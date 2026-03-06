package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 退款类型枚举
 */
@Getter
public enum RefundType {
    REFUND_ONLY(1, "仅退款"),
    RETURN_AND_REFUND(2, "退货退款");

    private final Integer code;
    private final String desc;

    RefundType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RefundType getByCode(Integer code) {
        for (RefundType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
