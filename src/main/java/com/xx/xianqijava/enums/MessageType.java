package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
public enum MessageType {
    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    PRODUCT_CARD(3, "商品卡片"),
    ORDER_CARD(4, "订单卡片"),
    SYSTEM_NOTIFICATION(5, "系统通知"),
    QUOTE(6, "引用消息");

    private final Integer code;
    private final String desc;

    MessageType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MessageType getByCode(Integer code) {
        for (MessageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
