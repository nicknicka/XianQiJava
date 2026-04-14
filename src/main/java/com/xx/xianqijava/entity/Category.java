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
 * 分类表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
@Schema(description = "分类")
public class Category extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分类ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类代码")
    private String code;

    @Schema(description = "父分类ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    @Schema(description = "图标（emoji）")
    private String icon;

    @Schema(description = "渐变色背景（CSS）")
    private String gradient;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
