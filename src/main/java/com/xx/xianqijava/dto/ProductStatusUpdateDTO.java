package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新商品状态DTO
 */
@Data
@Schema(description = "更新商品状态DTO")
public class ProductStatusUpdateDTO {

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-下架，1-在售，2-已售", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
}
