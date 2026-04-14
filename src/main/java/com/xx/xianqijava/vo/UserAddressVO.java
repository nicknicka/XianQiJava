package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户地址视图对象
 */
@Data
@Schema(description = "用户地址")
public class UserAddressVO {

    @Schema(description = "地址ID")
    private String addressId;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区/县")
    private String district;

    @Schema(description = "详细地址")
    private String detailAddress;

    @Schema(description = "完整地址")
    private String fullAddress;

    @Schema(description = "邮政编码")
    private String postalCode;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;
}
