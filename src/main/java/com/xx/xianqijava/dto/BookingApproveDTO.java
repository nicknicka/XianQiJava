package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批借用DTO
 */
@Data
@Schema(description = "审批借用请求")
public class BookingApproveDTO {

    @NotNull(message = "预约ID不能为空")
    @Schema(description = "预约ID")
    private Long bookingId;

    @NotNull(message = "审批状态不能为空")
    @Schema(description = "审批状态：1-批准，2-拒绝")
    private Integer status;

    @Schema(description = "审批备注")
    private String approveRemark;
}
