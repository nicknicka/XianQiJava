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
 * 浏览历史表
 */
@Data
@TableName("product_view_history")
@Schema(description = "浏览历史")
public class ProductViewHistory {

    @TableId(type = IdType.AUTO)
    @Schema(description = "历史ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long historyId;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "商品ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;

    @Schema(description = "浏览时间")
    private LocalDateTime viewTime;

    @Schema(description = "浏览时长（秒）")
    private Integer viewDuration;
}
