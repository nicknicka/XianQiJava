package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送图片消息DTO
 */
@Data
@Schema(description = "发送图片消息请求")
public class ImageMessageSendDTO {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID")
    private Long conversationId;

    @NotBlank(message = "图片URL不能为空")
    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "图片宽度（像素）")
    private Integer width;

    @Schema(description = "图片高度（像素）")
    private Integer height;

    @Schema(description = "图片大小（字节）")
    private Long size;

    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "引用的父消息ID")
    private Long parentMessageId;
}
