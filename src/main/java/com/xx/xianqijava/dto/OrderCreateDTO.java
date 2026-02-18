package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单 DTO
 */
@Data
@Schema(description = "创建订单请求")
public class OrderCreateDTO {

    @Schema(description = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "购买数量", required = true)
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量不能小于1")
    @Max(value = 99, message = "购买数量不能超过99")
    private Integer quantity;

    @Schema(description = "备注")
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    @Schema(description = "收货地址")
    @Size(max = 200, message = "收货地址长度不能超过200个字符")
    private String address;

    @Schema(description = "联系电话")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    @Schema(description = "联系人姓名")
    @Size(max = 50, message = "联系人姓名长度不能超过50个字符")
    private String contactName;
}
