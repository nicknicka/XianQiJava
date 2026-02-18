package com.xx.xianqijava.enums;

import lombok.Getter;

/**
 * 会话类型枚举
 */
@Getter
public enum ConversationType {
    SINGLE(1, "单聊"),
    GROUP(2, "群聊");

    private final Integer code;
    private final String desc;

    ConversationType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConversationType getByCode(Integer code) {
        for (ConversationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
