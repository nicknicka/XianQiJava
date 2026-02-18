package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 订单类型枚举
 */
@Getter
public enum OrderType {
    PURCHASE(1, "购买"),
    SHARE(2, "共享");

    private final Integer code;
    private final String desc;

    OrderType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderType getByCode(Integer code) {
        for (OrderType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
