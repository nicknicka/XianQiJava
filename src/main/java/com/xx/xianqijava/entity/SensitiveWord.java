package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sensitive_word")
@Schema(description = "敏感词")
public class SensitiveWord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "敏感词ID")
    private Long wordId;

    @Schema(description = "敏感词")
    private String word;

    @Schema(description = "类型：1-禁止词，2-敏感词，3-替换词")
    private Integer type;

    @Schema(description = "替换词（type=3时使用）")
    private String replaceWord;

    @Schema(description = "等级：1-一般，2-严重")
    private Integer level;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
