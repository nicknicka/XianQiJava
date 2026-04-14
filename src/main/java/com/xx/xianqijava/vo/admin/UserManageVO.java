package com.xx.xianqijava.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户管理VO - 管理端
 */
@Data
@Schema(description = "用户管理VO")
public class UserManageVO {

    @Schema(description = "用户ID")
    private String userId;

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

    @Schema(description = "注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @Schema(description = "商品数量")
    private Integer productCount;

    @Schema(description = "订单数量")
    private Integer orderCount;

    @Schema(description = "评价数量")
    private Integer evaluationCount;

    @Schema(description = "状态描述")
    public String getStatusDesc() {
        return status == 0 ? "正常" : "封禁";
    }

    @Schema(description = "实名认证状态描述")
    public String getVerifiedDesc() {
        return isVerified == 1 ? "已认证" : "未认证";
    }
}
