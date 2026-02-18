package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 实名认证审核DTO
 */
@Data
@Schema(description = "实名认证审核请求")
public class VerificationAuditDTO {

    @NotNull(message = "认证ID不能为空")
    @Schema(description = "认证ID")
    private Long verificationId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-通过，2-拒绝")
    private Integer status;

    @Schema(description = "审核意见")
    private String auditRemark;
}
