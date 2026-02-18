package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付押金DTO
 */
@Data
@Schema(description = "支付押金请求")
public class DepositPayDTO {

    @NotNull(message = "预约ID不能为空")
    @Schema(description = "预约ID")
    private Long bookingId;

    @NotNull(message = "支付方式不能为空")
    @Schema(description = "支付方式：1-余额，2-支付宝，3-微信")
    private Integer paymentMethod;
}
