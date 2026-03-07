package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新用户状态DTO - 管理端
 */
@Data
@Schema(description = "更新用户状态DTO")
public class UserUpdateStatusDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-正常，1-封禁", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "封禁原因（封禁时必填）")
    private String reason;
}
