package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分类视图对象
 */
@Data
@Schema(description = "分类")
public class CategoryVO {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类代码")
    private String code;

    @Schema(description = "图标（emoji）")
    private String icon;

    @Schema(description = "渐变色背景（CSS）")
    private String gradient;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "父分类ID")
    private Long parentId;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "子分类列表")
    private java.util.List<CategoryVO> children;
}
