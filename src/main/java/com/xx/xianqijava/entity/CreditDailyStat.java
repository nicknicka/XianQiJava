package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日信用分统计实体
 */
@Data
@TableName("credit_daily_stat")
public class CreditDailyStat {

    /**
     * 统计ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 交易加分
     */
    private BigDecimal transactionGain;

    /**
     * 交易次数
     */
    private Integer transactionCount;

    /**
     * 评价加分
     */
    private BigDecimal evaluationGain;

    /**
     * 评价次数
     */
    private Integer evaluationCount;

    /**
     * 其他加分
     */
    private BigDecimal otherGain;

    /**
     * 总加分
     */
    private BigDecimal totalGain;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
