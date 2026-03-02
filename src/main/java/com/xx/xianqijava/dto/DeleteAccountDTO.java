package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注销账号 DTO
 */
@Data
@Schema(description = "注销账号请求")
public class DeleteAccountDTO {

    @Schema(description = "登录密码（用于验证身份）")
    @NotBlank(message = "密码不能为空")
    private String password;
}
