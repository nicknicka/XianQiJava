package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息详情 VO
 */
@Data
@Schema(description = "用户信息详情")
public class UserInfoVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "信用分数")
    private Integer creditScore;

    @Schema(description = "状态：0-正常，1-封禁")
    private Integer status;

    @Schema(description = "是否实名认证：0-否，1-是")
    private Integer isVerified;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
