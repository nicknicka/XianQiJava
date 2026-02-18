package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消息VO
 */
@Data
@Schema(description = "消息信息")
public class MessageVO {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "发送者ID")
    private Long fromUserId;

    @Schema(description = "发送者昵称")
    private String fromUserNickname;

    @Schema(description = "发送者头像")
    private String fromUserAvatar;

    @Schema(description = "接收者ID")
    private Long toUserId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "类型：1-文本，2-图片，3-商品卡片，4-订单卡片，5-系统通知，6-引用消息")
    private Integer type;

    @Schema(description = "引用的父消息ID")
    private Long parentMessageId;

    @Schema(description = "引用的父消息内容")
    private String parentMessageContent;

    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;

    @Schema(description = "发送状态：0-发送中，1-成功，2-失败")
    private Integer sendStatus;

    @Schema(description = "扩展数据（JSON）")
    private String extraData;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "是否为当前用户发送的消息")
    private Boolean isMine;
}
