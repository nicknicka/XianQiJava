package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀场次 VO
 */
@Data
@Schema(description = "秒杀场次信息")
public class FlashSaleSessionVO {

    @Schema(description = "场次ID")
    private Long sessionId;

    @Schema(description = "场次时间（HH:mm格式）")
    private String time;

    @Schema(description = "状态：upcoming-即将开始，ongoing-进行中，ended-已结束")
    private String status;

    @Schema(description = "进度百分比（0-100）")
    private Integer progress;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
