package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统配置VO
 */
@Data
@Schema(description = "系统配置信息")
public class SystemConfigVO {

    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置类型")
    private String configType;

    @Schema(description = "可选值列表")
    private String valueOptions;

    @Schema(description = "配置说明")
    private String description;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "是否公开")
    private Integer isPublic;

    @Schema(description = "是否系统配置")
    private Integer isSystem;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
