package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_config")
@Schema(description = "系统配置")
public class SystemConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "配置键（唯一）")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置类型：string/number/boolean/json")
    private String configType;

    @Schema(description = "可选值列表（JSON）")
    private String valueOptions;

    @Schema(description = "配置说明")
    private String description;

    @Schema(description = "分组名称：basic/upload/payment/email/sms等")
    private String groupName;

    @Schema(description = "是否公开：0-否，1-是")
    private Integer isPublic;

    @Schema(description = "是否系统配置：0-否，1-是")
    private Integer isSystem;

    @Schema(description = "排序")
    private Integer sortOrder;
}
