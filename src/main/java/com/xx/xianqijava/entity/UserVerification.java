package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实名认证记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_verification")
@Schema(description = "用户实名认证记录")
public class UserVerification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "认证ID")
    private Long verificationId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "学生证图片URL")
    private String studentCardImage;

    @Schema(description = "身份证正面图片URL")
    private String idCardFrontImage;

    @Schema(description = "身份证背面图片URL")
    private String idCardBackImage;

    @Schema(description = "认证状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer status;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "审核时间")
    private java.time.LocalDateTime auditTime;

    @Schema(description = "审核人ID")
    private Long auditorId;
}
