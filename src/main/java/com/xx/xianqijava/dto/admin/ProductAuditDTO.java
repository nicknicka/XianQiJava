package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品审核DTO - 管理端
 */
@Data
@Schema(description = "商品审核DTO")
public class ProductAuditDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-通过，2-拒绝", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer auditStatus;

    @Schema(description = "审核备注（拒绝时必填）")
    private String auditRemark;
}
