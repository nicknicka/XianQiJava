package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统通知表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_notification")
@Schema(description = "系统通知")
public class SystemNotification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "通知ID")
    private Long notificationId;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容（支持富文本）")
    private String content;

    @Schema(description = "通知类型：1-系统公告，2-活动通知，3-账户提醒，4-交易提醒")
    private Integer type;

    @Schema(description = "目标类型：1-全部用户，2-指定用户，3-指定等级")
    private Integer targetType;

    @Schema(description = "指定用户ID列表（JSON数组）")
    private String targetUsers;

    @Schema(description = "目标用户等级")
    private Integer targetLevel;

    @Schema(description = "已读用户ID列表（JSON数组）")
    private String isRead;

    @Schema(description = "跳转链接类型：1-无，2-网页，3-商品详情，4-订单详情")
    private Integer linkType;

    @Schema(description = "跳转URL")
    private String linkUrl;

    @Schema(description = "关联商品ID")
    private Long linkProductId;

    @Schema(description = "关联订单ID")
    private Long linkOrderId;

    @Schema(description = "发布时间")
    private java.time.LocalDateTime publishTime;

    @Schema(description = "状态：0-草稿，1-已发布，2-已撤回")
    private Integer status;

    @Schema(description = "优先级：1-低，2-中，3-高")
    private Integer priority;
}
