package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改支付密码 DTO
 */
@Data
@Schema(description = "修改支付密码请求")
public class ChangePayPasswordDTO {

    @Schema(description = "原支付密码")
    @NotBlank(message = "原支付密码不能为空")
    private String oldPassword;

    @Schema(description = "新支付密码（6位数字）")
    @NotBlank(message = "新支付密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "支付密码必须为6位数字")
    private String newPassword;
}
