package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建举报DTO
 */
@Data
@Schema(description = "创建举报请求")
public class ReportCreateDTO {

    @NotNull(message = "被举报人ID不能为空")
    @Schema(description = "被举报人ID", required = true)
    private Long reportedUserId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "消息ID")
    private Long messageId;

    @NotBlank(message = "举报原因不能为空")
    @Schema(description = "举报原因：欺诈/骚扰/虚假信息/其他", required = true)
    private String reason;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "证据图片URL列表（JSON数组）")
    private String evidenceImages;
}
