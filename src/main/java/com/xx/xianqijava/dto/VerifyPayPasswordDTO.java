package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证支付密码 DTO
 */
@Data
@Schema(description = "验证支付密码请求")
public class VerifyPayPasswordDTO {

    @Schema(description = "支付密码")
    @NotBlank(message = "支付密码不能为空")
    private String password;
}
