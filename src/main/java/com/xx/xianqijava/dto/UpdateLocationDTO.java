package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新位置DTO
 */
@Data
@Schema(description = "更新用户位置请求")
public class UpdateLocationDTO {

    @NotNull(message = "纬度不能为空")
    @Schema(description = "纬度")
    private BigDecimal latitude;

    @NotNull(message = "经度不能为空")
    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区/县")
    private String district;
}
