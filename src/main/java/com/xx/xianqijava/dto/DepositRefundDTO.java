package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退还押金DTO
 */
@Data
@Schema(description = "退还押金请求")
public class DepositRefundDTO {

    @NotNull(message = "押金记录ID不能为空")
    @Schema(description = "押金记录ID")
    private Long recordId;

    @Schema(description = "扣除原因（如果需要扣除押金）")
    private String deductReason;
}
