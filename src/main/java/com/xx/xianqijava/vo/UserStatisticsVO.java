package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户统计VO
 */
@Data
@Schema(description = "用户统计")
public class UserStatisticsVO {

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "今日新增用户")
    private Long todayNewUsers;

    @Schema(description = "本周新增用户")
    private Long weekNewUsers;

    @Schema(description = "本月新增用户")
    private Long monthNewUsers;

    @Schema(description = "活跃用户数（7天内登录）")
    private Long activeUsers;

    @Schema(description = "实名认证用户数")
    private Long verifiedUsers;

    @Schema(description = "被封禁用户数")
    private Long bannedUsers;

    @Schema(description = "用户注册趋势（最近30天）")
    private java.util.List<TrendDataVO> registerTrend;
}
