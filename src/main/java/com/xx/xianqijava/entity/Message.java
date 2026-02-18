package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
@Schema(description = "消息")
public class Message extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "发送者ID")
    private Long fromUserId;

    @Schema(description = "接收者ID")
    private Long toUserId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "类型：1-文本，2-图片，3-商品卡片，4-订单卡片，5-系统通知，6-引用消息")
    private Integer type;

    @Schema(description = "引用的父消息ID")
    private Long parentMessageId;

    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "发送状态：0-发送中，1-成功，2-失败")
    private Integer sendStatus;

    @Schema(description = "送达时间")
    private LocalDateTime deliveredTime;

    @Schema(description = "扩展数据（JSON）")
    private String extraData;

    @Schema(description = "被回复次数")
    private Integer replyCount;

    @Schema(description = "状态：0-正常，1-撤回，2-删除")
    private Integer status;
}
