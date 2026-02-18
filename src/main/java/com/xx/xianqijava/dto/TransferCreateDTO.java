package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建转赠请求DTO
 */
@Data
@Schema(description = "创建转赠请求")
public class TransferCreateDTO {

    @NotNull(message = "共享物品ID不能为空")
    @Schema(description = "共享物品ID")
    private Long shareId;

    @NotNull(message = "接收人ID不能为空")
    @Schema(description = "接收人ID")
    private Long toUserId;

    @Schema(description = "转赠说明")
    private String transferNote;
}
