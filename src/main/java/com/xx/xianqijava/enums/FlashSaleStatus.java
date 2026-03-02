package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 秒杀活动状态枚举
 */
@Getter
public enum FlashSaleStatus {
    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    ENDED(2, "已结束"),
    CANCELLED(3, "已取消");

    private final Integer code;
    private final String desc;

    FlashSaleStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FlashSaleStatus getByCode(Integer code) {
        for (FlashSaleStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
