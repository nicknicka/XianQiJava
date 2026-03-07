package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信用分预警记录实体
 */
@Data
@TableName("credit_alert")
public class CreditAlert {

    /**
     * 预警ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 预警类型
     */
    private String alertType;

    /**
     * 预警级别
     */
    private String alertLevel;

    /**
     * 预警数据（JSON）
     */
    private String alertData;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 处理人ID
     */
    private String handledBy;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 预警类型枚举
     */
    public enum AlertType {
        RAPID_GROWTH("rapid_growth", "快速增长"),
        FREQUENT_TRADING("frequent_trading", "频繁交易"),
        MUTUAL_EVALUATION("mutual_evaluation", "互评刷分"),
        ABNORMAL_DECAY("abnormal_decay", "异常衰减");

        private final String code;
        private final String desc;

        AlertType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 预警级别枚举
     */
    public enum AlertLevel {
        LOW("low", "低"),
        MEDIUM("medium", "中"),
        HIGH("high", "高"),
        CRITICAL("critical", "严重");

        private final String code;
        private final String desc;

        AlertLevel(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 处理状态枚举
     */
    public enum Status {
        PENDING("pending", "待处理"),
        REVIEWING("reviewing", "审核中"),
        PROCESSED("processed", "已处理"),
        IGNORED("ignored", "已忽略");

        private final String code;
        private final String desc;

        Status(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
