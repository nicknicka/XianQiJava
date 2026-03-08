package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 实名认证信息VO
 */
@Data
@Schema(description = "实名认证信息")
public class RealNameAuthVO {

    @Schema(description = "认证ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "身份证号（脱敏）")
    private String idCard;

    @Schema(description = "认证状态：0-未认证 1-审核中 2-已认证 3-认证失败")
    private Integer status;

    @Schema(description = "认证失败原因")
    private String rejectReason;

    @Schema(description = "审核时间")
    private String auditedAt;

    @Schema(description = "创建时间")
    private String createdAt;
}
