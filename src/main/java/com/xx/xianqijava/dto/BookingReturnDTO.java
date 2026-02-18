package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 归还确认DTO
 */
@Data
@Schema(description = "归还确认请求")
public class BookingReturnDTO {

    @NotNull(message = "预约ID不能为空")
    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "归还备注")
    private String returnRemark;
}
