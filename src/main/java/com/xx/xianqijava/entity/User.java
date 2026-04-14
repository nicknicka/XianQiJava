package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
@Schema(description = "用户")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码（加密）")
    private String password;

    @Schema(description = "支付密码（加密）")
    private String payPassword;

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

    @Schema(description = "允许手机号搜索：0-否，1-是")
    private Integer phoneSearchEnabled;

    @Schema(description = "显示位置信息：0-否，1-是")
    private Integer locationEnabled;

    @Schema(description = "微信OpenID")
    private String wechatOpenid;

    @Schema(description = "微信UnionID")
    private String wechatUnionid;

    @Schema(description = "QQ OpenID")
    private String qqOpenid;

    @Schema(description = "最后登录时间")
    private java.time.LocalDateTime lastLoginTime;
}
