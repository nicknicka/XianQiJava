package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 填写退货物流 DTO
 */
@Data
@Schema(description = "填写退货物流请求")
public class RefundLogisticsDTO {

    @Schema(description = "物流公司", required = true)
    @NotBlank(message = "物流公司不能为空")
    @Size(max = 100, message = "物流公司名称长度不能超过100个字符")
    private String logisticsCompany;

    @Schema(description = "物流单号", required = true)
    @NotBlank(message = "物流单号不能为空")
    @Size(max = 50, message = "物流单号长度不能超过50个字符")
    private String logisticsNo;
}
