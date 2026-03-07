package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单退款处理DTO - 管理端
 */
@Data
@Schema(description = "订单退款处理DTO")
public class OrderRefundProcessDTO {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotNull(message = "处理结果不能为空")
    @Schema(description = "处理结果：1-同意退款，2-拒绝退款", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer result;

    @Schema(description = "处理备注（拒绝时必填）")
    private String remark;
}
