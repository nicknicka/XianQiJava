package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("conversation")
@Schema(description = "会话")
public class Conversation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "会话类型：1-单聊，2-群聊")
    private Integer conversationType;

    @Schema(description = "用户1ID")
    private Long userId1;

    @Schema(description = "用户2ID")
    private Long userId2;

    @Schema(description = "关联订单ID")
    private Long relatedOrderId;

    @Schema(description = "最后一条消息ID")
    private Long lastMessageId;

    @Schema(description = "最后一条消息内容")
    private String lastMessageContent;

    @Schema(description = "最后消息时间")
    private java.time.LocalDateTime lastMessageTime;

    @Schema(description = "用户1的未读消息数")
    private Integer unreadCountUser1;

    @Schema(description = "用户2的未读消息数")
    private Integer unreadCountUser2;

    @Schema(description = "用户1是否免打扰：0-否，1-是")
    private Integer isMutedUser1;

    @Schema(description = "用户2是否免打扰：0-否，1-是")
    private Integer isMutedUser2;

    @Schema(description = "用户1对会话的备注名")
    private String remarkUser1;

    @Schema(description = "用户2对会话的备注名")
    private String remarkUser2;

    @Schema(description = "用户1是否归档：0-否，1-是")
    private Integer isArchivedUser1;

    @Schema(description = "用户2是否归档：0-否，1-是")
    private Integer isArchivedUser2;

    @Schema(description = "状态：0-正常，1-删除，2-置顶")
    private Integer status;
}
