package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建/更新系统配置DTO
 */
@Data
@Schema(description = "创建/更新系统配置请求")
public class SystemConfigCreateDTO {

    @NotBlank(message = "配置键不能为空")
    @Schema(description = "配置键（唯一）")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @NotBlank(message = "配置类型不能为空")
    @Schema(description = "配置类型：string/number/boolean/json")
    private String configType;

    @Schema(description = "可选值列表（JSON）")
    private String valueOptions;

    @Schema(description = "配置说明")
    private String description;

    @NotBlank(message = "分组名称不能为空")
    @Schema(description = "分组名称：basic/upload/payment/email/sms等")
    private String groupName;

    @NotNull(message = "是否公开不能为空")
    @Schema(description = "是否公开：0-否，1-是")
    private Integer isPublic;

    @Schema(description = "是否系统配置：0-否，1-是")
    private Integer isSystem;

    @Schema(description = "排序")
    private Integer sortOrder;
}
