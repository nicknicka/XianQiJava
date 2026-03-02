package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 秒杀活动表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flash_sale_activity")
@Schema(description = "秒杀活动")
public class FlashSaleActivity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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

    @Schema(description = "排序权重")
    private Integer sortOrder;
}
