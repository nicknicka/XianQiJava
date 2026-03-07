package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信用分记录实体
 */
@Data
@TableName("credit_record")
public class CreditRecord {

    /**
     * 记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 变化前分数
     */
    private Integer scoreBefore;

    /**
     * 变化分数
     */
    private BigDecimal scoreChange;

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
     * 关联ID
     */
    private String relatedId;

    /**
     * 关联类型
     */
    private String relatedType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 信用变化原因枚举
     */
    public enum Reason {
        /**
         * 完成交易
         */
        TRANSACTION("transaction", "完成交易"),

        /**
         * 收到好评
         */
        EVALUATION_GOOD("evaluation_good", "收到好评"),

        /**
         * 收到中评
         */
        EVALUATION_NEUTRAL("evaluation_neutral", "收到中评"),

        /**
         * 收到差评
         */
        EVALUATION_BAD("evaluation_bad", "收到差评"),

        /**
         * 取消订单
         */
        CANCEL_ORDER("cancel_order", "取消订单"),

        /**
         * 实名认证
         */
        AUTH_REAL_NAME("auth_real_name", "实名认证"),

        /**
         * 学生认证
         */
        AUTH_STUDENT("auth_student", "学生认证"),

        /**
         * 手机绑定
         */
        AUTH_PHONE("auth_phone", "手机绑定"),

        /**
         * 信用分衰减
         */
        DECAY("decay", "长期不活跃衰减"),

        /**
         * 违规惩罚
         */
        PENALTY_VIOLATION("penalty_violation", "违规惩罚"),

        /**
         * 举报成立
         */
        PENALTY_REPORT("penalty_report", "举报成立"),

        /**
         * 商品下架
         */
        PENALTY_PRODUCT_REMOVED("penalty_product_removed", "商品下架"),

        /**
         * 举报被采纳
         */
        REPORT_ACCEPTED("report_accepted", "举报被采纳"),

        /**
         * 发布商品
         */
        PRODUCT_PUBLISH("product_publish", "发布商品"),

        /**
         * 提交反馈
         */
        FEEDBACK("feedback", "提交反馈");

        private final String code;
        private final String desc;

        Reason(String code, String desc) {
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
     * 关联类型枚举
     */
    public enum RelatedType {
        ORDER("order", "订单"),
        EVALUATION("evaluation", "评价"),
        PRODUCT("product", "商品"),
        AUTH("auth", "认证"),
        USER("user", "用户"),
        REPORT("report", "举报");

        private final String code;
        private final String desc;

        RelatedType(String code, String desc) {
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
