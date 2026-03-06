package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用户反馈DTO
 */
@Data
@Schema(description = "用户反馈请求")
public class UserFeedbackDTO {

    @Schema(description = "联系方式（邮箱/手机号）")
    private String contact;

    @NotBlank(message = "反馈类型不能为空")
    @Schema(description = "反馈类型：bug-功能异常，suggestion-功能建议，other-其他问题")
    private String type;

    @Schema(description = "反馈标题（选填）")
    private String title;

    @NotBlank(message = "反馈内容不能为空")
    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> images;
}
