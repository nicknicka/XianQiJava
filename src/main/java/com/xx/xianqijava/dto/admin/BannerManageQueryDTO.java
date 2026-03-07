package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 轮播图查询条件DTO - 管理端
 */
@Data
@Schema(description = "轮播图查询条件DTO")
public class BannerManageQueryDTO {

    @Schema(description = "轮播图标题（模糊搜索）")
    private String title;

    @Schema(description = "链接类型：1-无，2-外链，3-商品详情，4-功能页面")
    private Integer linkType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段：createTime/sortOrder/clickCount/exposureCount", example = "sortOrder")
    private String sortBy = "sortOrder";

    @Schema(description = "排序方式：asc/desc", example = "asc")
    private String sortOrder = "asc";
}
