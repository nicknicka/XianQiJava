package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 共享物品状态更新DTO - 管理端
 */
@Data
@Schema(description = "共享物品状态更新DTO")
public class ShareItemStatusUpdateDTO {

    @NotNull(message = "共享物品ID不能为空")
    @Schema(description = "共享物品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long shareId;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-下架，1-可借用，2-借用中", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
