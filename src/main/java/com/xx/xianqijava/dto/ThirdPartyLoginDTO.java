package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 第三方登录请求 DTO
 */
@Data
@Schema(description = "第三方登录请求")
public class ThirdPartyLoginDTO {

    @Schema(description = "第三方授权码")
    @NotBlank(message = "授权码不能为空")
    private String code;

    @Schema(description = "昵称（可选，前端传入备用）")
    private String nickname;

    @Schema(description = "头像URL（可选，前端传入备用）")
    private String avatar;
}
