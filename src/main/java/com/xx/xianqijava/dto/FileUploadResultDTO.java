package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传结果")
public class FileUploadResultDTO {

    @Schema(description = "文件UUID")
    private String uuid;

    @Schema(description = "文件扩展名")
    private String extension;

    @Schema(description = "文件名（UUID.扩展名）")
    private String filename;

    @Schema(description = "文件大小（字节）")
    private Long size;

    @Schema(description = "访问URL（伪地址）")
    private String url;
}
