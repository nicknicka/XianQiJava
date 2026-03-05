package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 学生认证提交DTO
 */
@Data
@Schema(description = "学生认证提交信息")
public class StudentAuthSubmitDTO {

    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号")
    private String studentId;

    @NotBlank(message = "学院不能为空")
    @Schema(description = "学院")
    private String college;

    @NotBlank(message = "专业不能为空")
    @Schema(description = "专业")
    private String major;

    @NotEmpty(message = "学生证图片不能为空")
    @Schema(description = "学生证图片URL列表")
    private List<String> studentCardImages;
}
