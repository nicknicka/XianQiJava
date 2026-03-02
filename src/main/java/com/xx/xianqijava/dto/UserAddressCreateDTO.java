package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 创建用户地址DTO
 */
@Data
@Schema(description = "创建用户地址")
public class UserAddressCreateDTO {

    @Schema(description = "联系人姓名")
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @Schema(description = "联系电话")
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    @Schema(description = "省份")
    @NotBlank(message = "省份不能为空")
    private String province;

    @Schema(description = "城市")
    @NotBlank(message = "城市不能为空")
    private String city;

    @Schema(description = "区/县")
    @NotBlank(message = "区/县不能为空")
    private String district;

    @Schema(description = "详细地址")
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    @Schema(description = "邮政编码")
    @Pattern(regexp = "^\\d{6}$", message = "邮政编码格式不正确")
    private String postalCode;

    @Schema(description = "标签：家/公司/学校")
    private String tag;

    @Schema(description = "是否设为默认地址")
    private Boolean isDefault;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;
}
