package com.xx.xianqijava.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 信用分详情视图对象
 */
@Data
public class CreditDetailVO {

    /**
     * 当前信用分
     */
    private Integer creditScore;

    /**
     * 信用等级
     */
    private String creditLevel;

    /**
     * 信用等级文本
     */
    private String creditLevelText;

    /**
     * 好评数
     */
    private Integer positiveCount;

    /**
     * 中评数
     */
    private Integer neutralCount;

    /**
     * 差评数
     */
    private Integer negativeCount;

    /**
     * 总评价数
     */
    private Integer totalEvaluations;

    /**
     * 好评率
     */
    private BigDecimal goodRate;

    /**
     * 完成交易数
     */
    private Integer transactionCount;

    /**
     * 信用分变化记录
     */
    private List<CreditRecordVO> scoreHistory;

    /**
     * 距离下一等级所需分数
     */
    private Integer nextLevelGap;

    /**
     * 今日已获得分数
     */
    private BigDecimal todayGain;

    /**
     * 今日剩余可获分数
     */
    private BigDecimal todayRemaining;

    /**
     * 信用分记录项
     */
    @Data
    public static class CreditRecordVO {
        /**
         * 变化分数
         */
        private BigDecimal scoreChange;

        /**
         * 变化后分数
         */
        private Integer scoreAfter;

        /**
         * 原因
         */
        private String reason;

        /**
         * 原因详情
         */
        private String reasonDetail;

        /**
         * 创建时间
         */
        private LocalDateTime createdAt;
    }
}
