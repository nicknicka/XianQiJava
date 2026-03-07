package com.xx.xianqijava.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 共享物品统计信息VO - 管理端
 */
@Data
@Schema(description = "共享物品统计信息VO")
public class ShareItemManageStatistics {

    @Schema(description = "总共享物品数")
    private Long totalItems;

    @Schema(description = "可借用物品数")
    private Long availableItems;

    @Schema(description = "借用中物品数")
    private Long borrowedItems;

    @Schema(description = "下架物品数")
    private Long offlineItems;

    @Schema(description = "今日新增物品数")
    private Long todayNewItems;

    @Schema(description = "本周新增物品数")
    private Long weekNewItems;

    @Schema(description = "本月新增物品数")
    private Long monthNewItems;

    @Schema(description = "总借用次数")
    private Long totalBorrows;

    @Schema(description = "总押金金额")
    private java.math.BigDecimal totalDepositAmount;

    @Schema(description = "总租金收入")
    private java.math.BigDecimal totalRentIncome;
}
