package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 敏感词检测结果VO
 */
@Data
@Schema(description = "敏感词检测结果")
public class SensitiveWordCheckVO {

    @Schema(description = "是否包含敏感词")
    private Boolean hasSensitiveWord;

    @Schema(description = "是否通过检测")
    private Boolean passed;

    @Schema(description = "检测到的敏感词列表")
    private List<String> sensitiveWords;

    @Schema(description = "替换后的内容（如果需要替换）")
    private String filteredContent;

    @Schema(description = "提示信息")
    private String message;
}
