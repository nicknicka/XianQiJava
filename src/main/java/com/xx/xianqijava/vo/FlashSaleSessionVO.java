package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀场次 VO（已简化，合并了活动功能）
 */
@Data
@Schema(description = "秒杀场次信息")
public class FlashSaleSessionVO {

    @Schema(description = "场次ID")
    private String sessionId;

    @Schema(description = "场次名称")
    private String name;

    @Schema(description = "场次描述")
    private String description;

    @Schema(description = "场次时间（HH:mm格式）")
    private String time;

    @Schema(description = "进度百分比（0-100）")
    private Integer progress;

    @Schema(description = "状态：upcoming-即将开始, ongoing-进行中, ended-已结束")
    private String status;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "商品数量")
    private Integer productCount;
}
