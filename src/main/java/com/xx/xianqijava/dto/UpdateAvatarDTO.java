package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新头像DTO
 */
@Data
@Schema(description = "更新头像请求")
public class UpdateAvatarDTO {

    @NotBlank(message = "头像URL不能为空")
    @Schema(description = "头像URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String avatar;
}
