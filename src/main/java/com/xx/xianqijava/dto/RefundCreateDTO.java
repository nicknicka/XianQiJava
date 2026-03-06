package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建退款申请 DTO
 */
@Data
@Schema(description = "创建退款申请请求")
public class RefundCreateDTO {

    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "退款金额", required = true)
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因", required = true)
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 500, message = "退款原因长度不能超过500个字符")
    private String refundReason;

    @Schema(description = "退款类型：1-仅退款 2-退货退款", required = true)
    @NotNull(message = "退款类型不能为空")
    private Integer refundType;

    @Schema(description = "退款凭证图片列表")
    private List<String> evidenceImages;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
