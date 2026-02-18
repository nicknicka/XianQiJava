package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传响应 VO
 */
@Data
@Schema(description = "文件上传响应")
public class FileUploadVO {

    @Schema(description = "文件URL")
    private String url;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "文件大小（字节）")
    private Long size;

    @Schema(description = "文件扩展名")
    private String extension;
}
