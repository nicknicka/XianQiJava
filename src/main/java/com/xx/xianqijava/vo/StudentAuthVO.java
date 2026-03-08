package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学生认证信息VO
 */
@Data
@Schema(description = "学生认证信息")
public class StudentAuthVO {

    @Schema(description = "认证ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "学院")
    private String college;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "学生证图片URL列表")
    private List<String> studentCardImages;

    @Schema(description = "认证状态：0-未认证 1-审核中 2-已认证 3-认证失败")
    private Integer status;

    @Schema(description = "认证失败原因")
    private String rejectReason;

    @Schema(description = "入学年份")
    private String enrollmentYear;

    @Schema(description = "毕业年份")
    private String graduationYear;

    @Schema(description = "学历层次")
    private String educationLevel;

    @Schema(description = "审核时间")
    private String auditedAt;

    @Schema(description = "创建时间")
    private String createdAt;
}
