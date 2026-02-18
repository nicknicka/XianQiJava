package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 趋势数据VO
 */
@Data
@Schema(description = "趋势数据")
public class TrendDataVO {

    @Schema(description = "日期（格式：MM-dd）")
    private String date;

    @Schema(description = "数量")
    private Long count;
}
