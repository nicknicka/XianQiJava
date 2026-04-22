package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 追加评价 DTO
 */
@Data
@Schema(description = "追加评价请求")
public class EvaluationAppendDTO {

    @Schema(description = "追加评价内容")
    @Size(max = 500, message = "追加评价内容长度不能超过500个字符")
    private String content;

    @Schema(description = "追加评价图片URL列表，最多3张")
    @Size(max = 3, message = "追加评价图片最多上传3张")
    private List<@Size(max = 1024, message = "图片地址长度不能超过1024个字符") String> images;
}
