package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户实名认证VO
 */
@Data
@Schema(description = "用户实名认证")
public class UserVerificationVO {

    @Schema(description = "认证ID")
    private Long verificationId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "身份证号（脱敏）")
    private String idCard;

    @Schema(description = "学生证图片URL")
    private String studentCardImage;

    @Schema(description = "身份证正面图片URL")
    private String idCardFrontImage;

    @Schema(description = "身份证背面图片URL")
    private String idCardBackImage;

    @Schema(description = "认证状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer status;

    @Schema(description = "认证状态描述")
    private String statusDesc;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "审核时间")
    private String auditTime;

    @Schema(description = "提交时间")
    private String createTime;
}
