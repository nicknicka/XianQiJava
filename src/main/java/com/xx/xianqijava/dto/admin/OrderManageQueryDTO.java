package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单管理查询条件DTO - 管理端
 */
@Data
@Schema(description = "订单管理查询条件DTO")
public class OrderManageQueryDTO {

    @Schema(description = "订单号（模糊搜索）")
    private String orderNo;

    @Schema(description = "商品名称（模糊搜索）")
    private String productTitle;

    @Schema(description = "买家ID")
    private Long buyerId;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "订单状态：0-待确认，1-进行中，2-已完成，3-已取消，4-退款中")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段：createTime/totalAmount/completeTime", example = "createTime")
    private String sortBy = "createTime";

    @Schema(description = "排序方式：asc/desc", example = "desc")
    private String sortOrder = "desc";
}
