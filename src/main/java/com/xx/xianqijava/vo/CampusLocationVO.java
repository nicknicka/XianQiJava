package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 校园位置视图对象
 */
@Data
@Schema(description = "校园位置")
public class CampusLocationVO {

    @Schema(description = "位置ID")
    private String locationId;

    @Schema(description = "位置名称")
    private String name;

    @Schema(description = "位置代码")
    private String code;

    @Schema(description = "位置描述")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
