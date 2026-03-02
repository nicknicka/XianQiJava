package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀活动 VO
 */
@Data
@Schema(description = "秒杀活动信息")
public class FlashSaleActivityVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：0-未开始，1-进行中，2-已结束，3-已取消")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "商品数量")
    private Integer productCount;
}
