package com.xx.xianqijava.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送消息DTO
 */
@Data
@Schema(description = "发送消息请求")
public class MessageSendDTO {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "消息内容（图片消息时为图片URL）")
    private String content;

    @NotNull(message = "消息类型不能为空")
    @Schema(description = "类型：1-文本，2-图片，3-商品卡片，4-订单卡片，5-系统通知，6-引用消息")
    private Integer type;

    @Schema(description = "引用的父消息ID")
    private Long parentMessageId;

    @Schema(description = "扩展数据（JSON）")
    private String extraData;
}
