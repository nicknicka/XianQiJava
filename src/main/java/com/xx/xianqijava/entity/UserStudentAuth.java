package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户学生认证表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_student_auth")
@Schema(description = "用户学生认证")
public class UserStudentAuth extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "学生证图片（JSON数组）")
    private String studentCardImages;

    @Schema(description = "认证状态：0-未认证 1-审核中 2-已认证 3-认证失败")
    private Integer status;

    @Schema(description = "认证失败原因")
    private String rejectReason;

    @Schema(description = "审核人ID")
    private Long auditedBy;

    @Schema(description = "审核时间")
    private LocalDateTime auditedAt;

    @Schema(description = "入学年份")
    private String enrollmentYear;

    @Schema(description = "毕业年份")
    private String graduationYear;

    @Schema(description = "学历层次：本科/硕士/博士")
    private String educationLevel;

    // 覆盖 BaseEntity 的字段映射，使用数据库的 created_at 和 updated_at
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
