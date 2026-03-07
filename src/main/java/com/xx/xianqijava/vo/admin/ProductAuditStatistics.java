package com.xx.xianqijava.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品审核统计信息VO - 管理端
 */
@Data
@Schema(description = "商品审核统计信息VO")
public class ProductAuditStatistics {

    @Schema(description = "总商品数")
    private Long totalProducts;

    @Schema(description = "待审核商品数")
    private Long pendingAuditCount;

    @Schema(description = "已通过商品数")
    private Long approvedCount;

    @Schema(description = "已拒绝商品数")
    private Long rejectedCount;

    @Schema(description = "今日待审核商品数")
    private Long todayPendingCount;

    @Schema(description = "本周审核通过数")
    private Long weekApprovedCount;

    @Schema(description = "本月审核通过数")
    private Long monthApprovedCount;
}
