package com.xx.xianqijava.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会话VO
 */
@Data
@Schema(description = "会话信息")
public class ConversationVO {

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "会话类型：1-单聊，2-群聊")
    private Integer conversationType;

    @Schema(description = "对方用户ID")
    private Long otherUserId;

    @Schema(description = "对方用户昵称")
    private String otherUserNickname;

    @Schema(description = "对方用户头像")
    private String otherUserAvatar;

    @Schema(description = "对方用户在线状态")
    private Boolean otherUserOnline;

    @Schema(description = "最后一条消息内容")
    private String lastMessageContent;

    @Schema(description = "最后消息时间")
    private String lastMessageTime;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "关联的订单ID")
    private Long relatedOrderId;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "是否免打扰")
    private Integer isMuted;

    @Schema(description = "是否归档")
    private Integer isArchived;
}
