package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 快捷回复DTO
 */
@Data
@Schema(description = "快捷回复请求")
public class QuickReplyDTO {

    @NotBlank(message = "模板标题不能为空")
    @Schema(description = "模板标题", required = true)
    private String title;

    @NotBlank(message = "回复内容不能为空")
    @Schema(description = "回复内容", required = true)
    private String content;

    @Schema(description = "分类：交易-询问/交易-确认/其他")
    private String category;

    @Schema(description = "排序")
    private Integer sortOrder;
}
