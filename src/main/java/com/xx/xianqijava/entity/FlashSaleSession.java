package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 秒杀场次表实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("flash_sale_session")
@Schema(description = "秒杀场次")
public class FlashSaleSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "场次ID")
    private Long sessionId;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "场次名称")
    private String sessionName;

    @Schema(description = "场次时间点")
    private java.time.LocalTime sessionTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime startTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态：0-未开始 1-进行中 2-已结束")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
