package com.xx.xianqijava.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员登录响应VO
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Data
@Schema(description = "管理员登录响应")
public class AdminLoginVO {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "管理员信息")
    private AdminInfoVO adminInfo;
}
