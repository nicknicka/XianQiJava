package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话成员表 - 群聊场景使用
 */
@Data
@TableName("conversation_member")
@Schema(description = "会话成员")
public class ConversationMember {

    @TableId(type = IdType.AUTO)
    @Schema(description = "成员ID")
    private Long memberId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "群昵称")
    private String nickname;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "是否免打扰：0-否，1-是")
    private Integer isMuted;

    @Schema(description = "加入时间")
    private LocalDateTime joinTime;

    @Schema(description = "最后阅读的消息ID")
    private Long lastReadMessageId;
}
