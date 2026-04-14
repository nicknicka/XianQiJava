package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀场次模板表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flash_sale_session_template")
@Schema(description = "秒杀场次模板")
public class FlashSaleSessionTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "模板ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    @Schema(description = "场次名称")
    private String name;

    @Schema(description = "时间点（HH:mm格式）")
    private String sessionTime;

    @Schema(description = "持续小时数")
    private Integer durationHours;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "是否启用：0-禁用，1-启用")
    private Integer isEnabled;
}
