package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 转赠响应DTO
 */
@Data
@Schema(description = "转赠响应请求")
public class TransferRespondDTO {

    @NotNull(message = "转赠记录ID不能为空")
    @Schema(description = "转赠记录ID")
    private Long transferId;

    @NotNull(message = "响应状态不能为空")
    @Schema(description = "响应状态：1-接受，2-拒绝")
    private Integer status;

    @Schema(description = "拒绝原因（status=2时必填）")
    private String rejectReason;
}
