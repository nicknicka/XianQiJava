package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计数据视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户统计数据")
public class UserStatsVO {

    @Schema(description = "发布商品数")
    private Integer publishCount;

    @Schema(description = "订单数")
    private Integer orderCount;

    @Schema(description = "关注数")
    private Integer followingCount;

    @Schema(description = "粉丝数")
    private Integer followerCount;
}
