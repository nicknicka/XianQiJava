package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 商品状态枚举
 */
@Getter
public enum ProductStatus {
    OFFLINE(0, "下架"),
    ON_SALE(1, "在售"),
    SOLD(2, "已售"),
    RESERVED(3, "预订");

    private final Integer code;
    private final String desc;

    ProductStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProductStatus getByCode(Integer code) {
        for (ProductStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
