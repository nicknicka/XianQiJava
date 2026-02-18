package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建预约借用DTO
 */
@Data
@Schema(description = "创建预约借用请求")
public class ShareItemBookingCreateDTO {

    @NotNull(message = "共享物品ID不能为空")
    @Schema(description = "共享物品ID")
    private Long shareId;

    @NotNull(message = "借用开始日期不能为空")
    @Schema(description = "借用开始日期")
    private LocalDate startDate;

    @NotNull(message = "借用结束日期不能为空")
    @Schema(description = "借用结束日期")
    private LocalDate endDate;

    @Schema(description = "借用说明/备注")
    private String remark;
}
