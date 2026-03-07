package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品审核查询条件DTO - 管理端
 */
@Data
@Schema(description = "商品审核查询条件DTO")
public class ProductAuditQueryDTO {

    @Schema(description = "商品名称（模糊搜索）")
    private String title;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "审核状态：0-待审核，1-已通过，2-已拒绝")
    private Integer auditStatus;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段：createTime/auditTime/price", example = "createTime")
    private String sortBy = "createTime";

    @Schema(description = "排序方式：asc/desc", example = "desc")
    private String sortOrder = "desc";
}
