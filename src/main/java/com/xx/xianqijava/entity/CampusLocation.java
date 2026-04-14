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
 * 校园位置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("campus_location")
@Schema(description = "校园位置")
public class CampusLocation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "位置ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long locationId;

    @Schema(description = "位置名称")
    private String name;

    @Schema(description = "位置代码")
    private String code;

    @Schema(description = "位置描述")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
