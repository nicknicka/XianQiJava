package com.xx.xianqijava.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 信用分配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "credit.score")
public class CreditScoreConfig {

    /**
     * 初始分数
     */
    private Integer initialScore = 60;

    /**
     * 分数范围
     */
    private Integer minScore = 0;
    private Integer maxScore = 100;

    /**
     * 每日/周/月加分上限（防刷）
     */
    private Double dailyMaxGain = 3.0;
    private Double weeklyMaxGain = 15.0;
    private Double monthlyMaxGain = 50.0;

    /**
     * 单次交易加分上限
     */
    private Double singleTransactionMax = 2.0;

    /**
     * 评价加分配置
     */
    private EvaluationConfig evaluation = new EvaluationConfig();

    /**
     * 交易加分配置
     */
    private TransactionConfig transaction = new TransactionConfig();

    /**
     * 认证加分配置
     */
    private AuthConfig auth = new AuthConfig();

    /**
     * 减分配置
     */
    private PenaltyConfig penalty = new PenaltyConfig();

    /**
     * 衰减配置
     */
    private DecayConfig decay = new DecayConfig();

    /**
     * 刷分检测配置
     */
    private AntiSpamConfig antiSpam = new AntiSpamConfig();

    @Data
    public static class EvaluationConfig {
        /**
         * 好评基础分（4-5星）
         */
        private Double goodBase = 1.0;

        /**
         * 好评最高分（含质量加成）
         */
        private Double goodMax = 2.0;

        /**
         * 中评分数（3星）
         */
        private Double neutral = -1.0;

        /**
         * 2星差评扣分
         * @deprecated 评分逻辑已优化，直接使用硬编码值
         */
        @Deprecated
        private Double bad2Star = -3.0;

        /**
         * 1星差评扣分
         * @deprecated 评分逻辑已优化，直接使用硬编码值
         */
        @Deprecated
        private Double bad1Star = -5.0;

        // 保留旧字段名以兼容
        private Double badMin = -3.0;
        private Double badMax = -5.0;
    }

    @Data
    public static class TransactionConfig {
        private Double smallMin = 0.0;
        private Double smallMax = 50.0;
        private Double smallScore = 0.5;

        private Double mediumMin = 50.0;
        private Double mediumMax = 200.0;
        private Double mediumScore = 1.0;

        private Double largeMin = 200.0;
        private Double largeMax = 1000.0;
        private Double largeScore = 1.5;

        private Double hugeMin = 1000.0;
        private Double hugeScore = 2.0;
    }

    @Data
    public static class AuthConfig {
        private Double realName = 5.0;
        private Double student = 3.0;
        private Double phone = 1.0;
        private Double email = 0.5;
    }

    @Data
    public static class PenaltyConfig {
        private Double cancelOrder = -2.0;
        private Double reportPunished = -10.0;
        private Double violation = -20.0;
        private Double productRemoved = -5.0;
    }

    @Data
    public static class DecayConfig {
        private Integer inactiveDays = 90;
        private Double decayRate = 0.5;
        private Integer decayMin = 60;
    }

    @Data
    public static class AntiSpamConfig {
        /**
         * 同一对手交易次数阈值（7天内）
         */
        private Integer sameCounterpartyThreshold = 3;

        /**
         * 小额交易次数阈值（7天内）
         */
        private Integer smallAmountThreshold = 5;

        /**
         * 小额交易金额上限
         */
        private Double smallAmountLimit = 50.0;

        /**
         * 单日交易次数阈值
         */
        private Integer dailyTransactionThreshold = 5;

        /**
         * 互评检测时间范围（天）
         */
        private Integer mutualEvaluationDays = 30;

        /**
         * 快速增长阈值（7天内增长分数）
         */
        private Integer rapidGrowthThreshold = 20;
    }
}
