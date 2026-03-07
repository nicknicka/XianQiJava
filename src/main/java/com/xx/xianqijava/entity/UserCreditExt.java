package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信用分扩展实体
 * 用于存储信用分相关扩展信息，不修改原 user 表
 */
@Data
@TableName("user_credit_ext")
public class UserCreditExt {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 信用等级
     */
    private String creditLevel;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 上次信用分衰减时间
     */
    private LocalDateTime lastCreditDecayTime;

    /**
     * 好评总数
     */
    private Integer totalPositiveEvaluations;

    /**
     * 中评总数
     */
    private Integer totalNeutralEvaluations;

    /**
     * 差评总数
     */
    private Integer totalNegativeEvaluations;

    /**
     * 芝麻信用分
     */
    private Integer zhimaCreditScore;

    /**
     * 芝麻信用授权时间
     */
    private LocalDateTime zhimaAuthTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
