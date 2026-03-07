package com.xx.xianqijava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 信用分变化结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditChangeDTO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 变化分数
     */
    private BigDecimal scoreChange;

    /**
     * 变化前分数
     */
    private Integer scoreBefore;

    /**
     * 变化后分数
     */
    private Integer scoreAfter;

    /**
     * 变化原因
     */
    private String reason;

    /**
     * 原因详情
     */
    private String reasonDetail;

    /**
     * 是否达到上限
     */
    private Boolean reachedLimit;

    /**
     * 消息提示
     */
    private String message;

    /**
     * 创建无变化的结果
     */
    public static CreditChangeDTO noop(String message) {
        return CreditChangeDTO.builder()
                .success(false)
                .scoreChange(BigDecimal.ZERO)
                .message(message)
                .reachedLimit(true)
                .build();
    }

    /**
     * 创建成功的结果
     */
    public static CreditChangeDTO success(BigDecimal scoreChange, String reason) {
        return CreditChangeDTO.builder()
                .success(true)
                .scoreChange(scoreChange)
                .reason(reason)
                .reachedLimit(false)
                .build();
    }

    /**
     * 创建失败的结果
     */
    public static CreditChangeDTO fail(String message) {
        return CreditChangeDTO.builder()
                .success(false)
                .scoreChange(BigDecimal.ZERO)
                .message(message)
                .build();
    }
}
