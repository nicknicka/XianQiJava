package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户反馈DTO
 */
@Data
@Schema(description = "用户反馈请求")
public class UserFeedbackDTO {

    @Schema(description = "联系方式（邮箱/手机号）")
    private String contact;

    @NotNull(message = "反馈类型不能为空")
    @Schema(description = "反馈类型：1-功能建议，2-Bug反馈，3-投诉，4-其他", required = true)
    private Integer type;

    @NotBlank(message = "反馈标题不能为空")
    @Schema(description = "反馈标题", required = true)
    private String title;

    @NotBlank(message = "反馈内容不能为空")
    @Schema(description = "反馈内容", required = true)
    private String content;

    @Schema(description = "图片URL列表（JSON数组）")
    private String images;
}
