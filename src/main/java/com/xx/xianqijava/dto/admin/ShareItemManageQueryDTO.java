package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 共享物品查询条件DTO - 管理端
 */
@Data
@Schema(description = "共享物品查询条件DTO")
public class ShareItemManageQueryDTO {

    @Schema(description = "物品标题（模糊搜索）")
    private String title;

    @Schema(description = "所有者ID")
    private Long ownerId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "状态：0-下架，1-可借用，2-借用中，4-草稿")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段：createTime/dailyRent/deposit", example = "createTime")
    private String sortBy = "createTime";

    @Schema(description = "排序方式：asc/desc", example = "desc")
    private String sortOrder = "desc";
}
