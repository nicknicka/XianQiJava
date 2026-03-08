package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 热门搜索标签表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hot_tag")
@Schema(description = "热门搜索标签")
public class HotTag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "关键词")
    private String keyword;

    @TableField("click_count")
    @Schema(description = "搜索次数")
    private Integer searchCount;

    @Schema(description = "排序（数值越小越靠前）")
    private Integer sortOrder;

    @TableField("is_active")
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
