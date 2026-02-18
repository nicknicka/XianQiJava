package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 敏感词检测DTO
 */
@Data
@Schema(description = "敏感词检测请求")
public class SensitiveWordCheckDTO {

    @NotBlank(message = "检测内容不能为空")
    @Schema(description = "待检测的内容", required = true)
    private String content;

    @Schema(description = "检测类型：1-商品发布，2-消息发送，3-评论")
    private Integer checkType;
}
