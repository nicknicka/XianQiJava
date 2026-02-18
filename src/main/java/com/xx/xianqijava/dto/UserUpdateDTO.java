package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新信息 DTO
 */
@Data
@Schema(description = "用户更新信息请求")
public class UserUpdateDTO {

    @Schema(description = "昵称")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "真实姓名")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "学院")
    @Size(max = 100, message = "学院长度不能超过100个字符")
    private String college;

    @Schema(description = "专业")
    @Size(max = 100, message = "专业长度不能超过100个字符")
    private String major;
}
