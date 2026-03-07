package com.xx.xianqijava.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户统计信息VO - 管理端
 */
@Data
@Schema(description = "用户统计信息VO")
public class UserStatisticsInfo {

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "正常用户数")
    private Long normalUsers;

    @Schema(description = "封禁用户数")
    private Long bannedUsers;

    @Schema(description = "实名认证用户数")
    private Long verifiedUsers;

    @Schema(description = "今日新增用户数")
    private Long todayNewUsers;

    @Schema(description = "本周新增用户数")
    private Long weekNewUsers;

    @Schema(description = "本月新增用户数")
    private Long monthNewUsers;

    @Schema(description = "活跃用户数（7天内登录）")
    private Long activeUsers;
}
