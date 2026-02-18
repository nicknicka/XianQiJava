package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户实名认证DTO
 */
@Data
@Schema(description = "用户实名认证请求")
public class UserVerificationDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    @Schema(description = "身份证号")
    private String idCard;

    @NotBlank(message = "学生证图片不能为空")
    @Schema(description = "学生证图片URL")
    private String studentCardImage;

    @Schema(description = "身份证正面图片URL")
    private String idCardFrontImage;

    @Schema(description = "身份证背面图片URL")
    private String idCardBackImage;
}
