package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建评价 DTO
 */
@Data
@Schema(description = "创建评价请求")
public class EvaluationCreateDTO {

    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "评分：1-5星", required = true)
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer rating;

    @Schema(description = "评价内容")
    @Size(max = 500, message = "评价内容长度不能超过500个字符")
    private String content;

    @Schema(description = "评价标签（多个标签用逗号分隔）")
    @Size(max = 100, message = "标签长度不能超过100个字符")
    private String tags;

    @Schema(description = "评价对象类型：1-评价卖家 2-评价买家", required = true)
    @NotNull(message = "评价对象类型不能为空")
    private Integer targetType;
}
