package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 实名认证提交DTO
 */
@Data
@Schema(description = "实名认证提交信息")
public class RealNameAuthSubmitDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Schema(description = "身份证号")
    private String idCard;
}
