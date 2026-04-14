package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 热门标签视图对象
 */
@Data
@Schema(description = "热门搜索标签")
public class HotTagVO {

    @Schema(description = "标签ID")
    private String tagId;

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "搜索次数")
    private Integer searchCount;

    @Schema(description = "排序")
    private Integer sortOrder;
}
