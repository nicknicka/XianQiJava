package com.xx.xianqijava.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询条件DTO - 管理端
 */
@Data
@Schema(description = "用户查询条件DTO")
public class UserQueryDTO {

    @Schema(description = "用户名（模糊搜索）")
    private String username;

    @Schema(description = "手机号（精确匹配）")
    private String phone;

    @Schema(description = "学号（模糊搜索）")
    private String studentId;

    @Schema(description = "学院（模糊搜索）")
    private String college;

    @Schema(description = "专业（模糊搜索）")
    private String major;

    @Schema(description = "状态：0-正常，1-封禁")
    private Integer status;

    @Schema(description = "是否实名认证：0-否，1-是")
    private Integer isVerified;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段：createTime/creditScore", example = "createTime")
    private String sortBy = "createTime";

    @Schema(description = "排序方式：asc/desc", example = "desc")
    private String sortOrder = "desc";
}
