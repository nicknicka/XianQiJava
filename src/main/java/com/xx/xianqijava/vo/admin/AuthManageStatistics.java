package com.xx.xianqijava.vo.admin;

import lombok.Data;

/**
 * 认证管理统计信息
 */
@Data
public class AuthManageStatistics {

    // ==================== 实名认证统计 ====================

    /**
     * 实名认证总数
     */
    private Long realNameTotal;

    /**
     * 实名认证待审核数
     */
    private Long realNamePending;

    /**
     * 实名认证已通过数
     */
    private Long realNameApproved;

    /**
     * 实名认证已拒绝数
     */
    private Long realNameRejected;

    /**
     * 今日实名认证通过数
     */
    private Long realNameTodayApproved;

    /**
     * 今日实名认证拒绝数
     */
    private Long realNameTodayRejected;

    // ==================== 学号认证统计 ====================

    /**
     * 学号认证总数
     */
    private Long studentTotal;

    /**
     * 学号认证待审核数
     */
    private Long studentPending;

    /**
     * 学号认证已通过数
     */
    private Long studentApproved;

    /**
     * 学号认证已拒绝数
     */
    private Long studentRejected;

    /**
     * 今日学号认证通过数
     */
    private Long studentTodayApproved;

    /**
     * 今日学号认证拒绝数
     */
    private Long studentTodayRejected;
}
