package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品审核DTO
 */
@Data
@Schema(description = "商品审核请求")
public class ProductAuditDTO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID")
    private Long productId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态：1-审核通过，2-审核拒绝")
    private Integer auditStatus;

    @Schema(description = "审核意见")
    private String auditRemark;
}
