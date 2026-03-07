package com.xx.xianqijava.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员信息VO
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Data
@Schema(description = "管理员信息")
public class AdminInfoVO {

    @Schema(description = "管理员ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "是否启用：0-禁用 1-启用")
    private Integer isActive;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;
}
