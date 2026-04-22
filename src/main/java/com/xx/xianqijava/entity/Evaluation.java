package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价表
 */
@Data
@TableName("evaluation")
@Schema(description = "评价")
public class Evaluation {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "评价ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long evalId;

    @Schema(description = "订单ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    @Schema(description = "评价人ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromUserId;

    @Schema(description = "被评价人ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toUserId;

    @Schema(description = "评分：1-5星")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "标签（JSON）")
    private String tags;

    @Schema(description = "评价图片（JSON）")
    private String images;

    @Schema(description = "追评内容")
    private String appendContent;

    @Schema(description = "追评图片（JSON）")
    private String appendImages;

    @Schema(description = "追评时间")
    private LocalDateTime appendTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
